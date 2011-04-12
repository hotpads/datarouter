package com.hotpads.datarouter.storage.field.enums;

public interface StringEnum<E>
extends Comparable<E>{

	String getPersistentString();
	E fromPersistentString(String s);
	
}
