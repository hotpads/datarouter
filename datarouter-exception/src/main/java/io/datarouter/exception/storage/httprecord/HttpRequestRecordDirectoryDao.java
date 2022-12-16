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
package io.datarouter.exception.storage.httprecord;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.bytes.ExtractFromPrependedLengthByteArrayScanner;
import io.datarouter.bytes.PrependLengthByteArrayScanner;
import io.datarouter.exception.dto.HttpRequestRecordBinaryDto;
import io.datarouter.exception.storage.exceptionrecord.HttpRequestRecordDirectorySupplier;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;
import io.datarouter.types.Ulid;

@Singleton
public class HttpRequestRecordDirectoryDao{

	@Inject
	private HttpRequestRecordDirectorySupplier directory;

	public Scanner<HttpRequestRecordBinaryDto> read(String filename){
		return ExtractFromPrependedLengthByteArrayScanner.of(
				directory.getHttpRequestRecordDirectory().read(PathbeanKey.of(filename)))
				.map(HttpRequestRecordBinaryDto::decode);
	}

	public void write(Scanner<HttpRequestRecordBinaryDto> dtos, Ulid ulid){
		PathbeanKey key = PathbeanKey.of(ulid.value());
		dtos
				.map(HttpRequestRecordBinaryDto::encodeIndexed)
				.apply(PrependLengthByteArrayScanner::of)
				.then(scanner -> directory.getHttpRequestRecordDirectory().write(key, scanner));
	}

	public Scanner<String> scanKeysAllowUnsorted(){
		return directory.getHttpRequestRecordDirectory().scanKeys(
				Subpath.empty(),
				new Config().setAllowUnsortedScan(true))
				.map(PathbeanKey::getPathAndFile);
	}

	public void delete(String filename){
		directory.getHttpRequestRecordDirectory().delete(PathbeanKey.of(filename));
	}

	public void deleteAll(){
		directory.getHttpRequestRecordDirectory().deleteAll(Subpath.empty());
	}

}
