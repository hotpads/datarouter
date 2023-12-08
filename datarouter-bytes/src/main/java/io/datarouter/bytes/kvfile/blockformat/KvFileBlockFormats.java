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
package io.datarouter.bytes.kvfile.blockformat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.scanner.Scanner;

public class KvFileBlockFormats{

	public final List<KvFileBlockFormat> all;

	public KvFileBlockFormats(){
		this.all = new ArrayList<>(KvFileStandardBlockFormats.ALL);
	}

	public KvFileBlockFormats add(KvFileBlockFormat format){
		all.add(format);
		return this;
	}

	public KvFileBlockFormat getForEncodedName(String encodedName){
		return Scanner.of(all)
				.include(format -> format.encodedName().equals(encodedName))
				.findFirst()
				.orElseThrow(() -> {
					String registeredNames = all.stream()
							.map(KvFileBlockFormat::encodedName)
							.collect(Collectors.joining(",", "[", "]"));
					String message = String.format(
							"%s with name=%s is not registered.  registeredNames=%s",
							KvFileBlockFormat.class.getSimpleName(),
							encodedName,
							registeredNames);
					throw new IllegalArgumentException(message);
				});
	}
}
