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
package io.datarouter.metric.counter;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;

@Singleton
public class CountDirectoryDao{

	@Inject
	private CountDirectorySupplier directory;

	public CountBinaryDto read(String filename){
		return CountBinaryDto.decode(directory.getCountsBinaryDtoDirectory().read(PathbeanKey.of(filename)));
	}

	public void write(CountBinaryDto dto, String ulid){
		PathbeanKey key = PathbeanKey.of(ulid);
		directory.getCountsBinaryDtoDirectory().write(key, dto.encodeIndexed());
	}

	public Scanner<String> scanKeysAllowUnsorted(){
		return directory.getCountsBinaryDtoDirectory().scanKeys(
				Subpath.empty(), new Config().setAllowUnsortedScan(true))
				.map(PathbeanKey::getPathAndFile);
	}

	public void delete(String filename){
		directory.getCountsBinaryDtoDirectory().delete(PathbeanKey.of(filename));
	}

	public void deleteAll(){
		directory.getCountsBinaryDtoDirectory().deleteAll(Subpath.empty());
	}

}
