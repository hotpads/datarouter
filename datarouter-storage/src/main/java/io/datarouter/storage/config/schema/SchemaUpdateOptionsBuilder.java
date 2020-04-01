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
import java.util.Properties;

public class SchemaUpdateOptionsBuilder{

	private final Properties properties;

	public SchemaUpdateOptionsBuilder(boolean schemaUpdateEnabled){
		properties = new Properties();
		properties.setProperty(SchemaUpdateOptions.SCHEMA_UPDATE_ENABLE, String.valueOf(schemaUpdateEnabled));
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteCreateDatabases(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_createDatabases);
		return this;
	}

	public SchemaUpdateOptionsBuilder disableSchemaUpdateExecuteCreateDatabases(){
		disableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_createDatabases);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintCreateDatabases(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_createDatabases);
		return this;
	}

	public SchemaUpdateOptionsBuilder disableSchemaUpdatePrintCreateDatabases(){
		disableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_createDatabases);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteCreateTables(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_createTables);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintCreateTables(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_createTables);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteAddColumns(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_addColumns);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintAddColumns(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_addColumns);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteDeleteColumns(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_deleteColumns);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintDeleteColumns(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_deleteColumns);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteModifyColumns(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_modifyColumns);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintModifyColumns(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_modifyColumns);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteAddIndexes(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_addIndexes);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintAddIndexes(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_addIndexes);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteDropIndexes(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_dropIndexes);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintDropIndexes(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_dropIndexes);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteModifyEngine(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_modifyEngine);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintModifyEngine(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_modifyEngine);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteModifyRowFormat(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_modifyRowFormat);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintModifyRowFormat(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_modifyRowFormat);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteModifyCharacterSetOrCollation(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_modifyCharacterSetOrCollation);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintModifyCharacterSetOrCollation(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_modifyCharacterSetOrCollation);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteModifyTtl(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_modifyTtl);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintModifyTtl(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_modifyTtl);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdateExecuteModifyMaxVersions(){
		enableSchemaUpdateExecutePrefixedOption(SchemaUpdateOptions.SUFFIX_modifyMaxVersions);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableSchemaUpdatePrintModifyMaxVersions(){
		enableSchemaUpdatePrintPrefixedOption(SchemaUpdateOptions.SUFFIX_modifyMaxVersions);
		return this;
	}

	public SchemaUpdateOptionsBuilder withSchemaUpdateExecuteIgnoreClients(List<String> clientsToIgnore){
		String ignoreClientsOptionKey = makeSchemaUpdateExecutePrefixedKey(SchemaUpdateOptions.SUFFIX_ignoreClients);
		String clientsToIgnoreCsv = String.join(",", clientsToIgnore);
		properties.setProperty(ignoreClientsOptionKey, clientsToIgnoreCsv);
		return this;
	}

	public SchemaUpdateOptionsBuilder withSchemaUpdatePrintIgnoreClients(List<String> clientsToIgnore){
		String ignoreClientsOptionKey = makeSchemaUpdatePrintPrefixedKey(SchemaUpdateOptions.SUFFIX_ignoreClients);
		String clientsToIgnoreCsv = String.join(",", clientsToIgnore);
		properties.setProperty(ignoreClientsOptionKey, clientsToIgnoreCsv);
		return this;
	}

	public SchemaUpdateOptionsBuilder withSchemaUpdateExecuteIgnoreTables(List<String> tablesToIgnore){
		String ignoreTablesOptionKey = makeSchemaUpdateExecutePrefixedKey(SchemaUpdateOptions.SUFFIX_ignoreTables);
		String tablesToIgnoreCsv = String.join(",", tablesToIgnore);
		properties.setProperty(ignoreTablesOptionKey, tablesToIgnoreCsv);
		return this;
	}

	public SchemaUpdateOptionsBuilder withSchemaUpdatePrintIgnoreTables(List<String> tablesToIgnore){
		String ignoreTablesOptionKey = makeSchemaUpdatePrintPrefixedKey(SchemaUpdateOptions.SUFFIX_ignoreTables);
		String tablesToIgnoreCsv = String.join(",", tablesToIgnore);
		properties.setProperty(ignoreTablesOptionKey, tablesToIgnoreCsv);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableAllSchemaUpdateExecuteOptions(){
		SchemaUpdateOptions.ALL_SCHEMA_UPDATE_OPTIONS
				.forEach(this::enableSchemaUpdateExecutePrefixedOption);
		return this;
	}

	public SchemaUpdateOptionsBuilder enableAllSchemaUpdatePrintOptions(){
		SchemaUpdateOptions.ALL_SCHEMA_UPDATE_OPTIONS
				.forEach(this::enableSchemaUpdatePrintPrefixedOption);
		return this;
	}

	public Properties build(){
		return properties;
	}

	private void enableSchemaUpdateExecutePrefixedOption(String option){
		String schemaUpdateExecutePrefixedOptionKey = makeSchemaUpdateExecutePrefixedKey(option);
		properties.setProperty(schemaUpdateExecutePrefixedOptionKey, String.valueOf(true));
	}

	private void disableSchemaUpdateExecutePrefixedOption(String option){
		String schemaUpdateExecutePrefixedOptionKey = makeSchemaUpdateExecutePrefixedKey(option);
		properties.setProperty(schemaUpdateExecutePrefixedOptionKey, String.valueOf(false));
	}

	private void enableSchemaUpdatePrintPrefixedOption(String option){
		String schemaUpdateExecutePrefixedOptionKey = makeSchemaUpdatePrintPrefixedKey(option);
		properties.setProperty(schemaUpdateExecutePrefixedOptionKey, String.valueOf(true));
	}

	private void disableSchemaUpdatePrintPrefixedOption(String option){
		String schemaUpdateExecutePrefixedOptionKey = makeSchemaUpdatePrintPrefixedKey(option);
		properties.setProperty(schemaUpdateExecutePrefixedOptionKey, String.valueOf(false));
	}

	private String makeSchemaUpdateExecutePrefixedKey(String option){
		return SchemaUpdateOptions.EXECUTE_PREFIX + option;
	}

	private String makeSchemaUpdatePrintPrefixedKey(String option){
		return SchemaUpdateOptions.PRINT_PREFIX + option;
	}

}
