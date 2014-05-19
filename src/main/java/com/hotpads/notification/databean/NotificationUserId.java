package com.hotpads.notification.databean;

public class NotificationUserId {

	private NotificationUserType type;
	private String id;

	public NotificationUserId(NotificationUserType userType, String id) {
		super();
		this.type = userType;
		this.id = id;
	}

	public NotificationUserType getType() {
		return type;
	}

	public void setType(NotificationUserType userType) {
		this.type = userType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "NotificationUserId (" + type + ", " + id + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NotificationUserId other = (NotificationUserId) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
