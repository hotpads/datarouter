package com.hotpads.datarouter.storage.field.encoding;

import com.hotpads.datarouter.storage.field.Field;


public interface AutoGeneratedField{

	public boolean isAutoGenerated();
	public Field<?> setAutoGenerated(boolean b);
	
}