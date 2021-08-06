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
package io.datarouter.util.process;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RunNativeInputStreamReaderTests{

	@Test
	public void test(){
		InputStream emptyInputStream = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
		RunNativeInputStreamReader reader = new RunNativeInputStreamReader(emptyInputStream);
		Assert.assertTrue(reader.lines().isEmpty());
	}

	@Test
	public void returnsLines(){
		RunNativeInputStreamReader reader = new RunNativeInputStreamReader(new ByteArrayInputStream(
				"Line one\nLine two".getBytes()));
		Assert.assertEquals(reader.linesWithReplacement().list(), Arrays.asList("Line one", "Line two"));
	}

	@Test
	public void returnsLineWithCarriageReturn(){
		RunNativeInputStreamReader reader = new RunNativeInputStreamReader(new ByteArrayInputStream(
				"Line one\rLine two\r".getBytes()));
		Assert.assertEquals(reader.linesWithReplacement().list(), Arrays.asList("Line two\r"));
	}

	@Test
	public void returnsLineWithCarriageReturnAndNewLine(){
		RunNativeInputStreamReader reader = new RunNativeInputStreamReader(new ByteArrayInputStream(
				"Line one\r\nLine two\r\n".getBytes()));
		Assert.assertEquals(reader.linesWithReplacement().list(), Arrays.asList("Line one", "Line two"));
	}

}
