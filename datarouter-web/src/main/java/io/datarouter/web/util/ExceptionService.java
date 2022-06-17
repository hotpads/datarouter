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
package io.datarouter.web.util;

import static j2html.TagCreator.span;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.string.XmlStringTool;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import j2html.tags.specialized.SpanTag;

@Singleton
public class ExceptionService{
	private static final Logger logger = LoggerFactory.getLogger(ExceptionService.class);

	@Inject
	private DatarouterWebSettingRoot datarouterWebSettingRoot;

	public String getStackTraceStringForHtmlPreBlock(Throwable exception){
		String stackTrace = exception != null
				? ExceptionTool.getStackTraceAsString(exception)
				: "No exception defined.";
		return getColorized(stackTrace);
	}

	public String getColorized(String stackTrace){
		if(stackTrace == null){
			return null;
		}
		stackTrace = XmlStringTool.escapeXmlKeepSpecialChar(stackTrace);
		for(String highlight : datarouterWebSettingRoot.stackTraceHighlights.get()){
			SpanTag tag = span(highlight)
					.withStyle("color:red; font-weight:bold; font-size:1.5em;");
			stackTrace = stackTrace.replace(highlight, tag.render());
		}
		return stackTrace;
	}

	public String getShortStackTrace(String fullStackTrace){
		if(fullStackTrace == null){
			return null;
		}
		BufferedReader br = new BufferedReader(new StringReader(fullStackTrace));
		String line;
		boolean none = false;
		int nb = 0;
		StringBuilder builder = new StringBuilder();
		try{
			while((line = br.readLine()) != null){
				boolean lineContainsHighlight = datarouterWebSettingRoot.stackTraceHighlights.get().stream()
						.anyMatch(line::contains);
				if(lineContainsHighlight && nb < 10){
					none = false;
					nb++;
					builder.append(line);
					builder.append('\n');
				}else{
					if(!none){
						builder.append("[...]");
						builder.append('\n');
						none = true;
					}
				}
			}
			return getColorized(builder.toString());
		}catch(IOException e){
			logger.warn("Error building short stack trace", e);
			return fullStackTrace;
		}
	}

}
