package com.hotpads.util.core.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface StringEnum<E>
extends Comparable<E>, PersistentString{
	static final Logger logger = LoggerFactory.getLogger(StringEnum.class);

	E fromPersistentString(String string);

	static <E extends StringEnum<E>> E fromPersistentStringSafe(E sampleValue, String persistentString){
		if(persistentString == null){
			return null;
		}
		E enumValue = sampleValue.fromPersistentString(persistentString);
		if(enumValue == null || !persistentString.equals(enumValue.getPersistentString())){
			logger.error(sampleValue.getClass().getSimpleName() + ".fromPersistentString returned "
					+ (enumValue == null ? "null" : enumValue.getPersistentString()) + " instead of "
					+ persistentString);
		}
		return enumValue;
	}
}
