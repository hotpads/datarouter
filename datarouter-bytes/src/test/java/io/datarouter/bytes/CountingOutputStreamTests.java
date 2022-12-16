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
package io.datarouter.bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CountingOutputStreamTests{

	@Test
	public void testCountOfCountingOutputStream(){
		byte[] output = {0, 1, 2, 3, 4, 5};
		var outputStream = new ByteArrayOutputStream();
		var cout = new CountingOutputStream(outputStream);
		try(cout){
			cout.write(output);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Assert.assertEquals(cout.getCount(), output.length);
	}

	@Test
	public void testWithCallback(){
		byte[] output = {1, 2, 3, 4};
		var outputStream = new ByteArrayOutputStream();
		var totalBytes = new AtomicLong();
		Consumer<Long> countCallback = totalBytes::addAndGet;
		try(var cout = new CountingOutputStream(outputStream, 3, countCallback)){
			OutputStreamTool.write(cout, output);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Assert.assertEquals(totalBytes.get(), output.length);
	}

	@Test
	public void testWrite(){
		byte[] output = {1, 2, 3, 4, 5, 6};
		var countOfBytes = new AtomicLong();
		Consumer<Long> countCallback = countOfBytes::addAndGet;
		var outputStream = new ByteArrayOutputStream();
		int offset = 2;
		try(var cout = new CountingOutputStream(outputStream, 3, countCallback)){
			cout.write(output, offset, output.length - offset);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Assert.assertEquals(countOfBytes.get(), output.length - offset);
	}

}
