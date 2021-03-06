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
package org.apache.openmeetings.util;

import java.io.OutputStream;
import java.io.Writer;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.FlyweightAttribute;

/**
 * 
 * @author sebastianwagner
 * 
 */
public class LangExport {
	public static final String FILE_COMMENT = ""
			+ "\n"
			+ "  Licensed to the Apache Software Foundation (ASF) under one\n"
			+ "  or more contributor license agreements.  See the NOTICE file\n"
			+ "  distributed with this work for additional information\n"
			+ "  regarding copyright ownership.  The ASF licenses this file\n"
			+ "  to you under the Apache License, Version 2.0 (the\n"
			+ "  \"License\"); you may not use this file except in compliance\n"
			+ "  with the License.  You may obtain a copy of the License at\n"
			+ "  \n"
			+ "      http://www.apache.org/licenses/LICENSE-2.0\n"
			+ "    	  \n"
			+ "  Unless required by applicable law or agreed to in writing,\n"
			+ "  software distributed under the License is distributed on an\n"
			+ "  \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n"
			+ "  KIND, either express or implied.  See the License for the\n"
			+ "  specific language governing permissions and limitations\n"
			+ "  under the License.\n"
			+ "  \n"
			+ "\n"
			+ "\n"
			+ "###############################################\n"
			+ "This File is auto-generated by the LanguageEditor \n"
			+ "to add new Languages or modify/customize it use the LanguageEditor \n"
			+ "see http://openmeetings.apache.org/LanguageEditor.html for Details \n"
			+ "###############################################";

	public static Document createDocument() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		document.addComment(LangExport.FILE_COMMENT);
		return document;
	}
	
	public static Element createRoot(Document document) {
		Element root = document.addElement("language");
		Namespace xsi = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.add(xsi);
		root.add(new FlyweightAttribute("noNamespaceSchemaLocation", "language.xsd", xsi));
		return root;
	}
	
	public static void serializetoXML(Writer out, String aEncodingScheme, Document doc) throws Exception {
		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setEncoding(aEncodingScheme);
		XMLWriter writer = new XMLWriter(out, outformat);
		writer.write(doc);
		writer.flush();
	}
	
	public static void serializetoXML(OutputStream out, String aEncodingScheme, Document doc) throws Exception {
		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setEncoding(aEncodingScheme);
		XMLWriter writer = new XMLWriter(out, outformat);
		writer.write(doc);
		writer.flush();
	}

}
