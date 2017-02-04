package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.test.SqlTableMocks;

/******************** tests *************************/

public class SqlCreateTableGeneratorTests{

	@Test
	public void test(){
		SqlCreateTableGenerator generator = new SqlCreateTableGenerator(SqlTableMocks.ABERFELDY);
		String expected = "create table Aberfeldy (\n"
				+ " kwilu varchar(255) not null,\n"
				+ " safari varchar(255) not null,\n"
				+ " savanna text not null,\n"
				+ " primary key (kwilu),\n"
				+ " index Bombora (safari)) "
				+ "engine=INNODB character set = utf8mb4 collate utf8mb4_bin row_format = Dynamic;";
		Assert.assertEquals(generator.generateDdl(), expected);
	}

	@Test
	public void testWithDatabaseName(){
		SqlCreateTableGenerator generator = new SqlCreateTableGenerator(SqlTableMocks.ABERFELDY, "ableforth");
		String expected = "create table ableforth.Aberfeldy (\n"
				+ " kwilu varchar(255) not null,\n"
				+ " safari varchar(255) not null,\n"
				+ " savanna text not null,\n"
				+ " primary key (kwilu),\n"
				+ " index Bombora (safari)) "
				+ "engine=INNODB character set = utf8mb4 collate utf8mb4_bin row_format = Dynamic;";
		Assert.assertEquals(generator.generateDdl(), expected);
	}

	@Test
	public void testWithAutoIncrement(){
		SqlCreateTableGenerator generator = new SqlCreateTableGenerator(SqlTableMocks.ARDMORE);
		String expected = "create table Ardmore (\n"
				+ " vidzar bigint(8) not null auto_increment\n"
				+ ") engine=INNODB character set = utf8mb4 collate utf8mb4_unicode_ci row_format = Dynamic;";
		Assert.assertEquals(generator.generateDdl(), expected);
	}

	@Test
	public void testWithCompositePrimaryKey(){
		SqlCreateTableGenerator generator = new SqlCreateTableGenerator(SqlTableMocks.ARDNAMURCHAN);
		String expected = "create table Ardnamurchan (\n"
				+ " kwilu varchar(255) not null,\n"
				+ " vidzar bigint(8) not null auto_increment,\n"
				+ " charrette varchar(255) not null,\n"
				+ " primary key (kwilu,vidzar),\n"
				+ " unique index Downunder (vidzar, kwilu),\n"
				+ " index Downunder (vidzar, kwilu),\n"
				+ " index Cooranbong (charrette)"
				+ ") engine=INNODB character set = utf8mb4 collate utf8mb4_unicode_ci row_format = Dynamic;";
		Assert.assertEquals(generator.generateDdl(), expected);
	}

}