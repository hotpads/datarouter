package com.hotpads.clustersetting;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.datarouter.util.core.DrDateTool;

public class ClusterSettingLogKey extends BasePrimaryKey<ClusterSettingLogKey>{

	/********************** fields ****************************************/

	private String name;
	private Long reverseCreatedMs;

	public static class FieldKeys{
		public static final LongFieldKey reverseCreatedMs = new LongFieldKey("reverseCreatedMs");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(ClusterSettingKey.FieldKeys.name, name),
				new LongField(FieldKeys.reverseCreatedMs, reverseCreatedMs));
	}

	/*************************** constructors *******************************/

	ClusterSettingLogKey(){//required no-arg
	}

	public ClusterSettingLogKey(String name, Date date){
		this.name = name;
		this.reverseCreatedMs = DrDateTool.toReverseDateLong(date);
	}

	public static ClusterSettingLogKey createPrefix(String name){
		return new ClusterSettingLogKey(name, null);
	}

	/***************************** get/set *******************************/

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public Long getReverseCreatedMs(){
		return reverseCreatedMs;
	}

	public Date getCreated(){
		return DrDateTool.fromReverseDateLong(reverseCreatedMs);
	}
}