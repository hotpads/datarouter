package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.List;
import java.util.Properties;

import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.StringTool;

public class SchemaUpdateOptions{
	
	public static final String 
			SUFFIX_createTables = ".createTables", 
			SUFFIX_dropTables = ".dropTables",
			SUFFIX_addColumns = ".addColumns", 
			SUFFIX_deleteColumns = ".deleteColumns",
			/*SUFFIX_modifyColumnLengths = ".modifyColumnLengths",*/
			SUFFIX_modifyColumns = ".modifyColumns",
			SUFFIX_addIndexes = ".addIndexes",
			SUFFIX_dropIndexes = ".dropIndexes",
			SUFFIX_modifyEngine = ".modifyEngine",
			SUFFIX_ignoreClients = ".ignoreClients",
			SUFFIX_ignoreTables = ".ignoreTables",
			SUFFIX_modifyCharacterSet = ".modifyCharacterSet",
			SUFFIX_modifyCollation = ".modifyCollation";

	 
	
	protected Boolean createTables;
	protected Boolean dropTables;
	protected Boolean addColumns;
	protected Boolean deleteColumns;
	/*protected Boolean modifyColumnLengths;*/
	protected Boolean modifyColumns;
	protected Boolean addIndexes;
	protected Boolean dropIndexes;
	protected Boolean modifyEngine;
	protected boolean modifyCollation;
	protected boolean modifyCharacterSet;
	protected List<String> ignoreClients;
	protected List<String> ignoreTables;


	public SchemaUpdateOptions(){
		super();
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
		this.modifyColumns = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_modifyColumns));
		this.addIndexes = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_addIndexes));
		this.dropIndexes = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_dropIndexes));
		this.modifyEngine = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_modifyEngine));
		this.modifyCharacterSet = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_modifyCharacterSet));
		this.modifyCollation = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_modifyCollation));
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
		this.modifyColumns = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_modifyColumns));
		this.addIndexes = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_addIndexes));
		this.dropIndexes = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_dropIndexes));
		this.modifyEngine = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_modifyEngine));
		this.modifyCharacterSet = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_modifyCharacterSet));
		this.modifyCollation = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_modifyCollation));
		String schemaUpdatePrefix = prefix.substring(0, prefix.indexOf('.'));
		String clientsToIgnore = PropertiesTool.getFirstOccurrence(multiProperties, schemaUpdatePrefix + SUFFIX_ignoreClients);
		this.ignoreClients = StringTool.splitOnCharNoRegex(clientsToIgnore, ',');
		String tablesToIgnore = PropertiesTool.getFirstOccurrence(multiProperties, schemaUpdatePrefix  + SUFFIX_ignoreTables);
		this.ignoreTables = StringTool.splitOnCharNoRegex(tablesToIgnore, ',');
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
		modifyColumns = true;
		addIndexes = true;
		dropIndexes = true;
		modifyEngine = true;
		return this;
	}
	
	public SchemaUpdateOptions setAllFalse(){
		createTables = false;
		dropTables = false;
		addColumns = false;
		deleteColumns = false;
		/*modifyColumnLengths;*/
		modifyColumns = false;
		addIndexes = false;
		dropIndexes = false;
		modifyEngine = false;
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

	public Boolean getModifyColumns(){
		return modifyColumns;
	}

	public SchemaUpdateOptions setModifyColumns(Boolean modifyColumns){
		this.modifyColumns = modifyColumns;
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

	public Boolean getModifyEngine(){
		return modifyEngine;
	}

	public void setModifyEngine(Boolean modifyEngine){
		this.modifyEngine = modifyEngine;
	}

	public List<String> getIgnoreClients(){
		return ignoreClients;
	}

	public void setIgnoreClients(List<String> ignoreClients){
		this.ignoreClients = ignoreClients;
	}

	public List<String> getIgnoreTables(){
		return ignoreTables;
	}

	public void setIgnoreTables(List<String> ignoreTables){
		this.ignoreTables = ignoreTables;
	}

	public boolean getModifyCollation(){
		return modifyCollation;
	}

	public boolean getModifyCharacterSet(){
		// TODO Auto-generated method stub
		return modifyCharacterSet;
	}
	
	

}
