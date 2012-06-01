package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

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
			SUFFIX_addIndexes = ".addIndexes",
			SUFFIX_dropIndexes = ".dropIndexes";
	
	protected Boolean createTables;
	protected Boolean dropTables;
	protected Boolean addColumns;
	protected Boolean deleteColumns;
	/*protected Boolean modifyColumnLengths;*/
	protected Boolean modifyColumn;
	protected Boolean addIndexes;
	protected Boolean dropIndexes;
	
	public SchemaUpdateOptions(){
	}
	
	public SchemaUpdateOptions(List<Properties> multiProperties, String prefix, boolean printVsExecute){	
		if(printVsExecute){
			SetSchemaUpdateWithPrintOptions(multiProperties,  prefix);
		}else{
			SetSchemaUpdateWithExecuteOptions(multiProperties,  prefix);
		}
		
	}

	private SchemaUpdateOptions SetSchemaUpdateWithPrintOptions(List<Properties> multiProperties, String prefix){
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
		this.addIndexes = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_addIndexes));
		this.dropIndexes = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_dropIndexes));
		return this;
	}

	private SchemaUpdateOptions SetSchemaUpdateWithExecuteOptions(List<Properties> multiProperties, String prefix){
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
		this.addIndexes = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_addIndexes));
		this.dropIndexes = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_dropIndexes));
		return this;
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
		addIndexes = true;
		dropIndexes = true;
		return this;
	}
	
	public SchemaUpdateOptions setAllFalse(){
		createTables = false;
		dropTables = false;
		addColumns = false;
		deleteColumns = false;
		/*modifyColumnLengths;*/
		modifyColumn = false;
		addIndexes = false;
		dropIndexes = false;
		return this;
	}


	
	
	/******************************* get/set *****************************/

	public Boolean getCreateTables(){
		return createTables;
	}

	public SchemaUpdateOptions setCreateTables(Boolean createTables){
		this.createTables = createTables;
		return this;
	}

	public Boolean getDropTables(){
		return dropTables;
	}

	public SchemaUpdateOptions setDropTables(Boolean dropTables){
		this.dropTables = dropTables;
		return this;
	}

	public Boolean getAddColumns(){
		return addColumns;
	}

	public SchemaUpdateOptions setAddColumns(Boolean addColumns){
		this.addColumns = addColumns;
		return this;
	}

	public Boolean getDeleteColumns(){
		return deleteColumns;
	}

	public SchemaUpdateOptions setDeleteColumns(Boolean deleteColumns){
		this.deleteColumns = deleteColumns;
		return this;
	}

	public Boolean getModifyColumn(){
		return modifyColumn;
	}

	public SchemaUpdateOptions setModifyColumn(Boolean modifyColumn){
		this.modifyColumn = modifyColumn;
		return this;
	}

	public Boolean getAddIndexes(){
		return addIndexes;
	}

	public SchemaUpdateOptions setAddIndexes(Boolean addIndexes){
		this.addIndexes = addIndexes;
		return this;
	}

	public Boolean getDropIndexes(){
		return dropIndexes;
	}

	public SchemaUpdateOptions setDropIndexes(Boolean dropIndexes){
		this.dropIndexes = dropIndexes;
		return this;
	}
	
	

}
