package com.hotpads.handler.user;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class DatarouterUserKey extends BasePrimaryKey<DatarouterUserKey> {

	/** fields ********************************************************************************************************/
	private Long id;

	public static class F {
		public static final String
			ID = "id";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
			new UInt63Field(F.ID, id));
	}

	/** constructors **************************************************************************************************/
	DatarouterUserKey(){}

	public DatarouterUserKey(Long id){
		this.id = id;
	}
	
//	public static DatarouterUserKey create(){
//		return new DatarouterUserKey(RandomTool.nextPositiveLong());
//	}

	/** getters/setters ***********************************************************************************************/
	public Long getId(){
		return this.id;
	}

	public void setId(Long id){
		this.id = id;
	}

}