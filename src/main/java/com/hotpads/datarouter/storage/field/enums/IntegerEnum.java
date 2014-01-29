package com.hotpads.datarouter.storage.field.enums;

public interface IntegerEnum<E>
extends Comparable<E>{

	Integer getPersistentInteger();
	E fromPersistentInteger(Integer i);
	
}
