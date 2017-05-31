package com.hotpads.datarouter.client.imp.mysql.ddl.domain;

import java.util.Optional;

public interface MySqlCharacterSetCollationOpt{
	default Optional<MySqlCharacterSet> getCharacterSetOpt(){
		return Optional.empty();
	}

	default Optional<MySqlCollation> getCollationOpt(){
		return Optional.empty();
	}
}
