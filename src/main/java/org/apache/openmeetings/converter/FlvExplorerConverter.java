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
package org.apache.openmeetings.converter;

import static org.apache.openmeetings.util.OmFileHelper.getStreamsHibernateDir;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.openmeetings.db.dao.file.FileExplorerItemDao;
import org.apache.openmeetings.db.dao.record.FlvRecordingLogDao;
import org.apache.openmeetings.db.entity.file.FileExplorerItem;
import org.apache.openmeetings.util.OpenmeetingsVariables;
import org.apache.openmeetings.util.process.ConverterProcessResult;
import org.apache.openmeetings.util.process.ProcessHelper;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class FlvExplorerConverter extends BaseConverter {

	private static final Logger log = Red5LoggerFactory.getLogger(
			FlvExplorerConverter.class, OpenmeetingsVariables.webAppRootKey);

	// Spring loaded Beans
	@Autowired
	private FileExplorerItemDao fileExplorerItemDaoImpl;
	@Autowired
	private FlvRecordingLogDao flvRecordingLogDaoImpl;
	
	private class FlvDimension {
		public FlvDimension(int width, int height) {
			this.width = width;
			this.height = height;
		}
		public int width = 0;
		public int height = 0;
	}

	public List<ConverterProcessResult> startConversion(Long fileExplorerItemId, String moviePath) {
		List<ConverterProcessResult> returnLog = new ArrayList<ConverterProcessResult>();
		try {
			FileExplorerItem fileExplorerItem = fileExplorerItemDaoImpl.getFileExplorerItemsById(fileExplorerItemId);

			log.debug("fileExplorerItem " + fileExplorerItem.getFileExplorerItemId());

			//  Convert to FLV
			return convertToFLV(fileExplorerItem, moviePath);

			// Add empty pieces at the beginning and end of the wav
			// FIXME: Is this really needed anymore?!

		} catch (Exception err) {
			log.error("[startConversion]", err);
			returnLog.add(new ConverterProcessResult("startConversion", err.getMessage(), err));
		}

		return returnLog;
	}

	private List<ConverterProcessResult> convertToFLV(FileExplorerItem fileExplorerItem, String moviePath) {
		List<ConverterProcessResult> returnLog = new ArrayList<ConverterProcessResult>();
		try {
			String name = "UPLOADFLV_" + fileExplorerItem.getFileExplorerItemId();
			File outputFullFlv = new File(getStreamsHibernateDir(), name + ".flv");

			fileExplorerItem.setIsVideo(true);

			String[] argv_fullFLV = new String[] { getPathToFFMPEG(), "-i", moviePath,
					"-ar", "22050", "-acodec", "libmp3lame", "-ab", "32k",
					"-vcodec", "flv",
					outputFullFlv.getCanonicalPath() };
			// "-s", flvWidth + "x" + flvHeight, 

			ConverterProcessResult returnMapConvertFLV = ProcessHelper.executeScript("uploadFLV ID :: "
					+ fileExplorerItem.getFileExplorerItemId(), argv_fullFLV);
			
			//Parse the width height from the FFMPEG output
			FlvDimension flvDimension = getFlvDimension(returnMapConvertFLV.getError());
			int flvWidth = flvDimension.width;
			int flvHeight = flvDimension.height;
			
			
			fileExplorerItem.setFlvWidth(flvWidth);
			fileExplorerItem.setFlvHeight(flvHeight);

			returnLog.add(returnMapConvertFLV);

			String hashFileFullNameJPEG = "UPLOADFLV_" + fileExplorerItem.getFileExplorerItemId() + ".jpg";
			File outPutJpeg = new File(getStreamsHibernateDir(), name + ".jpg");

			fileExplorerItem.setPreviewImage(hashFileFullNameJPEG);

			String[] argv_previewFLV = new String[] { getPathToFFMPEG(), "-i",
					outputFullFlv.getCanonicalPath(), "-vcodec", "mjpeg", "-vframes", "1", "-an",
					"-f", "rawvideo", "-s", flvWidth + "x" + flvHeight,
					outPutJpeg.getCanonicalPath() };

			returnLog.add(ProcessHelper.executeScript("previewUpload ID :: "
							+ fileExplorerItem.getFileExplorerItemId(),
							argv_previewFLV));

			fileExplorerItemDaoImpl.updateFileOrFolder(fileExplorerItem);

			for (ConverterProcessResult returnMap : returnLog) {
				flvRecordingLogDaoImpl.addFLVRecordingLog("generateFFMPEG", null, returnMap);
			}
		} catch (Exception err) {
			log.error("[convertToFLV]", err);
			returnLog.add(new ConverterProcessResult("convertToFLV", err.getMessage(), err));
		}

		return returnLog;
	}
	
	private FlvDimension getFlvDimension(String txt) throws Exception {
		
		Pattern p = Pattern.compile("\\d{2,4}(x)\\d{2,4}");
		
		Matcher matcher = p.matcher(txt);
		
		while ( matcher.find() ) {
			String foundResolution = txt.substring(matcher.start(), matcher.end());
			
			String[] resultions = foundResolution.split("x");
			
			return new FlvDimension(Integer.valueOf(resultions[0]).intValue(), Integer.valueOf(resultions[1]).intValue());
			
	    }
		
		return null;
	}
	
}
