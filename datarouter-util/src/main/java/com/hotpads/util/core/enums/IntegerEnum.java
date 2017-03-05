package com.hotpads.util.core.enums;

public interface IntegerEnum<E>
extends Comparable<E>{

	Integer getPersistentInteger();
	E fromPersistentInteger(Integer value);

}
