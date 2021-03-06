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
package org.apache.openmeetings.util.crypt;

import java.security.NoSuchAlgorithmException;

public class MD5CryptImplementation implements ICryptString {

	/*
	 * (non-Javadoc)
	 * @see org.apache.openmeetings.utils.crypt.ICryptString#createPassPhrase(java.lang.String)
	 */
	public String createPassPhrase(String userGivenPass) {
		String passPhrase = null;
		try {
			passPhrase = MD5Crypt.crypt(userGivenPass);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		return passPhrase;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.openmeetings.utils.crypt.ICryptString#verifyPassword(java.lang.String, java.lang.String)
	 */
	public Boolean verifyPassword(String passGiven, String passwdFromDb) {
		boolean validPassword = false;
		String salt = passwdFromDb.split("\\$")[2];
	
		try {
			validPassword = passwdFromDb.equals(MD5Crypt.crypt(passGiven, salt));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return validPassword;
	}

}
