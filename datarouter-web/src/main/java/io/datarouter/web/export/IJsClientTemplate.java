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

package io.datarouter.web.export;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;

import io.datarouter.scanner.Scanner;

// temp name. Don't prefix with "I"
public interface IJsClientTemplate{

	Path getOutputPath();
	String getOutputDirectory();
	String buildTemplate();
	String getTsDirectory();
	String getFileName();

	static String generateGetParams(Map<String,Map<String,String>> getParams){
		var builder = new StringBuilder();
		if(!getParams.isEmpty()){
			Scanner.of(getParams.entrySet())
					.sort(Comparator.comparing(a -> a.getKey().toLowerCase()))
					.forEach(entry -> {
						builder.append("interface " + entry.getKey() + " {\n");
						entry.getValue().forEach((key, value) -> {
							builder.append("  " + key + ": " + value + ",\n");
						});
						builder.append("}\n");
					});
			builder.append("\n");
		}
		return builder.toString();
	}

}
