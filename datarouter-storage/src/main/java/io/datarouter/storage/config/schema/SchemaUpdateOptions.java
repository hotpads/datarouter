/**
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

import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.properties.PropertiesTool;
import io.datarouter.util.string.StringTool;

@Singleton
public class SchemaUpdateOptions{
	private static final Logger logger = LoggerFactory.getLogger(SchemaUpdateOptions.class);

	private static final String SCHEMA_UPDATE_FILENAME = "schema-update.properties";

	private static final String SCHEMA_UPDATE = "schemaUpdate";
	private static final String SCHEMA_UPDATE_ENABLE = SCHEMA_UPDATE + ".enable";

	private static final String PRINT_PREFIX = SCHEMA_UPDATE + ".print";
	private static final String EXECUTE_PREFIX = SCHEMA_UPDATE + ".execute";

	private static final String SUFFIX_createDatabases = ".createDatabases";
	private static final String SUFFIX_createTables = ".createTables";
	private static final String SUFFIX_addColumns = ".addColumns";
	private static final String SUFFIX_deleteColumns = ".deleteColumns";
	private static final String SUFFIX_modifyColumns = ".modifyColumns";
	private static final String SUFFIX_addIndexes = ".addIndexes";
	private static final String SUFFIX_dropIndexes = ".dropIndexes";
	private static final String SUFFIX_modifyEngine = ".modifyEngine";
	private static final String SUFFIX_ignoreClients = ".ignoreClients";
	private static final String SUFFIX_ignoreTables = ".ignoreTables";
	private static final String SUFFIX_modifyRowFormat = ".modifyRowFormat";
	private static final String SUFFIX_modifyCharacterSetOrCollation = ".modifyCharacterSetOrCollation";

	private final List<String> ignoreClients;
	private final List<String> ignoreTables;
	private Properties properties;

	@Inject
	public SchemaUpdateOptions(DatarouterProperties datarouterProperties){
		String configFileLocation = datarouterProperties.findConfigFile(SCHEMA_UPDATE_FILENAME);
		try{
			properties = PropertiesTool.parse(configFileLocation);
		}catch(Exception e){
			logger.warn("error parsing {}, using default schema-update options", configFileLocation);
			properties = new Properties();
		}

		String clientsToIgnore = properties.getProperty(SCHEMA_UPDATE + SUFFIX_ignoreClients);
		ignoreClients = StringTool.splitOnCharNoRegex(clientsToIgnore, ',');
		String tablesToIgnore = properties.getProperty(SCHEMA_UPDATE + SUFFIX_ignoreTables);
		ignoreTables = StringTool.splitOnCharNoRegex(tablesToIgnore, ',');
	}

	private Optional<Boolean> isPropertyTrue(boolean printVsExecute, String suffix){
		return isPropertyTrue(choosePrefix(printVsExecute) + suffix);
	}

	private Optional<Boolean> isPropertyTrue(String property){
		return Optional.ofNullable(properties.getProperty(property))
				.map(BooleanTool::isTrue);
	}

	private static String choosePrefix(boolean printVsExecute){
		return printVsExecute ? PRINT_PREFIX : EXECUTE_PREFIX;
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

	public boolean getModifyPrimaryKey(boolean printVsExecute){
		return getAddIndexes(printVsExecute) && getDropIndexes(printVsExecute);
	}

	public List<String> getIgnoreClients(){
		return ignoreClients;
	}

	public List<String> getIgnoreTables(){
		return ignoreTables;
	}

}
