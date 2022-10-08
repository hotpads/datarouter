/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.util.xml;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.datarouter.scanner.Scanner;

public class XmlDocumentTool{

	public static final String
			NONVALIDATING_LOAD_DTD_GRAMMAR = "http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
			NONVALIDATING_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

	public static DocumentBuilder getDocumentBuilder(Map<String,Boolean> features){
		var dbFactory = DocumentBuilderFactory.newDefaultInstance();
		try{
			for(Entry<String,Boolean> entry : features.entrySet()){
				dbFactory.setFeature(entry.getKey(), entry.getValue());
			}
			return dbFactory.newDocumentBuilder();
		}catch(ParserConfigurationException e){
			throw new RuntimeException(e);
		}
	}

	public static <T> T getFromDocument(DocumentBuilder documentBuilder, File source,
			Function<Document,T> documentInspector){
		try{
			Document doc = documentBuilder.parse(source);
			return documentInspector.apply(doc);
		}catch(SAXException | IOException e){
			throw new RuntimeException(e);
		}
	}

	public static Scanner<Node> scan(NodeList nodeList){
		return Scanner.iterate(0, i -> i + 1)
				.map(nodeList::item)
				.limit(nodeList.getLength());
	}

}
