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
package io.datarouter.exception.dto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.exception.dto.ExceptionRecordBlobDto.ExceptionRecordBlobItemDto;
import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;

public class ExceptionRecordsSplittingStringBuildersV2IntegrationTests{//TODO make one for v2...

	static final ExceptionRecordBlobItemDto EMPTY = new ExceptionRecordBlobItemDto(
			new ExceptionRecordDto(null, null, null, null, null, null, null, null, null, null, null, null, null));
	static final String EMPTY_ENCODED = toBase64ByteString(GsonTool.GSON.toJson(EMPTY));
	static final int EMPTY_LENGTH = EMPTY_ENCODED.length();

	@Test
	public void testTooSmallForAnything(){
		//not long enough for any item, so everything should be discarded
		var builder = new ExceptionRecordBlobDto.ExceptionRecordsSplittingStringBuildersV2(EMPTY_LENGTH - 1);

		//too small for anything
		builder.append(EMPTY);
		builder.append(EMPTY);
		var list = builder.scanSplitItems().list();
		Assert.assertEquals(list.size(), 0);
	}

	@Test
	public void testFileSplitting(){
		//these get split, because there's no room for \n
		var builder = new ExceptionRecordBlobDto.ExceptionRecordsSplittingStringBuildersV2(EMPTY_LENGTH * 2);
		builder.append(EMPTY);
		builder.append(EMPTY);

		var list = builder.scanSplitItems().list();
		Assert.assertEquals(list.size(), 2);
		Assert.assertEquals(list.get(0), EMPTY_ENCODED);
		Assert.assertEquals(list.get(1), EMPTY_ENCODED);

		//these don't get split, because there's room for the \n
		builder = new ExceptionRecordBlobDto.ExceptionRecordsSplittingStringBuildersV2(EMPTY_LENGTH * 2 + 1);
		builder.append(EMPTY);
		builder.append(EMPTY);

		list = builder.scanSplitItems().list();
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0), EMPTY_ENCODED + "," + EMPTY_ENCODED);
	}

	@Test
	public void testNormalSizeAppending(){
		var builder = new ExceptionRecordBlobDto.ExceptionRecordsSplittingStringBuildersV2(256 * 1024 - 30);

		builder.append(EMPTY);
		builder.append(EMPTY);
		builder.append(EMPTY);
		builder.append(EMPTY);
		builder.append(EMPTY);
		builder.append(EMPTY);

		var list = builder.scanSplitItems().list();
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0), (EMPTY_ENCODED + ",").repeat(5) + EMPTY_ENCODED);
	}

	private static String toBase64ByteString(String string){
		return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
	}

}
