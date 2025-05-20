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

import org.testng.Assert;
import org.testng.annotations.Test;

public class ByteReaderTests{

	@Test
	public void testComparableDouble(){
		var writer = new ByteWriter(100);
		writer.bytes(new byte[]{9, 9});//filler
		writer.comparableDouble(1.1);
		writer.comparableDouble(2.2);
		writer.comparableDouble(3.3);

		var reader1 = new ByteReader(writer.concat());
		Assert.assertEquals(reader1.bytes(2), new byte[]{9, 9});
		Assert.assertEquals(reader1.comparableDouble(), 1.1);
		Assert.assertEquals(reader1.comparableDouble(), 2.2);
		Assert.assertEquals(reader1.comparableDouble(), 3.3);
		reader1.assertFinished();

		var reader2 = new ByteReader(writer.concat());
		reader2.skip(2);
		Assert.assertEquals(reader2.comparableDoubles(3), new double[]{1.1, 2.2, 3.3});
		reader2.assertFinished();
	}

	@Test
	public void testComparableUtf8(){
		var writer = new ByteWriter(100);
		writer.rawInt(3);//filler
		writer.comparableUtf8("hello world");
		writer.comparableUtf8("hi");
		writer.comparableLong(55);//filler
		var reader = new ByteReader(writer.concat());
		Assert.assertEquals(reader.rawInt(), 3);
		Assert.assertEquals(reader.comparableUtf8(), "hello world");
		Assert.assertEquals(reader.comparableUtf8(), "hi");
		Assert.assertEquals(reader.comparableLong(), 55);
		reader.assertFinished();
	}

}
