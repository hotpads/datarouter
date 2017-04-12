package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.util.core.stream.StreamTool;

public class SqlIndex{

	private final String name;
	private final List<String> columnNames;

	public SqlIndex(String name, List<SqlColumn> columns){
		this.name = name;
		this.columnNames = StreamTool.map(columns, SqlColumn::getName);
	}

	public String getName(){
		return name;
	}

	public List<String> getColumnNames(){
		return columnNames;
	}

	@Override
	public int hashCode(){
		return Objects.hash(name, columnNames);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(!(obj instanceof SqlIndex)){
			return false;
		}
		SqlIndex other = (SqlIndex)obj;
		return Objects.equals(name, other.name)
				&& Objects.equals(columnNames, other.columnNames);
	}

	public static SqlIndex createPrimaryKey(List<SqlColumn> columns){
		return new SqlIndex("PRIMARY", columns);
	}

	public static class SqlIndexTests{
		@Test
		public void testEquals(){
			SqlColumn columnA = new SqlColumn("a", MySqlColumnType.BIGINT);
			SqlColumn columnB = new SqlColumn("b", MySqlColumnType.BIGINT);
			SqlColumn aa = new SqlColumn("a", MySqlColumnType.VARBINARY);
			SqlColumn bb = new SqlColumn("b", MySqlColumnType.VARCHAR);
			SqlIndex index1 = new SqlIndex("index", Arrays.asList(columnA, columnB));
			SqlIndex index2 = new SqlIndex("index", Arrays.asList(aa, bb));
			Assert.assertEquals(index1, index2);
		}
	}

}
