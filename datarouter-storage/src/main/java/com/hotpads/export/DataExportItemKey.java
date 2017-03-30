package com.hotpads.export;


import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class DataExportItemKey extends BasePrimaryKey<DataExportItemKey>{

	private Long exportId;
	private Integer rowId;

	public static class F{
		public static final String
			exportId = "exportId",
			rowId = "rowId"
			;
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new LongField(F.exportId, exportId),
				new UInt31Field(F.rowId, rowId));
	}
	/****************************** constructor ********************************/

	DataExportItemKey(){
	}

	public DataExportItemKey(Long exportId, Integer rowId){
		this.exportId = exportId;
		this.rowId = rowId;
	}

	public Long getExportId(){
		return exportId;
	}

	public void setK(Long exportId){
		this.exportId = exportId;
	}

	public Integer getRowId(){
		return rowId;
	}

	public void setRowId(Integer rowId){
		this.rowId = rowId;
	}
}
