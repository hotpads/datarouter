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
		if(obj == null || !(obj instanceof BaseStringWrapperField) || !this.getClass().getSimpleName().equals(
				obj.getClass().getSimpleName())){
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
		return (this.getClass().getSimpleName() + (persistentString == null ? "" : persistentString)).hashCode();
	}

	//for JSP
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public String toString(){
		return this.getClass().getSimpleName() + (persistentString == null ? "_" : "_" + persistentString);
	}
}
