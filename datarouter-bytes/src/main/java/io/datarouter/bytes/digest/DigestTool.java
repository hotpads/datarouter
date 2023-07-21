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
package io.datarouter.bytes.digest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import io.datarouter.bytes.io.InputStreamTool;

public class DigestTool{

	public static Digest digest(String input, DigestAlgorithm hashFunction){
		return new Digest(hashFunction.getMessageDigest().digest(input.getBytes()));
	}

	public static Digest digest(Path path, DigestAlgorithm hashFunction){
		try(DigestStream stream = startDigestStream(path, hashFunction)){
			InputStreamTool.transferTo(stream.getStream(), OutputStream.nullOutputStream());
			return stream.digest();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public static DigestStream startDigestStream(Path path, DigestAlgorithm hashFunction){
		MessageDigest instance = hashFunction.getMessageDigest();
		try{
			InputStream is = Files.newInputStream(path);
			var dis = new DigestInputStream(is, instance);
			return new DigestStream(dis);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}
