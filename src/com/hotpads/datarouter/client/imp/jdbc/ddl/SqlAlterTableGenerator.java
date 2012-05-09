package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

public class SqlAlterTableGenerator{

	protected SqlTable current, requested;

	public SqlAlterTableGenerator(SqlTable current, SqlTable requested){
		this.current = current;
		this.requested = requested;
	}
	
	public String generate() {
		String s="ALTER TABLE `" + current.getName()+"` (\n"; 
		//TODO everything
		
		// creating the sqlTableDiffGenerator
		SqlTableDiffGenerator diff = new SqlTableDiffGenerator(current,requested,true);

		// get the columns to add and the columns to remove
		List<SqlColumn> colsToAdd = diff.getColumnsToAdd(),
						colsToremove = diff.getColumnsToRemove();

		// get the other modifications ( the indexes )
		List<SqlIndex> indexesToAdd = diff.getIndexesToAdd(),
						 indexesToRemove = diff.getIndexesToRemove();
		
		// generate the alter table statements from columns to add and to remove
		s+=getAlterTableForAddingColumns(colsToAdd);
		// append them all into s
		return s;
	}

	private String getAlterTableForAddingColumns(List<SqlColumn> colsToAdd) {
		// TODO Auto-generated method stub
		String s="ALTER TABLE " + current.getName() + " \n" + "ADD ( ";
		for(SqlColumn col:colsToAdd){
			s+= col.getName() + " " + col.getType().toString().toLowerCase();
			if(col.getMaxLength()!=null){
				s+="(" + col.getMaxLength() + ") ";
			}
			if(col.getNullable()){
				s+=" DEFAULT NULL";
			}
			else{
				s+=" NOT NULL";
			}
			s+=",\n";//
		}
		s = s.substring(0, s.length()-2); // remove the last "," 
		s+=")\n";
		return s;
	}
	
	
}
