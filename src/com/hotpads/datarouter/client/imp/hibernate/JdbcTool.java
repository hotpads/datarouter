package com.hotpads.datarouter.client.imp.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.hotpads.util.core.ArrayTool;

public class JdbcTool {

	
	public static int[] bulkUpdate(Connection conn, String[] sql) throws SQLException{
		int numStatements = ArrayTool.nullSafeLength(sql);
		if(numStatements < 1){ return null; }
		PreparedStatement stmt = conn.prepareStatement(sql[0]);
		stmt.addBatch();
		if(numStatements > 1){
			for(int i=1; i < sql.length; ++i){
				stmt.addBatch(sql[i]);
			}
		}
		int[] rowsUpdated = stmt.executeBatch();
		return rowsUpdated;
	}
	
}
