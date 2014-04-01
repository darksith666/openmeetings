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
package org.apache.openmeetings.backup;

import org.apache.openmeetings.db.dao.room.RoomTypeDao;
import org.apache.openmeetings.db.entity.room.RoomType;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class RoomTypeConverter extends OmConverter<RoomType> {
	private RoomTypeDao dao;
	
	public RoomTypeConverter() {
		//default constructor is for export
	}
	
	public RoomTypeConverter(RoomTypeDao dao) {
		this.dao = dao;
	}
	
	public RoomType read(InputNode node) throws Exception {
		RoomType rt = dao.get(getlongValue(node));
		return rt != null ? rt : dao.get(1); // conference type will be used in case of bad type
	}

	public void write(OutputNode node, RoomType value) throws Exception {
		node.setData(true);
		node.setValue(value == null ? "0" : "" + value.getRoomtypes_id());
	}
}