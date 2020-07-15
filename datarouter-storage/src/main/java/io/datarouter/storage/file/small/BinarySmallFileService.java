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
package io.datarouter.storage.file.small;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BinarySmallFileService{

	@Inject
	private CheckedSmallBinaryFileService checkedService;

	public void writeBytes(Path fullPath, byte[] contents){
		try{
			checkedService.writeBytes(fullPath, contents);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public byte[] readBytes(Path fullPath){
		try{
			return checkedService.readBytes(fullPath);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Singleton
	public static class CheckedSmallBinaryFileService{

		public void writeBytes(Path fullPath, byte[] contents)
		throws IOException{
			Files.write(fullPath, contents);
		}

		public byte[] readBytes(Path fullPath)
		throws IOException{
			return Files.readAllBytes(fullPath);
		}

	}
}
