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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BinaryDto;
import io.datarouter.bytes.binarydto.dto.BinaryDtoField;
import io.datarouter.bytes.binarydto.fieldcodec.array.SignedByteArrayBinaryDtoFieldCodec;
import io.datarouter.scanner.Scanner;

public class BinaryDtoSignedByteArrayTests{

	public static class EncodeTestDto extends BinaryDto<EncodeTestDto>{
		@BinaryDtoField(codec = SignedByteArrayBinaryDtoFieldCodec.class)
		public final byte[] f1;
		@BinaryDtoField(codec = SignedByteArrayBinaryDtoFieldCodec.class)
		public final byte[] f2;
		@BinaryDtoField(codec = SignedByteArrayBinaryDtoFieldCodec.class)
		public final byte[] f3;

		public EncodeTestDto(byte[] f1, byte[] f2, byte[] f3){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
		}
	}

	@Test
	public void testEncoding(){
		var codec = BinaryDtoCodec.of(EncodeTestDto.class);
		var dto = new EncodeTestDto(
				new byte[]{'M', 'a', 't', 't'},
				null,
				new byte[]{});
		byte[] expectedBytes = {
				1,//f1 present
				4,//f1 length 4
				ByteTool.getComparableByte((byte)77),//M
				ByteTool.getComparableByte((byte)97),//a
				ByteTool.getComparableByte((byte)116),//t
				ByteTool.getComparableByte((byte)116),//t
				0,//f2 null
				1,//f3 present
				0};//f3 length 0
		byte[] actualBytes = codec.encode(dto);
		Assert.assertEquals(actualBytes, expectedBytes);

		EncodeTestDto actual = codec.decode(actualBytes);
		Assert.assertEquals(actual, dto);
	}


	public static class CompareTestDto extends BinaryDto<EncodeTestDto>{
		@BinaryDtoField(codec = SignedByteArrayBinaryDtoFieldCodec.class)
		public final byte[] f1;

		public CompareTestDto(byte[] f1){
			this.f1 = f1;
		}
	}

	@Test
	public void testSorting(){
		var dto1 = new CompareTestDto(new byte[]{-1, 0});
		var dto2 = new CompareTestDto(new byte[]{-2});
		var dto3 = new CompareTestDto(new byte[]{1});
		var dto4 = new CompareTestDto(new byte[]{1, 0});
		List<CompareTestDto> unsorted = List.of(dto1, dto2, dto3, dto4);
		List<CompareTestDto> expected = List.of(dto2, dto3, dto1, dto4);
		List<CompareTestDto> actual = Scanner.of(unsorted).sort().list();
		Assert.assertEquals(actual, expected);
	}

}
