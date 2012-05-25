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
			/*SUFFIX_modifyColumnLengths = ".modifyColumnLengths",*/
			SUFFIX_modifyColumn = ".modifyColumn",
			SUFFIX_addIndex = ".addIndex",
			SUFFIX_dropIndex = ".dropIndex";
	
	protected Boolean createTables;
	protected Boolean dropTables;
	protected Boolean addColumns;
	protected Boolean deleteColumns;
	/*protected Boolean modifyColumnLengths;*/
	protected Boolean modifyColumn;
	protected Boolean addIndex;
	protected Boolean dropIndex;
	
	public SchemaUpdateOptions(){
	}
	
	public SchemaUpdateOptions(List<Properties> multiProperties, String prefix, boolean printVsExecute){	
		if(printVsExecute){
			this.createTables = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_createTables));
			this.dropTables = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_dropTables));
			this.addColumns = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_addColumns));
			this.deleteColumns = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_deleteColumns));
			/*this.printModifyColumnLengths = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_printModifyColumnLengths));*/
			this.modifyColumn = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_modifyColumn));
			this.addIndex = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_addIndex));
			this.dropIndex = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_dropIndex));
		}else{
			this.createTables = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_createTables));
			this.dropTables = false;
			this.addColumns = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_addColumns));
			this.deleteColumns = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_deleteColumns));
			/*this.printModifyColumnLengths = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_printModifyColumnLengths));*/
			this.modifyColumn = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_modifyColumn));
			this.addIndex = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_addIndex));
			this.dropIndex = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
					prefix+SUFFIX_dropIndex));
		}
		
	}

	
	/****************************** methods ******************************/

//	public boolean anyTrue(){
//		return createTables | anyAlterTrue();
//	}
//	
//	public boolean anyAlterTrue(){
//		return dropTables | addColumns | deleteColumns | modify;
//	}
	
	public SchemaUpdateOptions setAllTrue(){
		createTables = true;
		dropTables = true;
		addColumns = true;
		deleteColumns = true;
		/*modifyColumnLengths;*/
		modifyColumn = true;
		addIndex = true;
		dropIndex = true;
		return this;
	}


	
	
	/******************************* get/set *****************************/

	public Boolean getCreateTables(){
		return createTables;
	}

	public void setCreateTables(Boolean createTables){
		this.createTables = createTables;
	}

	public Boolean getDropTables(){
		return dropTables;
	}

	public void setDropTables(Boolean dropTables){
		this.dropTables = dropTables;
	}

	public Boolean getAddColumns(){
		return addColumns;
	}

	public void setAddColumns(Boolean addColumns){
		this.addColumns = addColumns;
	}

	public Boolean getDeleteColumns(){
		return deleteColumns;
	}

	public void setDeleteColumns(Boolean deleteColumns){
		this.deleteColumns = deleteColumns;
	}

	public Boolean getModifyColumn(){
		return modifyColumn;
	}

	public void setModifyColumn(Boolean modifyColumn){
		this.modifyColumn = modifyColumn;
	}

	public Boolean getAddIndex(){
		return addIndex;
	}

	public void setAddIndex(Boolean addIndex){
		this.addIndex = addIndex;
	}

	public Boolean getDropIndex(){
		return dropIndex;
	}

	public void setDropIndex(Boolean dropIndex){
		this.dropIndex = dropIndex;
	}
	
	

}
