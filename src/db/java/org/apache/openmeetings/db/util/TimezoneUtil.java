/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openmeetings.db.util;

import static org.apache.openmeetings.util.OmFileHelper.getLanguagesDir;
import static org.apache.openmeetings.util.OmFileHelper.nameOfTimeZoneFile;
import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.apache.openmeetings.db.dao.basic.ConfigurationDao;
import org.apache.openmeetings.db.entity.user.User;
import org.apache.openmeetings.util.CalendarPatterns;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class TimezoneUtil {
	private static final Logger log = Red5LoggerFactory.getLogger(TimezoneUtil.class, webAppRootKey);
	private static final Map<String, String> ICAL_TZ_MAP = new Hashtable<String, String>();
	private static final Map<Long, String> ID_TZ_MAP = new Hashtable<Long, String>();

	private static void initTimeZones() {
		SAXReader reader = new SAXReader();
		Document document;
		try {
			ICAL_TZ_MAP.clear();
			ID_TZ_MAP.clear();
			document = reader.read(new File(getLanguagesDir(), nameOfTimeZoneFile));

			Element root = document.getRootElement();

			// HACK based on the fact timezones are not changed
			long id = 1;
			for (@SuppressWarnings("rawtypes")
			Iterator it = root.elementIterator("timezone"); it.hasNext();) {
				Element item = (Element) it.next();
				String timeZoneName = item.attributeValue("name");
				String iCal = item.attributeValue("iCal");
				ICAL_TZ_MAP.put(timeZoneName, iCal);
				ID_TZ_MAP.put(id++, iCal);
			}
		} catch (DocumentException e) {
			log.error("Unexpected error while reading old time zone list", e);
		}
	}

	@Autowired
	private ConfigurationDao configurationDao;

	/**
	 * Parameters: ID - the ID for a TimeZone, either an abbreviation such as "PST", a full name such as
	 * "America/Los_Angeles", or a custom ID such as "GMT-8:00". Note that the support of abbreviations is for JDK 1.1.x
	 * compatibility only and full names should be used.
	 * 
	 * Returns: the specified TimeZone, or the GMT zone if the given ID cannot be understood. <br/>
	 * <br/>
	 * TODO: Fall-back mechanism and maybe a log output if the given timeZoneId is not found in the list of available
	 * TimeZones of the current java.util.TimeZone package of the Java SDK the the user is running <br/>
	 * 
	 * @param timeZoneId
	 * @return
	 */

	public TimeZone getTimeZone(String timeZoneId) {

		if (timeZoneId == null || timeZoneId.equals("")) {
			return getDefaultTimeZone();
		}

		// see TODO in comments

		return TimeZone.getTimeZone(timeZoneId);
	}

	/**
	 * @return The current server configured time zone in the table configuration key: "default.timezone"
	 */
	public TimeZone getDefaultTimeZone() {

		String defaultTzName = configurationDao.getConfValue("default.timezone", String.class, "Europe/Berlin");

		TimeZone timeZoneByOmTimeZone = TimeZone.getTimeZone(defaultTzName);

		if (timeZoneByOmTimeZone != null) {
			return timeZoneByOmTimeZone;
		}

		// If everything fails take the servers default one
		log.error("There is no correct time zone set in the configuration of OpenMeetings for the key default.timezone or key is missing in table, using default locale!");
		return TimeZone.getDefault();

	}

	/**
	 * Returns the timezone based on the user profile, if not return the timezone from the server
	 * 
	 * @param user
	 * @return
	 */
	public TimeZone getTimeZone(User user) {

		if (user != null && user.getTimeZoneId() != null) {

			TimeZone timeZone = TimeZone.getTimeZone(user.getTimeZoneId());

			if (timeZone != null) {
				return timeZone;
			}

		}

		// if user has not time zone get one from the server configuration
		return getDefaultTimeZone();
	}

	/**
	 * Return the timezone based on our internal jName
	 * 
	 * @param jName
	 * @return
	 */
	public TimeZone getTimezoneByInternalJName(String jName) {
		if (ICAL_TZ_MAP.isEmpty()) {
			initTimeZones();
		}
		String omTimeZone = ICAL_TZ_MAP.get(jName);

		if (omTimeZone == null) {
			String err = String.format("There is not omTimeZone for this jName: '%s'", jName);
			log.error(err);
			throw new RuntimeException(err);
		}
		
		TimeZone timeZone = TimeZone.getTimeZone(omTimeZone);

		if (timeZone != null) {
			return timeZone;
		}
		// if user has not time zone get one from the server configuration
		return getDefaultTimeZone();
	}

	/**
	 * Return the timezone based Id from omTimeZone table
	 * 
	 * @param jName
	 * @return
	 */
	public TimeZone getTimezoneByOmTimeZoneId(Long omtimezoneId) {
		if (ID_TZ_MAP.isEmpty()) {
			initTimeZones();
		}
		String omTimeZone = ID_TZ_MAP.get(omtimezoneId);

		TimeZone timeZone = TimeZone.getTimeZone(omTimeZone);

		if (timeZone != null) {
			return timeZone;
		}

		// if user has not time zone get one from the server configuration
		return getDefaultTimeZone();
	}

	/**
	 * We ignore the fact that a Date Object is always in UTC internally and treat it as if it contains only dd.mm.yyyy
	 * HH:mm:ss. We need to do this cause we cannot trust the Date Object send from the client. We have the timeZone
	 * information additional to the Date, so we use it to transform it now to a Calendar Object.
	 * 
	 * @param dateTime
	 * @param timezone
	 * @return
	 */
	public static Calendar getCalendarInTimezone(String dateTimeStr, TimeZone timezone) {

		Date dateTime = CalendarPatterns.parseImportDate(dateTimeStr);

		Calendar calOrig = Calendar.getInstance();
		calOrig.setTime(dateTime);

		Calendar cal = Calendar.getInstance(timezone);
		cal.set(Calendar.YEAR, calOrig.get(Calendar.YEAR));
		cal.set(Calendar.MONTH, calOrig.get(Calendar.MONTH));
		cal.set(Calendar.DATE, calOrig.get(Calendar.DATE));
		cal.set(Calendar.HOUR_OF_DAY, calOrig.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, calOrig.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, calOrig.get(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, 0);

		return cal;
	}

	public static long _getOffset(TimeZone timezone) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(timezone);
		return cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
	}

}
