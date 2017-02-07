package com.hotpads.export;


import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;

public class DataExportItem extends BaseDatabean<DataExportItemKey,DataExportItem>{

	/** fields ****************************************************************/

	private DataExportItemKey key;
	private String routerName;
	private String nodeName;
	private String startAfterKey;
	private String endBeforeKey;
	private Long maxRows;
	private Date dateCreated;

	public static class F{

		public static final String
				routerName = "routerName",
				nodeName = "nodeName",
				startAfterKey = "startAfterKey",
				endBeforeKey = "endBeforeKey",
				maxRows = "maxRows",
				dateCreated = "dateCreated";
	}

	public static class DataExportItemFielder extends BaseDatabeanFielder<DataExportItemKey, DataExportItem>{

		public DataExportItemFielder(){
		}

		@Override
		public Class<DataExportItemKey> getKeyFielderClass(){
			return DataExportItemKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(DataExportItem databean){
			return FieldTool.createList(
					new StringField(F.routerName, databean.routerName, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.nodeName, databean.nodeName, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.startAfterKey, databean.startAfterKey, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.endBeforeKey, databean.endBeforeKey, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new LongField(F.maxRows, databean.maxRows),
					new DateField(F.dateCreated, databean.dateCreated));
		}

	}

	/** databean ****************************************************************/

	@Override
	public Class<DataExportItemKey> getKeyClass(){
		return DataExportItemKey.class;
	}

	@Override
	public DataExportItemKey getKey(){
		return key;
	}


	/** construct *************************************************************/

	public DataExportItem(){
		this.key = new DataExportItemKey(null, null);
	}

	public DataExportItem(Long exportId, Integer rowId, String routerName, String nodeName, String startAfterKey,
			String endBeforeKey, Long maxRows){
		this.key = new DataExportItemKey(exportId, rowId);
		this.routerName = routerName;
		this.nodeName = nodeName;
		this.startAfterKey = startAfterKey;
		this.endBeforeKey = endBeforeKey;
		this.maxRows = maxRows;
		this.dateCreated = new Date();
	}

	/** get/set ***************************************************************/

	public void setKey(DataExportItemKey key){
		this.key = key;
	}

	public String getRouterName(){
		return routerName;
	}

	public void setRouterName(String routerName){
		this.routerName = routerName;
	}

	public String getNodeName(){
		return nodeName;
	}

	public void setNodeName(String nodeName){
		this.nodeName = nodeName;
	}

	public String getStartAfterKey(){
		return startAfterKey;
	}

	public void setStartAfterKey(String startAfterKey){
		this.startAfterKey = startAfterKey;
	}

	public String getEndBeforeKey(){
		return endBeforeKey;
	}

	public void setEndBeforeKey(String endBeforeKey){
		this.endBeforeKey = endBeforeKey;
	}

	public Long getMaxRows(){
		return maxRows;
	}

	public void setMaxRows(Long maxRows){
		this.maxRows = maxRows;
	}

}
