package com.hotpads.notification.type;

import java.util.Map;

import com.hotpads.notification.sender.NotificationSender;
import com.hotpads.notification.sender.template.NotificationTemplate;
import com.hotpads.notification.tracking.TrackingNotificationType;
import com.hotpads.util.core.ClassTool;
import com.hotpads.util.core.MapTool;

public abstract class BaseNotificationType implements NotificationType {

	public static class F {
		public static final String
			name = "name";
	}

	private Map<Class<? extends NotificationSender>, Class<? extends NotificationTemplate<?>>> senderAndTemplates = MapTool.create();

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
		return ClassTool.sameClass(this, that);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Class<? extends NotificationSender>, Class<? extends NotificationTemplate<?>>> getSendersAndTemplates() {
		return senderAndTemplates;
	}

	protected final <S extends NotificationSender> void addSenderAndTemplate(Class<S> sender, Class<? extends NotificationTemplate<S>> template) {
		senderAndTemplates.put(sender, template);
	}

	public static TrackingNotificationType createEmptyInstance(){
		return new TrackingNotificationType(){

			class F {
				public static final String
					name = "name";
			}

			private String name;

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
