package com.hotpads.notification.destination;

import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public class NotificationDestinationApp{

	public static final StringFieldKey key = new StringFieldKey("persistentString");

	public final String persistentString;

	public NotificationDestinationApp(String persistentString){
		this.persistentString = persistentString;
	}

	public NotificationDestinationApp(){
		this(null);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null || !(obj instanceof NotificationDestinationApp)){
			return false;
		}
		NotificationDestinationApp other = (NotificationDestinationApp)obj;

		if(persistentString == null){
			return other.persistentString == null;
		}
		return persistentString.equals(other.persistentString);
	}

	@Override
	public int hashCode(){
		return persistentString == null ? 0 : persistentString.hashCode();
	}
}
