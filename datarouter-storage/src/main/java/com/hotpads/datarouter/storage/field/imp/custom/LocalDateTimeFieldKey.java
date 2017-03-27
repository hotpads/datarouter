package com.hotpads.datarouter.storage.field.imp.custom;

import java.time.LocalDateTime;

import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class LocalDateTimeFieldKey extends BaseFieldKey<LocalDateTime>{

	public static final int BACKWARDS_COMPATIBLE_NUM_FRACTIONAL_SECONDS = 0;
	public static final int DEFAULT_NUM_FRACTIONAL_SECONDS = 3;

	private final int numFractionalSeconds;

	/**
	 * Defines a LocalDateFieldKey with millis precision
	 */
	public LocalDateTimeFieldKey(String name){
		super(name);
		this.numFractionalSeconds = DEFAULT_NUM_FRACTIONAL_SECONDS;
	}

	private LocalDateTimeFieldKey(String name, String columnName, boolean nullable,
			FieldGeneratorType fieldGeneratorType, LocalDateTime defaultValue, int numFractionalSeconds){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
		this.numFractionalSeconds = numFractionalSeconds;
	}

	public int getNumFractionalSeconds(){
		return numFractionalSeconds;
	}

	public LocalDateTimeFieldKey withColumnName(String columnNameOverride){
		return new LocalDateTimeFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue,
				numFractionalSeconds);
	}

	/**
	 * Defines a LocalDateFieldKey with 0-9 fractional seconds of precision
	 */
	public LocalDateTimeFieldKey overrideNumFractionalSeconds(int numFractionalSeconds){
		if(numFractionalSeconds < 0 || numFractionalSeconds > 9){
			throw new RuntimeException("numFractionalSeconds cannot be less than 0 or greater than 9");
		}
		return new LocalDateTimeFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue,
				numFractionalSeconds);
	}
}
