package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;

public interface SqlTableGenerator{

	SqlTable generate() throws Exception;
	
}
