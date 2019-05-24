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
package io.datarouter.model.field.imp.array;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;
import io.datarouter.util.bytes.IntegerByteTool;
import io.datarouter.util.exception.NotImplementedException;

public class PrimitiveIntegerArrayField extends BaseField<int[]>{

	private PrimitiveIntegerArrayFieldKey key;

	public PrimitiveIntegerArrayField(PrimitiveIntegerArrayFieldKey key, int[] value){
		super(null, value);
		this.key = key;
	}

	@Override
	public PrimitiveIntegerArrayFieldKey getKey(){
		return key;
	}

	@Override
	public String getValueString(){
		return Arrays.toString(value);
	}

	@Override
	public int getValueHashCode(){
		return Arrays.hashCode(value);
	}

	@Override
	public int compareTo(Field<int[]> field){
		if(field == null){
			return 1;
		}
		return toString().compareTo(field.toString());
	}

	@Override
	public String getStringEncodedValue(){
		throw new NotImplementedException();
	}

	@Override
	public int[] parseStringEncodedValueButDoNotSet(String value){
		throw new NotImplementedException();
	}

	@Override
	public byte[] getBytes(){
		return IntegerByteTool.getComparableByteArray(value);
	}

	@Override
	public int[] fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		return IntegerByteTool.fromComparableByteArray(bytes);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new NotImplementedException();
	}

	public static class PrimitiveIntegerArrayFieldTests{

		@Test
		public void testByteSerialization(){
			int[] array = {1, 2, 100};
			PrimitiveIntegerArrayField field = new PrimitiveIntegerArrayField(new PrimitiveIntegerArrayFieldKey("test"),
					array);
			Assert.assertEquals(field.fromBytesButDoNotSet(field.getBytes(), 0), array);
		}

	}

}
