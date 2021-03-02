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
package io.datarouter.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MultiByteArrayInputStreamTests{

	@Test
	public void testEmpty(){
		MultiByteArrayInputStream is = new MultiByteArrayInputStream(List.of(new byte[0]));
		Assert.assertEquals(is.readAllBytes(), new byte[0]);
	}

	@Test
	public void testOther() throws IOException{
		List<byte[]> inputs = List.of(new byte[]{}, new byte[]{0}, new byte[]{1, 2}, new byte[]{});

		try(MultiByteArrayInputStream is1 = new MultiByteArrayInputStream(inputs)){
			Assert.assertEquals(0, is1.read());
			Assert.assertEquals(1, is1.read());
			Assert.assertEquals(2, is1.read());
			Assert.assertEquals(-1, is1.read());
		}

		try(MultiByteArrayInputStream is2 = new MultiByteArrayInputStream(inputs)){
			byte[] buffer = new byte[2];
			Assert.assertEquals(is2.read(buffer), 2);
			Assert.assertEquals(buffer, new byte[]{0, 1});
			Assert.assertEquals(is2.read(buffer), 1);
			Assert.assertEquals(buffer, new byte[]{2, 1});// 1 is leftover
		}

		try(MultiByteArrayInputStream is3 = new MultiByteArrayInputStream(inputs)){
			Assert.assertEquals(is3.readAllBytes(), new byte[]{0, 1, 2});
		}

		try(MultiByteArrayInputStream is4 = new MultiByteArrayInputStream(inputs)){
			Assert.assertEquals(is4.readNBytes(1), new byte[]{0});
			Assert.assertEquals(is4.readNBytes(2), new byte[]{1, 2});
			Assert.assertEquals(is4.readNBytes(2), new byte[]{});
		}

		try(MultiByteArrayInputStream is5 = new MultiByteArrayInputStream(inputs)){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			long count = is5.transferTo(baos);
			Assert.assertEquals(count, 3);
			Assert.assertEquals(baos.toByteArray(), new byte[]{0, 1, 2});
		}
	}
}
