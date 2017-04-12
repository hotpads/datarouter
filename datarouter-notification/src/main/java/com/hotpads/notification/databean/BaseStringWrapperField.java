package com.hotpads.notification.databean;

import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public abstract class BaseStringWrapperField{

	public static final StringFieldKey key = new StringFieldKey("persistentString");

	public final String persistentString;

	public BaseStringWrapperField(String persistentString){
		this.persistentString = persistentString;
	}

	public BaseStringWrapperField(){
		this(null);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null || !(obj instanceof BaseStringWrapperField)){//TODO consider comparing getClass as well...
			return false;
		}
		BaseStringWrapperField other = (BaseStringWrapperField)obj;

		if(persistentString == null){
			return other.persistentString == null;
		}
		return persistentString.equals(other.persistentString);
	}

	@Override
	public int hashCode(){
		return persistentString == null ? 0 : persistentString.hashCode();
	}

	//for JSP
	public String getPersistentString(){
		return persistentString;
	}
}
