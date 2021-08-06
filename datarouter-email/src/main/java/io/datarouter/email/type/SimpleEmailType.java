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
package io.datarouter.email.type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.datarouter.scanner.Scanner;

public abstract class SimpleEmailType{

	public final List<String> tos;

	public SimpleEmailType(List<String> tos){
		this.tos = tos;
	}

	public String getAsCsv(String...additionalTos){
		return getAsCsv(Scanner.of(additionalTos).list());
	}

	public String getAsCsv(List<String> additionalTos){
		Set<String> toEmails = new HashSet<>();
		toEmails.addAll(tos);
		toEmails.addAll(additionalTos);
		return String.join(",", toEmails);
	}

}
