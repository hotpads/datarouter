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
package io.datarouter.model.field.imp;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.util.DateTool;
import io.datarouter.util.bytes.LongByteTool;
import io.datarouter.util.string.StringTool;

public class DateField extends BasePrimitiveField<Date>{

	public DateField(DateFieldKey key, Date value){
		this(null, key, value);
	}

	public DateField(String prefix, DateFieldKey key, Date value){
		super(prefix, key, value);
	}

	public int getNumDecimalSeconds(){
		return ((DateFieldKey) getKey()).getNumDecimalSeconds();
	}

	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return DateTool.getInternetDate(value);
	}

	@Override
	public Date parseStringEncodedValueButDoNotSet(String str){
		if(StringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return DateTool.parseUserInputDate(str,null);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		return LongByteTool.getUInt63Bytes(value.getTime());
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}

	@Override
	public Date fromBytesButDoNotSet(byte[] bytes, int offset){
		return new Date(LongByteTool.fromUInt63Bytes(bytes, offset));
	}

	public static class DateFieldTester{
		@Test
		public void testParseStringEncodedValueButDoNotSet(){
			String dateStr = "2016-06-22T19:20:14Z";
			String dateStr2 = "Mon Jun 22 00:00:00 PDT 2015";
			DateField field = new DateField(new DateFieldKey("test"), null);
			Date date = field.parseStringEncodedValueButDoNotSet(dateStr);
			Assert.assertEquals(date.getTime(), 1466648414000L);
			Date date2 = field.parseStringEncodedValueButDoNotSet(dateStr2);
			Assert.assertEquals(date2.getTime(), 1434956400000L);
			Assert.assertNotNull(date);
		}
	}
}
