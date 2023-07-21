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
package io.datarouter.binarydto.encoding;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.dto.ComparableBinaryDto;
import io.datarouter.bytes.codec.bytestringcodec.CsvIntByteStringCodec;

public class BinaryDtoIntArrayTests{

	private static class TestDto extends ComparableBinaryDto<TestDto>{
		public final int[] f1;
		public final int[] f2;
		public final int[] f3;

		public TestDto(int[] f1, int[] f2, int[] f3){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
		}
	}

	@Test
	public void testEncoding(){
		var dto = new TestDto(
				new int[]{5, 6},
				null,
				new int[]{});
		Assert.assertEquals(dto.cloneIndexed(), dto);
		Assert.assertEquals(dto.cloneComparable(), dto);
	}

	@Test
	public void testIndexedEncodingCsvInt(){
		var dto = new TestDto(
				new int[]{5, 6},
				null,
				new int[]{});
		byte[] bytes = dto.encodeIndexed();
		String actual = CsvIntByteStringCodec.INSTANCE.encode(bytes);
		String expected = "0,8,128,0,0,5,128,0,0,6"//index=0, length=8, data
				+ ",2,0";//index=2, length=0
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testComparableEncodingCsvInt(){
		var dto = new TestDto(
				new int[]{5, 6},
				null,
				new int[]{});
		byte[] bytes = dto.encodeComparable();
		String actual = CsvIntByteStringCodec.INSTANCE.encode(bytes);
		String expected = "1,128,1,2,1,2,5,128,1,2,1,2,6,0"//nonnull, escaped data, terminator
				+ ",0"//null
				+ ",1,0";//nonnull, (no data), terminator
		Assert.assertEquals(actual, expected);
	}

}
