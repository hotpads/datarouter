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

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.metric.dto.GaugeBlobDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;

@Singleton
public class GaugeBlobDirectoryDao{

	@Inject
	private GaugeBlobDirectorySupplier directory;

	public String read(String filename){
		return StringCodec.UTF_8.decode(directory.getGaugeBlobDirectory().read(PathbeanKey.of(filename)));
	}

	public void write(GaugeBlobDto countBlobDto, String ulid){
		PathbeanKey key = PathbeanKey.of(ulid);
		directory.getGaugeBlobDirectory().write(key, countBlobDto.serializeToBytes());
	}

	public Scanner<String> scanKeys(){
		return directory.getGaugeBlobDirectory().scanKeys(Subpath.empty())
				.map(PathbeanKey::getPathAndFile);
	}

	public void delete(String filename){
		directory.getGaugeBlobDirectory().delete(PathbeanKey.of(filename));
	}

	public void deleteAll(){
		directory.getGaugeBlobDirectory().deleteAll(Subpath.empty());
	}

}
