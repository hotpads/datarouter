package com.hotpads.notification.type;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.util.core.DrClassTool;
import com.hotpads.notification.destination.NotificationDestinationApp;
import com.hotpads.notification.tracking.TrackingNotificationType;

public abstract class BaseNotificationType implements NotificationType {

	public static class F {
		public static final String
			name = "name";
	}

	private String name = getClass().getName();

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getFieldName(){
		return F.name;
	}

	@Override
	public boolean isMergeableWith(NotificationType that) {
		return DrClassTool.sameClass(this, that);
	}

	@Override
	public List<NotificationDestinationApp> getDestinationApps() {
		return new ArrayList<>(getTemplateForApp().keySet());
	}

	public static TrackingNotificationType createEmptyInstance(){
		return createEmptyInstance(null);
	}

	public static TrackingNotificationType createEmptyInstance(final String pName){
		return new TrackingNotificationType(){

			class F {
				public static final String
					name = "name";
			}

			private String name = pName;

			@Override
			public String getName(){
				return name;
			}

			@Override
			public String getFieldName(){
				return F.name;
			}

		};
	}

}
