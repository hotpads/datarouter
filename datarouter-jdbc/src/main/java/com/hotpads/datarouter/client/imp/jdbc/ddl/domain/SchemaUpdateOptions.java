package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.List;
import java.util.Properties;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class SchemaUpdateOptions{

	private static final String
			SUFFIX_createDatabases = ".createDatabases",
			SUFFIX_createTables = ".createTables",
			SUFFIX_dropTables = ".dropTables",
			SUFFIX_addColumns = ".addColumns",
			SUFFIX_deleteColumns = ".deleteColumns",
			SUFFIX_modifyColumns = ".modifyColumns",
			SUFFIX_addIndexes = ".addIndexes",
			SUFFIX_dropIndexes = ".dropIndexes",
			SUFFIX_modifyEngine = ".modifyEngine",
			SUFFIX_ignoreClients = ".ignoreClients",
			SUFFIX_ignoreTables = ".ignoreTables",
			SUFFIX_modifyCharacterSetOrCollation = ".modifyCharacterSetOrCollation";

	private boolean createDatabases;
	private boolean createTables;
	private boolean dropTables;
	private boolean addColumns;
	private boolean deleteColumns;
	private boolean modifyColumns;
	private boolean addIndexes;
	private boolean dropIndexes;
	private boolean modifyEngine;
	private boolean modifyCharacterSetOrCollation;
	private List<String> ignoreClients;
	private List<String> ignoreTables;

	// set nothing, use in tests
	public SchemaUpdateOptions(){}

	public SchemaUpdateOptions(List<Properties> multiProperties, String prefix, boolean printVsExecute){
		if(printVsExecute){
			setSchemaUpdateWithPrintOptions(multiProperties,  prefix);
		}else{
			setSchemaUpdateWithExecuteOptions(multiProperties,  prefix);
		}
	}

	private SchemaUpdateOptions setSchemaUpdateWithPrintOptions(List<Properties> multiProperties, String prefix){
		createDatabases = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_createDatabases));
		createTables = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_createTables));
		dropTables = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_dropTables));
		addColumns = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_addColumns));
		deleteColumns = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_deleteColumns));
		modifyColumns = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_modifyColumns));
		addIndexes = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_addIndexes));
		dropIndexes = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_dropIndexes));
		modifyEngine = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_modifyEngine));
		modifyCharacterSetOrCollation = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties,
				prefix + SUFFIX_modifyCharacterSetOrCollation));
		return this;
	}

	private SchemaUpdateOptions setSchemaUpdateWithExecuteOptions(List<Properties> multiProperties, String prefix){
		//isTrue returns false as default and isTrueOrNull returns a true as default,
		//so on missing the setting in config, isTrueOrNull returns a default value true
		//createDatabase and createTables are set to default true to avoid confusions in developers machine
		//due to missing databases and tables
		createDatabases = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_createDatabases));
		createTables = DrBooleanTool.isTrueOrNull(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_createTables));

		//drop tables are always set to false for obvious reasons
		dropTables = false;

		//settings that modify an existing tables are returned with default true since they are less dangerous
		addColumns = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_addColumns));
		deleteColumns = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_deleteColumns));
		/*printModifyColumnLengths = BooleanTool.isTrueOrNull(PropertiesTool.getFirstOccurrence(multiProperties,
				prefix+SUFFIX_printModifyColumnLengths));*/
		modifyColumns = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_modifyColumns));
		addIndexes = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_addIndexes));
		dropIndexes = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_dropIndexes));
		modifyEngine = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_modifyEngine));
		modifyCharacterSetOrCollation = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties, prefix
				+ SUFFIX_modifyCharacterSetOrCollation));

		String schemaUpdatePrefix = prefix.substring(0, prefix.indexOf('.'));
		String clientsToIgnore = DrPropertiesTool.getFirstOccurrence(multiProperties, schemaUpdatePrefix
				+ SUFFIX_ignoreClients);
		ignoreClients = DrStringTool.splitOnCharNoRegex(clientsToIgnore, ',');
		String tablesToIgnore = DrPropertiesTool.getFirstOccurrence(multiProperties, schemaUpdatePrefix
				+ SUFFIX_ignoreTables);
		ignoreTables = DrStringTool.splitOnCharNoRegex(tablesToIgnore, ',');
		return this;
	}

	public SchemaUpdateOptions setAllTrue(){
		createTables = true;
		dropTables = true;
		addColumns = true;
		deleteColumns = true;
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
		modifyColumns = false;
		addIndexes = false;
		dropIndexes = false;
		modifyEngine = false;
		return this;
	}

	public Boolean getCreateTables(){
		return createTables;
	}

	public Boolean getCreateDatabases(){
		return createDatabases;
	}

	public Boolean getDropTables(){
		return dropTables;
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

	public Boolean getModifyColumns(){
		return modifyColumns;
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

	public Boolean getModifyEngine(){
		return modifyEngine;
	}

	public List<String> getIgnoreClients(){
		return ignoreClients;
	}

	public List<String> getIgnoreTables(){
		return ignoreTables;
	}

	public boolean getModifyCharacterSetOrCollation(){
		return modifyCharacterSetOrCollation;
	}

}
