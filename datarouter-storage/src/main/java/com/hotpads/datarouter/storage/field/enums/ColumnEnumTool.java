package com.hotpads.datarouter.storage.field.enums;

import java.util.Date;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;

public class ColumnEnumTool{

	public static String getFieldName(ColumnEnum columnEnum){
		if(columnEnum.getPrefix() == null){
			return columnEnum.getColumnName();
		}
		return columnEnum.getPrefix() + "." + columnEnum.getColumnName();
	}

	public static StringField field(ColumnEnum columnEnum, String value){
		return new StringField(columnEnum.getPrefix(), columnEnum.getColumnName(), value,
				MySqlColumnType.DEFAULT_LENGTH_VARCHAR);
	}

	public static IntegerField field(ColumnEnum columnEnum, Integer value){
		return new IntegerField(columnEnum.getPrefix(), columnEnum.getColumnName(), value);
	}

	public static DateField field(ColumnEnum columnEnum, Date value){
		return new DateField(columnEnum.getPrefix(), columnEnum.getColumnName(), value);
	}

	public static LongField field(ColumnEnum columnEnum, Long value){
		return new LongField(columnEnum.getPrefix(), columnEnum.getColumnName(), value);
	}
	// add more field types here if/as needed
}
