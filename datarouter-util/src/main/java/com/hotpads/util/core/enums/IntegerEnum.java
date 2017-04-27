package com.hotpads.util.core.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface IntegerEnum<E>
extends Comparable<E>{
	static final Logger logger = LoggerFactory.getLogger(IntegerEnum.class);

	Integer getPersistentInteger();
	E fromPersistentInteger(Integer value);

	public static <E extends IntegerEnum<E>> E fromPersistentIntegerSafe(E sampleValue, Integer persistentInteger){
		if(persistentInteger == null){
			return null;
		}
		E enumValue = sampleValue.fromPersistentInteger(persistentInteger);
		if(enumValue == null || !persistentInteger.equals(enumValue.getPersistentInteger())){
			logger.error(sampleValue.getClass().getSimpleName() + ".fromPersistentInteger returned "
					+ (enumValue == null ? "null" : enumValue.getPersistentInteger()) + " instead of "
					+ persistentInteger);
		}
		return enumValue;
	}

}
