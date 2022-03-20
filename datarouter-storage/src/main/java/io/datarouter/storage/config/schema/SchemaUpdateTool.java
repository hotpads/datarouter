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
package io.datarouter.storage.config.schema;

import org.slf4j.Logger;

import io.datarouter.gson.serialization.GsonTool;

public class SchemaUpdateTool{

	private static final int CONSOLE_WIDTH = 80;

	public static final String SINGLE_LINE_SCHEMA_UPDATE_PREFIX = "SchemaUpdate: ";

	private static final String
			PLEASE_EXECUTE_SCHEMA_UPDATE_MESSAGE = generateFullWidthMessage("Please Execute SchemaUpdate"),
			THANK_YOU_MESSAGE = generateFullWidthMessage("Thank You");

	public static void printSchemaUpdate(Logger logger, String schemaUpdate){
		logger.warn(SINGLE_LINE_SCHEMA_UPDATE_PREFIX + GsonTool.GSON.toJson(schemaUpdate));
		logger.warn(PLEASE_EXECUTE_SCHEMA_UPDATE_MESSAGE);
		logger.warn(schemaUpdate);
		logger.warn(THANK_YOU_MESSAGE);
	}

	public static String generateFullWidthMessage(String message){
		var fullWidthMessage = new StringBuilder();
		int numCharsOnSide = (CONSOLE_WIDTH - message.length()) / 2 - 1;
		if(numCharsOnSide <= 0){
			return message;
		}
		int chars;
		for(chars = 0; chars < numCharsOnSide; chars++){
			fullWidthMessage.append("=");
		}
		fullWidthMessage.append(" ").append(message).append(" ");
		chars += message.length();
		for(; chars < CONSOLE_WIDTH; chars++){
			fullWidthMessage.append("=");
		}
		return fullWidthMessage.toString();
	}

}
