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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.exception.dto.ExceptionRecordBlobDto.ExceptionRecordBlobItemDto;
import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;

public class ExceptionRecordsSplittingStringBuildersIntegrationTests{

	private static final ExceptionRecordBlobItemDto EMPTY = new ExceptionRecordBlobItemDto(
			new ExceptionRecordDto(null, null, null, null, null, null, null, null, null, null, null, null, null));
	private static final String EMPTY_JSON = GsonTool.JAVA9_GSON.toJson(EMPTY);
	private static final int EMPTY_LENGTH = EMPTY_JSON.length();

	@Test
	public void testTooSmallForAnything(){
		//not long enough for any item, so everything should be discarded
		var builder = new ExceptionRecordBlobDto.ExceptionRecordsSplittingStringBuilders(EMPTY_LENGTH - 1);

		//too small for anything
		builder.append(EMPTY);
		builder.append(EMPTY);
		var list = builder.scanSplitItems().list();
		Assert.assertEquals(list.size(), 0);
	}

	@Test
	public void testFileSplitting(){
		//these get split, because there's no room for \n
		var builder = new ExceptionRecordBlobDto.ExceptionRecordsSplittingStringBuilders(EMPTY_LENGTH * 2);
		builder.append(EMPTY);
		builder.append(EMPTY);

		var list = builder.scanSplitItems().list();
		Assert.assertEquals(list.size(), 2);
		Assert.assertEquals(list.get(0), EMPTY_JSON);
		Assert.assertEquals(list.get(1), EMPTY_JSON);

		//these don't get split, because there's room for the \n
		builder = new ExceptionRecordBlobDto.ExceptionRecordsSplittingStringBuilders(EMPTY_LENGTH * 2 + 1);
		builder.append(EMPTY);
		builder.append(EMPTY);

		list = builder.scanSplitItems().list();
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0), EMPTY_JSON + "\n" + EMPTY_JSON);
	}

	@Test
	public void testNormalSizeAppending(){
		var builder = new ExceptionRecordBlobDto.ExceptionRecordsSplittingStringBuilders(256 * 1024 - 30);

		builder.append(EMPTY);
		builder.append(EMPTY);
		builder.append(EMPTY);
		builder.append(EMPTY);
		builder.append(EMPTY);
		builder.append(EMPTY);

		var list = builder.scanSplitItems().list();
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0), (EMPTY_JSON + "\n").repeat(5) + EMPTY_JSON);
	}

}
