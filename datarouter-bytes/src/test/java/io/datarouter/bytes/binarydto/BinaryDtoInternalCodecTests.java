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
package io.datarouter.bytes.binarydto;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.binarydto.dto.ComparableBinaryDto;
import io.datarouter.bytes.codec.bytestringcodec.CsvIntByteStringCodec;

public class BinaryDtoInternalCodecTests{

	public static class TestDto extends ComparableBinaryDto<TestDto>{
		public final int f1;
		public final int f2;

		public TestDto(int f1, int f2){
			this.f1 = f1;
			this.f2 = f2;
		}
	}

	@Test
	public void testEncoding(){
		var dto = new TestDto(5, 6);
		Assert.assertEquals(dto.cloneIndexed(), dto);
		Assert.assertEquals(dto.cloneComparable(), dto);
	}

	@Test
	public void testIndexedEncodingCsvInt(){
		var dto = new TestDto(5, 6);
		byte[] bytes = dto.encodeIndexed();
		String actual = CsvIntByteStringCodec.INSTANCE.encode(bytes);
		String expected = "0,4,128,0,0,5"//index=0, length=4, item 0
				+ ",1,4,128,0,0,6";//index=1, length=4, item 1
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testComparableEncodingCsvInt(){
		var dto = new TestDto(5, 6);
		byte[] bytes = dto.encodeComparable();
		String actual = CsvIntByteStringCodec.INSTANCE.encode(bytes);
		String expected = "128,0,0,5"//item 0
				+ ",128,0,0,6";//item 1
		Assert.assertEquals(actual, expected);
	}

}
