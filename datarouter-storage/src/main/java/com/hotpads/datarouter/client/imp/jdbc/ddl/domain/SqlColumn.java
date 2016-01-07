package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class SqlColumn implements Comparable<SqlColumn>{

	/********************** fields *************************/
	private static final int MAX_DATETIME_LENGTH = 19;
	private static final String NOT_NULL = " not null";

	private String name;
	private MySqlColumnType type;
	private Integer maxLength;
	private Boolean nullable;
	private Boolean autoIncrement;
	private String defaultValue;
	private MySqlCharacterSet characterSet;
	private MySqlCollation collation;

	/********************** construct **********************/

	//constructor that specifies the value to override the default value for the column
	public SqlColumn(String name, MySqlColumnType type, Integer maxLength, Boolean nullable, Boolean autoIncrement,
			String defaultValue, MySqlCharacterSet characterSet, MySqlCollation collation){
		this.name = name;
		this.type = type;
		this.maxLength = maxLength;
		this.nullable = nullable;
		this.autoIncrement = autoIncrement;
		this.defaultValue = defaultValue;
		this.characterSet = characterSet;
		this.collation = collation;
	}

	public SqlColumn(String name, MySqlColumnType type, Integer maxLength, Boolean nullable, Boolean autoIncrement,
			MySqlCharacterSet characterSet, MySqlCollation collation){
		this(name, type, maxLength, nullable, autoIncrement, null, characterSet, collation);
	}

	public SqlColumn(String name, MySqlColumnType type, Integer maxLength, Boolean nullable, Boolean autoIncrement){
		this(name, type, maxLength, nullable, autoIncrement, null, null);
	}

	public SqlColumn(String name, MySqlColumnType type){
		this(name, type, null, true, false);
	}


	/******************* Object methods **********************/

	@Override
	public String toString(){
		return "\t[" + name + ", " + type + ", " + maxLength + ", " + nullable +  ", " + autoIncrement + "]";
	}

	@Override
	public SqlColumn clone(){
		return new SqlColumn(name, type, maxLength, nullable, autoIncrement, defaultValue, characterSet, collation);
	}

	@Override
	public boolean equals(Object otherObject){
		if(!(otherObject instanceof SqlColumn)){
			return false;
		}
		// //return 0==compareTo((SqlColumn)otherObject);
		return 0 == new SqlColumnNameComparator(true).compare(this,(SqlColumn) otherObject);
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (maxLength == null?0:maxLength.hashCode());
		result = prime * result + (name == null?0:name.hashCode());
		result = prime * result + (nullable == null?0:nullable.hashCode());
		result = prime * result + (type == null?0:type.hashCode());
		result = prime * result + (autoIncrement == null?0:autoIncrement.hashCode());
		result = prime * result + (defaultValue == null?0:defaultValue.hashCode());
		return result;
	}


	/******************* comparator *************************/

	@Override
	public int compareTo(SqlColumn other){
		int diff = DrComparableTool.nullFirstCompareTo(name, other.name);
		if(diff!=0){
			return diff;
		}
		diff = DrComparableTool.nullFirstCompareTo(type, other.type);
		if(diff!=0){
			return diff;
		}
		diff = DrComparableTool.nullFirstCompareTo(maxLength, other.maxLength);
		if(diff!=0){
			return diff;
		}
		diff = DrComparableTool.nullFirstCompareTo(nullable, other.nullable);
		if(diff!=0){
			return diff;
		}
		diff = DrComparableTool.nullFirstCompareTo(autoIncrement, other.autoIncrement);
		return diff;
	}

	public static class SqlColumnNameComparator implements Comparator<SqlColumn>{
		private boolean caseSensitive = true;
		public SqlColumnNameComparator(boolean caseSensitive){
			this.caseSensitive = caseSensitive;
		}
		@Override
		public int compare(SqlColumn colA, SqlColumn colB){
			if(colA==null && colB==null){
				return 0;
			}
			if(colA==null){
				return -1;
			}
			if(colB==null){
				return 1;
			}
			if(caseSensitive){
				return DrComparableTool.nullFirstCompareTo(colA.name, colB.name);
			}
			return DrComparableTool.nullFirstCompareTo(DrStringTool.nullSafe(colA.name).toLowerCase(),
					DrStringTool.nullSafe(colB.name).toLowerCase());
		}
	}

	public static class SqlColumnNameTypeComparator extends SqlColumnNameComparator{
		public SqlColumnNameTypeComparator(boolean caseSensitive){
			super(caseSensitive);
		}
		@Override
		public int compare(SqlColumn colA, SqlColumn colB){
			if(colA==null && colB==null){
				return 0;
			}
			int diff = super.compare(colA, colB);
			if(diff!=0){
				return diff;
			}
			return DrComparableTool.nullFirstCompareTo(colA.type, colB.type);

		}
	}

	public static class SqlColumnNameTypeLengthAutoIncrementDefaultComparator extends SqlColumnNameTypeComparator{
		public SqlColumnNameTypeLengthAutoIncrementDefaultComparator(boolean caseSensitive){
			super(caseSensitive);
		}

		@Override
		public int compare(SqlColumn colA, SqlColumn colB){
			if(colA==null && colB==null){
				return 0;
			}
			int diff = super.compare(colA, colB);
			if(diff!=0){
				return diff;
			}
			diff = DrComparableTool.nullFirstCompareTo(colA.maxLength, colB.maxLength);
			if(diff!=0){
				// adding this case, so that a dateTime without specifying precision deosn't generate alter statements
				if(colA.type != MySqlColumnType.DATETIME ){
					return diff;
				}
				if(colB.maxLength < MAX_DATETIME_LENGTH){
					return diff;
				}
				if(colA.maxLength != 0){
					return diff;
				}
			}
			return DrComparableTool.nullFirstCompareTo(colA.autoIncrement, colB.autoIncrement);
		}
	}

	public static class SqlColumnCharsetCollationComparator implements Comparator<SqlColumn>{

		@Override
		public int compare(SqlColumn colA, SqlColumn colB){
			int diff = DrComparableTool.nullFirstCompareTo(colA.getCharacterSet(), colB.getCharacterSet());
			if(diff != 0){
				return diff;
			}
			return DrComparableTool.nullFirstCompareTo(colA.getCollation(), colB.getCollation());
		}

	}

	/******************* get/set ****************************/

	public String getName(){
		return name;
	}

	public MySqlColumnType getType(){
		return type;
	}

	public String getDefaultValue(){
		return defaultValue;
	}

	public Integer getMaxLength(){
		return maxLength;
	}

	public void setMaxLength(Integer maxLength){
		this.maxLength = maxLength;
	}

	public Boolean getNullable(){
		return nullable;
	}

	public void setNullable(Boolean nullable){
		this.nullable = nullable;
	}

	public MySqlCharacterSet getCharacterSet(){
		return characterSet;
	}

	public void setCharacterSet(MySqlCharacterSet characterSet){
		this.characterSet = characterSet;
	}

	public MySqlCollation getCollation(){
		return collation;
	}

	public void setCollation(MySqlCollation collation){
		this.collation = collation;
	}

	public final Boolean getAutoIncrement(){
		return autoIncrement;
	}

	public String getDefaultValueStatement(){
		if(!getNullable()){
			return NOT_NULL;
		}
		if(type.isDefaultValueSupported() && getDefaultValue() !=null){
			return " default " + getDefaultValue();
		}
		return "";
	}

	/******************* tests ***************************/

	public static class SqlColumnTests{
		@Test
		public void testCompareTo(){
			//two different values a, b
			SqlColumn columnA = new SqlColumn("a", MySqlColumnType.BIGINT, 19, false, false);
			SqlColumn columnB = new SqlColumn("b", MySqlColumnType.VARCHAR, 120, true, false);


			Assert.assertEquals(-1, columnA.compareTo(columnB));
			Assert.assertFalse(columnA.equals(columnB));

			//new value a2 which equals a
			SqlColumn a2 = new SqlColumn("a", MySqlColumnType.BIGINT, 19, false, false);
			Assert.assertTrue(a2.equals(columnA));
			Assert.assertFalse(a2==columnA);

			//test adding to SortedSet to test compareTo method
			SortedSet<SqlColumn> columns = new TreeSet<>();
			columns.add(columnB);
			columns.add(columnA);//should keep this version of a/a2 since it was added first
			columns.add(a2);
			List<SqlColumn> columnList = DrListTool.createArrayList(columns);
			Assert.assertTrue(columnA==columnList.get(0));//it kept the first version
			Assert.assertTrue(columnB==columnList.get(1));

			//test list sorting
			List<SqlColumn> sortedList = new ArrayList<>();
			sortedList.add(columnB);
			sortedList.add(columnA);
			Assert.assertTrue(columnB==sortedList.get(0));
			Collections.sort(sortedList);
			Assert.assertTrue(columnB==sortedList.get(1));
		}

		@Test
		public void testMinus(){
			List<SqlColumn> colA = Arrays.asList(new SqlColumn("a", MySqlColumnType.VARCHAR, 255, true, false));
			List<SqlColumn> colAAndB = Arrays.asList(
					new SqlColumn("a", MySqlColumnType.VARCHAR, 255, true, false),
					new SqlColumn("b", MySqlColumnType.VARCHAR, 250, false, false));
			Collection<SqlColumn> minus = DrCollectionTool.minus(colA, colAAndB);
			Assert.assertTrue(DrCollectionTool.isEmpty(minus));
		}

		@Test
		public void testComparators(){
			SqlColumn columnA = new SqlColumn("a", MySqlColumnType.BIGINT, 19, false, false);
			SqlColumn columnB = new SqlColumn("A", MySqlColumnType.VARCHAR, 120, true, false);
			Assert.assertTrue(new SqlColumnNameComparator(true).compare(columnA, columnB) != 0);
			Assert.assertTrue(new SqlColumnNameComparator(false).compare(columnA, columnB) == 0);

			Set<SqlColumn> caseSensitive = new TreeSet<>(new SqlColumnNameComparator(true));
			caseSensitive.add(columnA);
			caseSensitive.add(columnB);
			Assert.assertEquals(2, caseSensitive.size());

			Set<SqlColumn> caseInsensitive = new TreeSet<>(new SqlColumnNameComparator(false));
			caseInsensitive.add(columnA);
			caseInsensitive.add(columnB);
			Assert.assertEquals(1, caseInsensitive.size());
		}

		//TODO Test the auto-increment !
	}

}
