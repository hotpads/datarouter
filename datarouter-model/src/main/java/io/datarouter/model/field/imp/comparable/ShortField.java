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
package io.datarouter.model.field.imp.comparable;

import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.util.bytes.ShortByteTool;
import io.datarouter.util.string.StringTool;

public class ShortField extends BasePrimitiveField<Short,ShortFieldKey>{

	public ShortField(ShortFieldKey key, Short value){
		super(key, value);
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.toString();
	}

	@Override
	public Short parseStringEncodedValueButDoNotSet(String str){
		if(StringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return Short.valueOf(str);
	}

	@Override
	public byte[] getBytes(){
		return value == null ? null : ShortByteTool.getComparableBytes(this.value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 2;
	}

	@Override
	public Short fromBytesButDoNotSet(byte[] bytes, int offset){
		return ShortByteTool.fromComparableBytes(bytes, offset);
	}

}
