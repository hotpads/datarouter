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
package io.datarouter.aws.s3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import io.datarouter.util.Require;

public class S3Tool{

	public static final String S3_ENDPOINT = "https://s3.amazonaws.com";

	public static final Pattern S3_KEY_NON_SAFE_CHARACTERS = Pattern.compile("[^0-9a-zA-Z\\!\\-\\_\\.\\*\\'\\(\\)]");

	public static String replaceS3KeyNonSafeCharacters(String input, String defaultReplacement){
		if(input == null){
			return input;
		}
		Require.isFalse(S3_KEY_NON_SAFE_CHARACTERS.matcher(defaultReplacement).find());
		return S3_KEY_NON_SAFE_CHARACTERS.matcher(input).replaceAll(defaultReplacement);
	}

	public static String makeGetPartialObjectRangeParam(long offset, int length){
		long startInclusive = offset;
		long endInclusive = offset + length - 1;
		// https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35
		// https://github.com/aws/aws-sdk-java-v2/issues/1472
		return "bytes=" + startInclusive + "-" + endInclusive;
	}

	public static void prepareLocalFileDestination(Path path){
		try{
			Files.createDirectories(path.getParent());
			Files.deleteIfExists(path);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}
