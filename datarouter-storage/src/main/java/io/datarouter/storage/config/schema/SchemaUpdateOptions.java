/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.config.schema;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.config.properties.InternalConfigDirectory;
import io.datarouter.util.properties.PropertiesTool;
import io.datarouter.util.properties.TypedProperties;
import io.datarouter.util.string.StringTool;

@Singleton
public class SchemaUpdateOptions{
	private static final Logger logger = LoggerFactory.getLogger(SchemaUpdateOptions.class);

	private static final String SCHEMA_UPDATE_FILENAME = "schema-update.properties";

	private static final String SCHEMA_UPDATE = "schemaUpdate";

	public static final String SCHEMA_UPDATE_ENABLE = SCHEMA_UPDATE + ".enable";

	public static final String PRINT_PREFIX = SCHEMA_UPDATE + ".print";
	public static final String EXECUTE_PREFIX = SCHEMA_UPDATE + ".execute";

	public static final String SUFFIX_createDatabases = ".createDatabases";
	public static final String SUFFIX_createTables = ".createTables";
	public static final String SUFFIX_addColumns = ".addColumns";
	public static final String SUFFIX_deleteColumns = ".deleteColumns";
	public static final String SUFFIX_modifyColumns = ".modifyColumns";
	public static final String SUFFIX_addIndexes = ".addIndexes";
	public static final String SUFFIX_dropIndexes = ".dropIndexes";
	public static final String SUFFIX_modifyEngine = ".modifyEngine";
	public static final String SUFFIX_modifyRowFormat = ".modifyRowFormat";
	public static final String SUFFIX_modifyCharacterSetOrCollation = ".modifyCharacterSetOrCollation";
	public static final String SUFFIX_modifyTtl = ".modifyTtl";
	public static final String SUFFIX_modifyMaxVersions = ".modifyMaxVersions";
	public static final List<String> ALL_SCHEMA_UPDATE_OPTIONS = List.of(
			SUFFIX_createDatabases,
			SUFFIX_createTables,
			SUFFIX_addColumns,
			SUFFIX_deleteColumns,
			SUFFIX_modifyColumns,
			SUFFIX_addIndexes,
			SUFFIX_dropIndexes,
			SUFFIX_modifyEngine,
			SUFFIX_modifyRowFormat,
			SUFFIX_modifyCharacterSetOrCollation,
			SUFFIX_modifyTtl,
			SUFFIX_modifyMaxVersions);

	protected static final String SUFFIX_ignoreClients = ".ignoreClients";
	protected static final String SUFFIX_ignoreTables = ".ignoreTables";

	private final List<String> ignoreClients;
	private final List<String> ignoreTables;

	private TypedProperties properties;

	@Inject
	public SchemaUpdateOptions(
			SchemaUpdateOptionsFactory schemaUpdateOptionsFactory,
			InternalConfigDirectory internalConfigDirectory){
		Properties propertiesFromClass = schemaUpdateOptionsFactory.getInternalConfigDirectoryTypeSchemaUpdateOptions(
				internalConfigDirectory.get());
		properties = new TypedProperties(List.of(propertiesFromClass));
		if(!properties.getUnmodifiablePropertiesList().isEmpty()){
			logger.warn(
					"Got schema update properties from class {}",
					schemaUpdateOptionsFactory.getClass().getCanonicalName());
		}else{
			String configFileLocation = internalConfigDirectory.findConfigFile(SCHEMA_UPDATE_FILENAME);
			try{
				properties.addProperties(PropertiesTool.parse(configFileLocation));
				logger.warn("Got schema update properties from file {}", configFileLocation);
			}catch(Exception e){
				logger.warn("Error parsing file {}, using default schema-update options", configFileLocation, e);
			}
		}

		String clientsToIgnore = properties.getString(SCHEMA_UPDATE + SUFFIX_ignoreClients);
		ignoreClients = StringTool.splitOnCharNoRegex(clientsToIgnore, ',');
		String tablesToIgnore = properties.getString(SCHEMA_UPDATE + SUFFIX_ignoreTables);
		ignoreTables = StringTool.splitOnCharNoRegex(tablesToIgnore, ',');
	}

	public boolean getEnabled(){
		return isPropertyTrue(SCHEMA_UPDATE_ENABLE).orElse(false);
	}

	public Boolean getCreateDatabases(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_createDatabases).orElse(true);
	}

	public Boolean getCreateTables(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_createTables).orElse(true);
	}

	public Boolean getAddColumns(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_addColumns).orElse(printVsExecute);
	}

	public Boolean getDeleteColumns(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_deleteColumns).orElse(printVsExecute);
	}

	public Boolean getModifyColumns(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_modifyColumns).orElse(printVsExecute);
	}

	public Boolean getAddIndexes(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_addIndexes).orElse(printVsExecute);
	}

	public Boolean getDropIndexes(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_dropIndexes).orElse(printVsExecute);
	}

	public Boolean getModifyEngine(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_modifyEngine).orElse(printVsExecute);
	}

	public boolean getModifyCharacterSetOrCollation(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_modifyCharacterSetOrCollation).orElse(printVsExecute);
	}

	public boolean getModifyRowFormat(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_modifyRowFormat).orElse(printVsExecute);
	}

	public boolean getModifyTtl(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_modifyTtl).orElse(printVsExecute);
	}

	public boolean getModifyMaxVersions(boolean printVsExecute){
		return isPropertyTrue(printVsExecute, SUFFIX_modifyMaxVersions).orElse(printVsExecute);
	}

	public boolean getModifyPrimaryKey(boolean printVsExecute){
		return getAddIndexes(printVsExecute) && getDropIndexes(printVsExecute);
	}

	public List<String> getIgnoreClients(){
		return ignoreClients;
	}

	public List<String> getIgnoreTables(){
		return ignoreTables;
	}

	private Optional<Boolean> isPropertyTrue(boolean printVsExecute, String suffix){
		return isPropertyTrue(choosePrefix(printVsExecute) + suffix);
	}

	private Optional<Boolean> isPropertyTrue(String property){
		return Optional.ofNullable(properties.getBoolean(property));
	}

	private static String choosePrefix(boolean printVsExecute){
		return printVsExecute ? PRINT_PREFIX : EXECUTE_PREFIX;
	}

}
