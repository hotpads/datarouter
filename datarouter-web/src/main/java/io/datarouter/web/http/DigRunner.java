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
package io.datarouter.web.http;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.util.DatarouterRuntimeTool;
import io.datarouter.util.RunNativeDto;
import io.datarouter.util.string.StringTool;

public class DigRunner{

	public static RunNativeDto lookup(String name){
		return DatarouterRuntimeTool.runNative("dig", name);
	}

	public static List<DnsAnswer> parse(String stdout){
		List<String> lines = StringTool.splitOnCharNoRegex(stdout, '\n');
		boolean answerStarted = false;
		List<DnsAnswer> answers = new ArrayList<>();
		for(String line : lines){
			if(";; ANSWER SECTION:".equals(line)){
				answerStarted = true;
				continue;
			}
			if(answerStarted && "".equals(line)){
				break;
			}
			if(answerStarted){
				answers.add(parseAnswer(line));
			}
		}
		return answers;
	}

	public static DnsAnswer parseAnswer(String line){
		List<String> parts = StringTool.splitOnCharNoRegex(line.replace('\t', ' '), ' ', false);
		try{
			return new DnsAnswer(parts.get(0), parts.get(1), parts.get(2), parts.get(3), parts.get(4));
		}catch(Exception e){
			throw new RuntimeException("failed to parse line=" + line, e);
		}
	}

}
