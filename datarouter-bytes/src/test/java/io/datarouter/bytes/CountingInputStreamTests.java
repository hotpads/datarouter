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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.io.CountingInputStream;
import io.datarouter.bytes.io.InputStreamTool;

public class CountingInputStreamTests{

	@Test
	public void testCountOfCountingInputStream(){
		byte[] input = {0, 1, 2, 3, 4, 5};
		var inputStream = new ByteArrayInputStream(input);
		var cin = new CountingInputStream(inputStream);
		try(cin){
			cin.read(new byte[input.length]);
			cin.read(new byte[input.length]);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Assert.assertEquals(cin.getCount(), input.length);
	}

	@Test
	public void testCountWhileSkipping(){
		byte[] input = {0, 1, 2, 3, 4, 5};
		var inputStream = new ByteArrayInputStream(input);
		var cin = new CountingInputStream(inputStream);
		try(cin){
			cin.skip(2);
			cin.read();
			cin.skip(1);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Assert.assertEquals(cin.getCount(), 4);
	}

	@Test
	public void testWithCallback(){
		byte[] input = {1, 2, 3, 4};
		var inputStream = new ByteArrayInputStream(input);
		var totalBytes = new AtomicLong();
		Consumer<Long> countCallback = totalBytes::addAndGet;
		var cin = new CountingInputStream(inputStream, 3, countCallback);
		InputStreamTool.readNBytes(cin, 100);
		Assert.assertEquals(totalBytes.get(), input.length);
	}

	@Test
	public void testRead(){
		byte[] input = {1, 2, 3, 4};
		var countOfBytes = new AtomicLong();
		Consumer<Long> countCallback = countOfBytes::addAndGet;
		var inputStream = new ByteArrayInputStream(input);
		try(var cinput = new CountingInputStream(inputStream, 3, countCallback)){
			cinput.read();
			cinput.read();
			cinput.read();
			cinput.read();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Assert.assertEquals(countOfBytes.get(), input.length);
	}

	@Test
	public void testSkip(){
		byte[] input = {1, 2, 3, 4};
		var countOfBytes = new AtomicLong();
		Consumer<Long> countCallback = countOfBytes::addAndGet;
		var inputStream = new ByteArrayInputStream(input);
		try(var cinput = new CountingInputStream(inputStream, 3, countCallback)){
			cinput.skip(4);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Assert.assertEquals(countOfBytes.get(), input.length);
	}

}
