package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

public enum SqlAlterTypes{

	ADD_COLUMN, 
	DROP_COLUMN, 
	MODIFY, 
	DROP_TABLE,
	CREATE_TABLE,
	DROP_INDEX,
	ADD_INDEX,
	ADD_CONSTRAINT,
	DROP_CONSTRAINT,
	MODIFY_ENGINE,
	MODIFY_COLLATION,
	MODIFY_CHARACTER_SET
	;
}
