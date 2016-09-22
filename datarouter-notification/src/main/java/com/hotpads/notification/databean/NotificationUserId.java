package com.hotpads.notification.databean;

import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumFieldKey;

public class NotificationUserId{

	private NotificationUserType type;
	private String id;


	private static final String
		COL_USER_ID = "userId",
		COL_USER_TYPE = "userType";


	public static class FieldKeys{
		public static final StringFieldKey userId = new StringFieldKey("id").withColumnName(COL_USER_ID);
		public static final StringEnumFieldKey<NotificationUserType> userType = new StringEnumFieldKey<>("type",
				NotificationUserType.class).withColumnName(COL_USER_TYPE);
	}

	public NotificationUserId(NotificationUserType userType, String id){
		this.type = userType;
		this.id = id;
	}

	public NotificationUserType getType(){
		return type;
	}

	public void setType(NotificationUserType userType){
		this.type = userType;
	}

	public String getId(){
		return id;
	}

	public void setId(String id){
		this.id = id;
	}

	@Override
	public String toString(){
		return "NotificationUserId (" + type + ", " + id + ")";
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		NotificationUserId other = (NotificationUserId)obj;
		if(id == null){
			if(other.id != null){
				return false;
			}
		}else if(!id.equals(other.id)){
			return false;
		}
		if(type != other.type){
			return false;
		}
		return true;
	}

}
