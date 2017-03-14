package com.hotpads.datarouter.storage.field.imp;

import java.util.Date;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class DateFieldKey extends PrimitiveFieldKey<Date>{

	private static final int BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS = 0;
	private static final int DEFAULT_DECIMAL_SECONDS = 3;//match java's millisecond precision

	private final int numDecimalSeconds;

	/**
	 * Defines a DateFieldKey with milliseconds precision
	 */
	public static DateFieldKey createWithMillis(String name){
		return new DateFieldKey(name, DEFAULT_DECIMAL_SECONDS);
	}

	/**
	 * Defines a DateFieldKey with seconds precision
	 */
	public static DateFieldKey createWithSeconds(String name){
		return new DateFieldKey(name);
	}

	/**
	 * Defines a DateFieldKey with seconds precision
	 */
	public DateFieldKey(String name){
		this(name, BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS);
	}

	private DateFieldKey(String name, int numDecimalSeconds){
		super(name);
		this.numDecimalSeconds = numDecimalSeconds;
	}

	private DateFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Date defaultValue){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
		this.numDecimalSeconds = BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS;
	}

	public DateFieldKey withColumnName(String columnNameOverride){
		return new DateFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue);
	}

	public int getNumDecimalSeconds(){
		return numDecimalSeconds;
	}

}
