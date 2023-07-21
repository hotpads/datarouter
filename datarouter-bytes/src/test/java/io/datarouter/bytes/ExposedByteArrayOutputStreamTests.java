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

import java.io.IOException;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.io.ExposedByteArrayOutputStream;

public class ExposedByteArrayOutputStreamTests{

	@Test
	public void testNormalBehavior() throws IOException{
		var os = new ExposedByteArrayOutputStream();
		os.write(new byte[]{1, 2});
		os.write(new byte[]{3, 4});
		InputStream is = os.toInputStream();
		byte[] output = is.readAllBytes();
		Assert.assertEquals(output, new byte[]{1, 2, 3, 4});
	}

	@Test
	public void testImproperUsage() throws IOException{
		var os = new ExposedByteArrayOutputStream();
		os.write(new byte[]{1, 2, 3});
		InputStream is = os.toInputStream();
		os.reset();// even though we reset(), the data is still in the exposed buffer
		os.write(9);// overwriting the buffer before the InputStream reads it
		byte[] output = is.readAllBytes();
		Assert.assertEquals(output, new byte[]{9, 2, 3});
	}

}
