package com.hotpads.datarouter.storage.field.enums;

import com.hotpads.util.core.enums.PersistentString;

public interface StringEnum<E>
extends Comparable<E>, PersistentString{
	E fromPersistentString(String string);
}
