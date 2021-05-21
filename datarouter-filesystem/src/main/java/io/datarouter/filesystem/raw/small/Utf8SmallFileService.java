/**
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
package io.datarouter.filesystem.raw.small;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Utf8SmallFileService{

	@Inject
	private CheckedUtf8FileService checkedService;

	public void writeUtf8(Path fullPath, String contents){
		try{
			checkedService.writeUtf8(fullPath, contents);
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
	}

	public String readUtf8(Path fullPath){
		try{
			return checkedService.readUtf8(fullPath);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Singleton
	public static class CheckedUtf8FileService{

		@Inject
		private BinaryFileService binaryFileService;

		public void writeUtf8(Path fullPath, String contents) throws UnsupportedEncodingException{
			binaryFileService.writeBytes(fullPath, contents.getBytes(StandardCharsets.UTF_8.name()));
		}

		public String readUtf8(Path fullPath)
		throws IOException{
			byte[] bytes = Files.readAllBytes(fullPath);
			return new String(bytes, StandardCharsets.UTF_8);
		}

	}

}
