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
package io.datarouter.exception.storage;

import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.bytes.VarIntByteArraysTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;
import io.datarouter.types.Ulid;

public abstract class BaseRecordDirectoryDao<E extends BinaryDto<?>>{

	protected abstract Directory getDirectory();
	protected abstract E decode(byte[] bytes);

	public Scanner<E> read(String filename){
		return VarIntByteArraysTool.decodeMulti(getDirectory().read(PathbeanKey.of(filename)))
				.map(this::decode);
	}

	public void write(Scanner<E> dtos, Ulid ulid){
		PathbeanKey key = PathbeanKey.of(ulid.value());
		dtos
				.map(E::encodeIndexed)
				.map(VarIntByteArraysTool::encodeOne)
				.then(scanner -> getDirectory().write(key, scanner));
	}

	public Scanner<String> scanKeysAllowUnsorted(){
		return getDirectory().scanKeys(
				Subpath.empty(),
				new Config().setAllowUnsortedScan(true))
				.map(PathbeanKey::getPathAndFile);
	}

	public void delete(String filename){
		getDirectory().delete(PathbeanKey.of(filename));
	}

	public void deleteAll(){
		getDirectory().deleteAll(Subpath.empty());
	}

}
