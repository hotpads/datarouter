package com.hotpads.notification.type;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.notification.sender.template.NotificationTemplate;
import com.hotpads.notification.tracking.TrackingNotificationType;
import com.hotpads.util.core.ClassTool;

public abstract class BaseNotificationType implements NotificationType {

	public static class F {
		public static final String
			name = "name";
	}

	private List<Class<? extends NotificationTemplate>> templates = new ArrayList<>();

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

	@Override
	public List<Class<? extends NotificationTemplate>> getTemplates() {
		return templates;
	}

	protected void addTemplate(Class<? extends NotificationTemplate> template) {
		templates.add(template);
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
