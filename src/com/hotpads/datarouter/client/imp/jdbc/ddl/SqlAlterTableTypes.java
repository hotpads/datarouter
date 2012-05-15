package com.hotpads.datarouter.client.imp.jdbc.ddl;

public enum SqlAlterTableTypes {

	ADD_COLUMN, DROP_COLUMN, MODIFY, 
	DROP_TABLE // even if it's not an alter table statement
	, CREATE_TABLE 	// even if it's not an alter table statement
	;
}
