package com.hotpads.datarouter.client.imp.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.CollectionTool;

public class JdbcTool {
	
	public static int tryInsert(Connection conn, String sql) throws SQLException{
		PreparedStatement stmt = conn.prepareStatement(sql);
		try{
			return stmt.executeUpdate();
		}catch(SQLException e){
			//System.out.println(e.getMessage());
			return 0;
		}
	}

	public static int[] bulkUpdate(Connection conn, List<String> sql) throws SQLException{
		if(conn==null || CollectionTool.isEmpty(sql)){ return new int[]{}; }
		return bulkUpdate(conn, sql.toArray(new String[sql.size()]));
	}
	
	public static int[] bulkUpdate(Connection conn, String[] sql) throws SQLException{
		if(conn==null || ArrayTool.isEmpty(sql)){ return new int[]{}; }
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
