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
package io.datarouter.metric.gauge;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.metric.dto.GaugeBinaryDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.Ulid;

@Singleton
public class GaugeDirectoryDao{

	@Inject
	private GaugeDirectorySupplier directory;

	public GaugeBinaryDto read(String filename){
		return GaugeBinaryDto.decode(directory.getGaugeDirectory().read(PathbeanKey.of(filename)));
	}

	public void write(GaugeBinaryDto dto, Ulid ulid){
		PathbeanKey key = PathbeanKey.of(ulid.value());
		directory.getGaugeDirectory().write(key, dto.encodeIndexed());
	}

	public Scanner<String> scanKeysAllowUnsorted(){
		return directory.getGaugeDirectory().scanKeys(Subpath.empty(), new Config().setAllowUnsortedScan(true))
				.map(PathbeanKey::getPathAndFile);
	}

	public void delete(String filename){
		directory.getGaugeDirectory().delete(PathbeanKey.of(filename));
	}

	public void deleteAll(){
		directory.getGaugeDirectory().deleteAll(Subpath.empty());
	}

}
