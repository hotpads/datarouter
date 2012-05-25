package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;
import java.util.Properties;

import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.PropertiesTool;

public class SchemaUpdateOptions{
	
	public static final String 
			SUFFIX_printCreateTables = ".printCreateTables", 
			SUFFIX_printDropTables = ".printDropTables",
			SUFFIX_printAddColumns = ".printAddColumns", 
			SUFFIX_printDeleteColumns = ".printDeleteColumns",
			/*SUFFIX_printModifyColumnLengths = ".printModifyColumnLengths",*/
			SUFFIX_printModifyColumn = ".printModifyColumn",
			SUFFIX_printAddIndex = ".printAddIndex",
			SUFFIX_printDropIndex = ".printAddIndex";
	
	/*
	 * print the DDL with a message: "Please Execute: alter table xyz..."
	 */
	//default to true
	protected Boolean printCreateTables;
	protected Boolean printDropTables;
	protected Boolean printAddColumns;
	protected Boolean printDeleteColumns;
	/*protected Boolean printModifyColumnLengths;*/
	protected Boolean printModifyColumn;
	protected Boolean printAddIndex;
	protected Boolean printDropIndex;
	
	/*
	 * if these are true, we should still print the DDL, but with a message saying "Executing: alter table xyz..."
	 * 
	 * if any needed operation on a given table is disabled, then we should disable all execution for the table and 
	 * revert to printing
	 */
	//default to true
	protected Boolean executeCreateTables;
	//default to false
	protected Boolean executeDropTables;
	protected Boolean executeAddColumns;
	protected Boolean executeDeleteColumns;
	/*protected Boolean executeModifyColumnLengths;*/
	protected Boolean executeAddIndex;
	protected Boolean executeDropIndex;
	
	public SchemaUpdateOptions(){
	}
	
	public SchemaUpdateOptions(List<Properties> multiProperties, String prefix){		
		this.printCreateTables = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_printCreateTables));
		this.printDropTables = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_printDropTables));
		this.printAddColumns = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_printAddColumns));
		this.printDeleteColumns = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_printDeleteColumns));
		/*this.printModifyColumnLengths = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_printModifyColumnLengths));*/
		this.printModifyColumn = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_printModifyColumn));
		this.printAddIndex = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_printAddIndex));
		this.printDropIndex = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				prefix+SUFFIX_printDropIndex));
		
	}

	
	/****************************** methods ******************************/
/*	
	public boolean anyTrue(){
		return createTables | anyAlterTrue();
	}
	
	public boolean anyAlterTrue(){
		return dropTables | addColumns | deleteColumns | modify;
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

	public Boolean getPrintCreateTables() {
		return printCreateTables;
	}

	public void setPrintCreateTables(Boolean printCreateTables) {
		this.printCreateTables = printCreateTables;
	}

	public Boolean getPrintDropTables() {
		return printDropTables;
	}

	public void setPrintDropTables(Boolean printDropTables) {
		this.printDropTables = printDropTables;
	}

	public Boolean getPrintAddColumns() {
		return printAddColumns;
	}

	public void setPrintAddColumns(Boolean printAddColumns) {
		this.printAddColumns = printAddColumns;
	}

	public Boolean getPrintDeleteColumns() {
		return printDeleteColumns;
	}

	public void setPrintDeleteColumns(Boolean printDeleteColumns) {
		this.printDeleteColumns = printDeleteColumns;
	}

	/*
	public Boolean getPrintModifyColumnLengths() {
		return printModifyColumnLengths;
	}

	public void setPrintModifyColumnLengths(Boolean printModifyColumnLengths) {
		this.printModifyColumnLengths = printModifyColumnLengths;
	}
	 */
	
	public Boolean getPrintAddIndex() {
		return printAddIndex;
	}

	public void setPrintAddIndex(Boolean printAddIndex) {
		this.printAddIndex = printAddIndex;
	}

	public Boolean getPrintDropIndex() {
		return printDropIndex;
	}

	public void setPrintDropIndex(Boolean printDropIndex) {
		this.printDropIndex = printDropIndex;
	}

	public Boolean getExecuteCreateTables() {
		return executeCreateTables;
	}

	public void setExecuteCreateTables(Boolean executeCreateTables) {
		this.executeCreateTables = executeCreateTables;
	}

	public Boolean getExecuteDropTables() {
		return executeDropTables;
	}

	public void setExecuteDropTables(Boolean executeDropTables) {
		this.executeDropTables = executeDropTables;
	}

	public Boolean getExecuteAddColumns() {
		return executeAddColumns;
	}

	public void setExecuteAddColumns(Boolean executeAddColumns) {
		this.executeAddColumns = executeAddColumns;
	}

	public Boolean getExecuteDeleteColumns() {
		return executeDeleteColumns;
	}

	public void setExecuteDeleteColumns(Boolean executeDeleteColumns) {
		this.executeDeleteColumns = executeDeleteColumns;
	}
	
	/*
	public Boolean getExecuteModifyColumnLengths() {
		return executeModifyColumnLengths;
	}

	public void setExecuteModifyColumnLengths(Boolean executeModifyColumnLengths) {
		this.executeModifyColumnLengths = executeModifyColumnLengths;
	}
	*/
	
	public Boolean getExecuteAddIndex() {
		return executeAddIndex;
	}

	public void setExecuteAddIndex(Boolean executeAddIndex) {
		this.executeAddIndex = executeAddIndex;
	}

	public Boolean getExecuteDropIndex() {
		return executeDropIndex;
	}

	public void setExecuteDropIndex(Boolean executeDropIndex) {
		this.executeDropIndex = executeDropIndex;
	}

	public Boolean getPrintModifyColumn() {
		return printModifyColumn;
	}

	public void setPrintModifyColumn(Boolean printModifyColumn) {
		this.printModifyColumn = printModifyColumn;
	}

}
