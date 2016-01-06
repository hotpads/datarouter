package com.hotpads.datarouter.storage.field.enums;

public interface NewStringEnum<E extends StringEnum<E>> extends StringEnum<E>{

	@Override
	default E fromPersistentString(String string){
		return DatarouterEnumTool.getEnumFromString(getValues(), string, null);
	}

	E[] getValues();

}
