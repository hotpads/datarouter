package com.hotpads.data.exporter;

import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class ExportParameters<PK extends PrimaryKey<PK>,D extends Databean<PK, D>>{
	private final Class<D> databeanClass;
	private final Class<DatabeanFielder<PK, D>> fielderClass;
	private final String tableName;
	private final String columnNameCsv;
	private final int hoursToRedownload;
	private final boolean isHibernateTable;
	private final SortedStorageReader<PK, D> sortedStorageReader;

	public ExportParameters(Class<D> databeanClass, Class<DatabeanFielder<PK, D>> fielderClass, String tableName,
			String columnNameCsv, SortedStorageReader<PK, D> sortedStorageReader, int hoursToRedownload,
			boolean isHibernateTable){
		this.databeanClass = databeanClass;
		this.fielderClass = fielderClass;
		this.tableName = tableName;
		this.columnNameCsv = columnNameCsv;
		this.hoursToRedownload = hoursToRedownload;
		this.isHibernateTable = isHibernateTable;
		this.sortedStorageReader = sortedStorageReader;
	}

	public Class<D> getDatabeanClass(){
		return databeanClass;
	}

	public Class<DatabeanFielder<PK, D>> getFielderClass(){
		return fielderClass;
	}

	public String getTableName(){
		return tableName;
	}

	public String getColumnNameCsv(){
		return columnNameCsv;
	}

	public int getHoursToRedownload(){
		return hoursToRedownload;
	}

	public boolean isHibernateTable(){
		return isHibernateTable;
	}

	public SortedStorageReader<PK, D> getSortedStorageReader(){
		return sortedStorageReader;
	}

	@Override
	public String toString(){
		return "databeanClass=" + getDatabeanClass().getName() + "|fielderClass=" + getFielderClass().getName()
				+ "|tableName=" + getTableName() + "|isHibernateTable=" + isHibernateTable()
				+ "|columnNameCsv=" + getColumnNameCsv() + "|hoursToRedownload=" + getHoursToRedownload();
	}
}
