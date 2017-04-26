package com.hotpads.util.core.enums;

public interface StringEnum<E>
extends Comparable<E>, PersistentString{
	E fromPersistentString(String string);

	static <E extends StringEnum<E>> E fromPersistentStringSafe(E sampleValue, String persistentString){
		if(persistentString == null){
			return null;
		}
		E enumValue = sampleValue.fromPersistentString(persistentString);
		if(enumValue == null || !persistentString.equals(enumValue.getPersistentString())){
			throw new RuntimeException(sampleValue.getClass().getSimpleName() + ".fromPersistentString returned "
					+ enumValue == null ? "null" : enumValue.getPersistentString() + " instead of " + persistentString);
		}
		return enumValue;
	}
}
