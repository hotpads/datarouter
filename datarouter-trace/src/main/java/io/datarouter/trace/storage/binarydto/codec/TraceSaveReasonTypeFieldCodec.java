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
package io.datarouter.trace.storage.binarydto.codec;

import io.datarouter.binarydto.fieldcodec.BinaryDtoConvertingFieldCodec;
import io.datarouter.binarydto.fieldcodec.string.Utf8BinaryDtoFieldCodec;
import io.datarouter.enums.StringMappedEnum;
import io.datarouter.instrumentation.trace.TraceSaveReasonType;

public class TraceSaveReasonTypeFieldCodec
extends BinaryDtoConvertingFieldCodec<TraceSaveReasonType,String>{

	public static final StringMappedEnum<TraceSaveReasonType> BY_TYPE_STRING
			= new StringMappedEnum<>(TraceSaveReasonType.values(), value -> value.type);

	public TraceSaveReasonTypeFieldCodec(){
		super(
				type -> type.type,
				BY_TYPE_STRING::fromOrThrow,
				new Utf8BinaryDtoFieldCodec(),
				true);
	}

}
