package com.hotpads.datarouter.storage.field.imp;

import java.util.Date;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class DateFieldKey extends PrimitiveFieldKey<Date>{

	private static final int BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS = 0;
	private static final int DEFAULT_DECIMAL_SECONDS = 3;//match java's millisecond precision

	private final int numDecimalSeconds;

	/**
	 * Defines a DateFieldKey with seconds precision
	 */
	public DateFieldKey(String name){
		super(name, Date.class);
		this.numDecimalSeconds = BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS;
	}

	private DateFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Date defaultValue, int numDecimalSeconds){
		super(name, columnName, nullable, Date.class, fieldGeneratorType, defaultValue);
		this.numDecimalSeconds = numDecimalSeconds;
	}

	public DateFieldKey withColumnName(String columnNameOverride){
		return new DateFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue,
				numDecimalSeconds);
	}

	public int getNumDecimalSeconds(){
		return numDecimalSeconds;
	}

	/**
	 * Defines a DateFieldKey with millis precision
	 */
	public DateFieldKey withMillis(){
		return new DateFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, DEFAULT_DECIMAL_SECONDS);
	}

	@Override
	public DateField createValueField(final Date value){
		return new DateField(this, value);
	}
}
