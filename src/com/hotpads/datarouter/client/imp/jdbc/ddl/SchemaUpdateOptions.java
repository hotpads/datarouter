package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;
import java.util.Properties;

import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.PropertiesTool;

public class SchemaUpdateOptions{
	
	public static final String 
			SUFFIX_createTables = ".createTables", 
			SUFFIX_dropTables = ".dropTables",
			SUFFIX_addColumns = ".addColumns", 
			SUFFIX_deleteColumns = ".deleteColumns",
			SUFFIX_modifyColumnLengths = ".modifyColumnLengths";

	
	protected Boolean createTables, dropTables, addColumns, deleteColumns, modifyColumnLengths;
	
	public SchemaUpdateOptions(){
	}
	
	public SchemaUpdateOptions(List<Properties> multiProperties, String prefix){
		this.createTables = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_createTables));
		this.dropTables = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_dropTables));
		this.addColumns = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_addColumns));
		this.deleteColumns = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_deleteColumns));
		this.modifyColumnLengths = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_modifyColumnLengths));
	}

	
	/****************************** methods ******************************/
	
	public boolean anyTrue(){
		return createTables | anyAlterTrue();
	}
	
	public boolean anyAlterTrue(){
		return dropTables | addColumns | deleteColumns | modifyColumnLengths;
	}
	
	public SchemaUpdateOptions setAllTrue(){
		createTables = true;
		dropTables = true;
		addColumns = true;
		deleteColumns = true;
		modifyColumnLengths = true;
		return this;
	}
	
	
	/******************************* get/set *****************************/

	public Boolean getCreateTables(){
		return createTables;
	}

	public Boolean getDropTables(){
		return dropTables;
	}

	public Boolean getAddColumns(){
		return addColumns;
	}

	public Boolean getDeleteColumns(){
		return deleteColumns;
	}

	public Boolean getModifyColumnLengths(){
		return modifyColumnLengths;
	}


	public void setCreateTables(Boolean createTables){
		this.createTables = createTables;
	}


	public void setDropTables(Boolean dropTables){
		this.dropTables = dropTables;
	}


	public void setAddColumns(Boolean addColumns){
		this.addColumns = addColumns;
	}


	public void setDeleteColumns(Boolean deleteColumns){
		this.deleteColumns = deleteColumns;
	}


	public void setModifyColumnLengths(Boolean modifyColumnLengths){
		this.modifyColumnLengths = modifyColumnLengths;
	}
	
	
	
}
