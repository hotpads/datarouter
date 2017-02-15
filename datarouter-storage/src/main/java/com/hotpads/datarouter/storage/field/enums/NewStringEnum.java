package com.hotpads.datarouter.storage.field.enums;

import com.hotpads.util.core.enums.DatarouterEnumTool;
import com.hotpads.util.core.enums.StringEnum;

public interface NewStringEnum<E extends StringEnum<E>> extends StringEnum<E>{

	@Override
	default E fromPersistentString(String string){
		return DatarouterEnumTool.getEnumFromString(getValues(), string, null);
	}

	E[] getValues();

}
