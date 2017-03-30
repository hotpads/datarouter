package com.hotpads.util.core.enums;

public interface StringEnum<E>
extends Comparable<E>, PersistentString{
	E fromPersistentString(String string);
}
