package com.hotpads.datarouter.storage.field.imp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.LongByteTool;

public class DateField extends BasePrimitiveField<Date>{
	private static final Logger logger = LoggerFactory.getLogger(DateField.class);

	public static final int
		BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS = 0,
		DEFAULT_DECIMAL_SECONDS = 3;//match java's millisecond precision

	private final int numDecimalSeconds;


	public DateField(PrimitiveFieldKey<Date> key, Date value){
		super(key, value);
		this.numDecimalSeconds = BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS;
	}

	@Deprecated
	public DateField(String name, Date value){
		super(name, value);
		this.numDecimalSeconds = BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS;
	}

	@Deprecated
	public DateField(String name, Date value, int numDecimalSeconds){
		super(name, value);
		this.numDecimalSeconds =  numDecimalSeconds;
	}

	@Deprecated
	public DateField(String prefix, String name, Date value){
		super(prefix, name, value);
		this.numDecimalSeconds = BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS;
	}

	@Deprecated
	public DateField(String prefix, String name, String columnName, boolean nullable, Date value){
		super(prefix, name, columnName, nullable, FieldGeneratorType.NONE, value);
		this.numDecimalSeconds = BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS;
	}


	public int getNumDecimalSeconds(){
		return numDecimalSeconds;
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return DrDateTool.getInternetDate(value);
	}

	@Override
	public Date parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s) || s.equals("null")){
			return null;
		}
		String fmt = "E MMM dd HH:mm:ss z yyyy";
		try{
			Date parsed = new SimpleDateFormat(fmt).parse(s);
			return parsed;
		}catch(ParseException e){
			logger.error("wrong date format s=" + s);
		}
		return DrDateTool.parseUserInputDate(s,null);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		if(value==null){ return null; }
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
			String dateStr = "Mon Jun 22 00:00:00 PDT 2015";
			DateField field = new DateField("test", null);
			Date date = field.parseStringEncodedValueButDoNotSet(dateStr);
			Assert.assertNotNull(date);
		}
	}
}
