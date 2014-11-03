package com.hotpads.notification.alias.databean;

import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class AutomatedEmailKey extends BasePrimaryKey<AutomatedEmailKey> {

	/** fields ****************************************************************/

	private Long reverseCreatedMs;
	private Long nanoTime;

	/** columns ***************************************************************/

	public static class F {
		public static final String
			reverseCreatedMs = "reverseCreatedMs",
			nanoTime = "nanoTime";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
			new LongField(F.reverseCreatedMs, reverseCreatedMs),
			new LongField(F.nanoTime, nanoTime));
	}

	/** construct *************************************************************/

	public AutomatedEmailKey(){
		this.reverseCreatedMs = getReverseDate(new Date());
		this.nanoTime = System.nanoTime();
	}

	public static Long getReverseDate(Date date) {
		if (date == null) {
			return null;
		}
		return Long.MAX_VALUE - date.getTime();
	}

	/** get/set ***************************************************************/

}
