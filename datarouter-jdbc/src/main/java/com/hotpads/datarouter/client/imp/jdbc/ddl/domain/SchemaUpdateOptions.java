package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.List;
import java.util.Properties;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class SchemaUpdateOptions{

	public static final String
	SUFFIX_createDatabases = ".createDatabases",
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
	SUFFIX_modifyCharacterSetOrCollation = ".modifyCharacterSetOrCollation",
	SCHEMA_UPDATE_ENABLE = "schemaUpdate.enable"
	;

	private Boolean createDatabases;
	private Boolean createTables;
	private Boolean dropTables;
	private Boolean addColumns;
	private Boolean deleteColumns;
	/*private Boolean modifyColumnLengths;*/
	private Boolean modifyColumns;
	private Boolean addIndexes;
	private Boolean dropIndexes;
	private Boolean modifyEngine;
	private boolean modifyCharacterSetOrCollation;
	private Boolean schemaUpdateEnabled;
	private List<String> ignoreClients;
	private List<String> ignoreTables;


	public SchemaUpdateOptions(){
		super();
	}

	public SchemaUpdateOptions(List<Properties> multiProperties, String prefix, boolean printVsExecute){
		if(printVsExecute){
			setSchemaUpdateWithPrintOptions(multiProperties,  prefix);
		}else{
			setSchemaUpdateWithExecuteOptions(multiProperties,  prefix);
		}

	}

	private SchemaUpdateOptions setSchemaUpdateWithPrintOptions(List<Properties> multiProperties, String prefix){
		this.schemaUpdateEnabled = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				SCHEMA_UPDATE_ENABLE));
		this.createDatabases = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_createDatabases));
		this.createTables = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_createTables));
		this.dropTables = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_dropTables));
		this.addColumns = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_addColumns));
		this.deleteColumns = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_deleteColumns));
		/*this.printModifyColumnLengths = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_printModifyColumnLengths));*/
		this.modifyColumns = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_modifyColumns));
		this.addIndexes = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_addIndexes));
		this.dropIndexes = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_dropIndexes));
		this.modifyEngine = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_modifyEngine));
		this.modifyCharacterSetOrCollation = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(
				multiProperties, prefix+SUFFIX_modifyCharacterSetOrCollation));
		return this;
	}

	private SchemaUpdateOptions setSchemaUpdateWithExecuteOptions(List<Properties> multiProperties, String prefix){
		//isTrue returns false as default and isTrueOrNull returns a true as default,
		//so on missing the setting in config, isTrueOrNull returns a default value true
		this.schemaUpdateEnabled = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				SCHEMA_UPDATE_ENABLE));
		//createDatabase and createTables are set to default true to avoid confusions in developers machine
		//due to missing databases and tables
		this.createDatabases = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_createDatabases));
		this.createTables = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_createTables));

		//drop tables are always set to false for obvious reasons
		this.dropTables = false;

		//settings that modify an existing tables are returned with default true since they are less dangerous
		this.addColumns = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_addColumns));
		this.deleteColumns = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_deleteColumns));
		/*this.printModifyColumnLengths = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_printModifyColumnLengths));*/
		this.modifyColumns = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_modifyColumns));
		this.addIndexes = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_addIndexes));
		this.dropIndexes = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_dropIndexes));
		this.modifyEngine = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_modifyEngine));
		this.modifyCharacterSetOrCollation = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_modifyCharacterSetOrCollation));

		String schemaUpdatePrefix = prefix.substring(0, prefix.indexOf('.'));
		String clientsToIgnore = DrPropertiesTool.getFirstOccurrence(multiProperties, schemaUpdatePrefix
				+ SUFFIX_ignoreClients);
		this.ignoreClients = DrStringTool.splitOnCharNoRegex(clientsToIgnore, ',');
		String tablesToIgnore = DrPropertiesTool.getFirstOccurrence(multiProperties, schemaUpdatePrefix
				+ SUFFIX_ignoreTables);
		this.ignoreTables = DrStringTool.splitOnCharNoRegex(tablesToIgnore, ',');
		return this;
	}


	/****************************** methods ******************************/

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

	public Boolean getCreateDatabases(){
		return createDatabases;
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

	public boolean getModifyCharacterSetOrCollation(){
		return modifyCharacterSetOrCollation;
	}

	public boolean schemaUpdateEnabled(){
		return schemaUpdateEnabled;
	}

}
