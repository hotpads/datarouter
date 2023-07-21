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
package io.datarouter.bytes.blockfile.checksum;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.scanner.Scanner;

public class BlockfileChecksummers{

	public final List<BlockfileChecksummer> all;

	public BlockfileChecksummers(List<BlockfileChecksummer> all){
		this.all = new ArrayList<>(all);
	}

	public BlockfileChecksummers add(BlockfileChecksummer checksummer){
		all.add(checksummer);
		return this;
	}

	public BlockfileChecksummer getForEncodedName(String encodedName){
		return Scanner.of(all)
				.include(checksummer -> checksummer.encodedName().equals(encodedName))
				.findFirst()
				.orElseThrow(() -> {
					String registeredNames = all.stream()
							.map(BlockfileChecksummer::encodedName)
							.collect(Collectors.joining(",", "[", "]"));
					String message = String.format(
							"checksummer with name=%s is not registered.  registeredChecksummers=%s",
							encodedName,
							registeredNames);
					throw new IllegalArgumentException(message);
				});
	}


}
