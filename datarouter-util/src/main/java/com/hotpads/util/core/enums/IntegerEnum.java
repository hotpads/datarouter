package com.hotpads.util.core.enums;

public interface IntegerEnum<E>
extends Comparable<E>{

	Integer getPersistentInteger();
	E fromPersistentInteger(Integer value);

	static <E extends IntegerEnum<E>> E fromPersistentIntegerSafe(E sampleValue, Integer persistentInteger){
		if(persistentInteger == null){
			return null;
		}
		E enumValue = sampleValue.fromPersistentInteger(persistentInteger);
		if(enumValue == null || persistentInteger != enumValue.getPersistentInteger()){
			throw new RuntimeException(sampleValue.getClass().getSimpleName() + ".fromPersistentInteger returned "
					+ enumValue == null ? "null" : enumValue.getPersistentInteger() + " instead of "
					+ persistentInteger);
		}
		return enumValue;
	}

}
