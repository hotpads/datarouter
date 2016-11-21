package com.hotpads.spark.data.downloaders;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.MapTool;

public class SparkTableParameters<PK extends PrimaryKey<PK>,D extends Databean<PK, D>> {
	private static final Logger logger = LoggerFactory.getLogger(SparkTableParameters.class);

	private final Class<D> databeanClass;
	private final Class<DatabeanFielder<PK, D>> fielderClass;
	private final String tableName;
	private final String columnNameCsv;
	private final int hoursToRedownload;
	private final boolean isHibernateTable;
	private final String outputCodec;
	private final SortedStorageReader<PK, D> sortedStorageReader;

	private String s3InputPath;
	private String hdfsPath;

	public SparkTableParameters(Class<D> databeanClass, Class<DatabeanFielder<PK, D>> fielderClass, String tableName,
			String columnNameCsv, SortedStorageReader<PK, D> sortedStorageReader, int hoursToRedownload){
		this(databeanClass, fielderClass, tableName, columnNameCsv, sortedStorageReader, hoursToRedownload, false,
				null);
	}

	public SparkTableParameters(Class<D> databeanClass, Class<DatabeanFielder<PK, D>> fielderClass, String tableName,
			String columnNameCsv, SortedStorageReader<PK, D> sortedStorageReader, int hoursToRedownload,
			boolean isHibernateTable, String outputCodec){
		this.databeanClass = databeanClass;
		this.fielderClass = fielderClass;
		this.tableName = tableName;
		this.columnNameCsv = columnNameCsv;
		this.hoursToRedownload = hoursToRedownload;
		this.isHibernateTable = isHibernateTable;
		this.outputCodec = outputCodec;
		this.sortedStorageReader = sortedStorageReader;
	}

	public String getTableName() {
		return tableName;
	}

	public String getColumnNameCsv() {
		return columnNameCsv;
	}

	public int getHoursToRedownload() {
		return hoursToRedownload;
	}

	@Override
	public String toString() {
		return "databeanClass=" + databeanClass.getName() + "|fielderClass=" + fielderClass.getName()
				+ "|tableName=" + tableName + "|isHibernateTable=" + isHibernateTable
				+ "|columnNameCsv=" + columnNameCsv + "|hoursToRedownload=" + hoursToRedownload
				+ "|s3InputPath=" + s3InputPath + "|hdfsPath=" + hdfsPath + "|outputCodec=" + outputCodec;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK, D>> SparkTableParameters<PK, D> fromString(
			String optionsAsString) {
		Map<String, String> fieldNameToValueMap = MapTool.getMapFromString(optionsAsString, "|", "=");
		Class<D> databeanClass;
		Class<DatabeanFielder<PK, D>> fielderClass;
		try {
			databeanClass = (Class<D>)Class.forName(fieldNameToValueMap.get("databeanClass"));
			fielderClass = (Class<DatabeanFielder<PK, D>>)Class.forName(fieldNameToValueMap.get("fielderClass"));
		}catch(ClassNotFoundException e) {
			logger.warn("Databean class {} not found", fieldNameToValueMap.get("databeanClass"), e);
			throw new RuntimeException(e);
		}
		SparkTableParameters<PK, D> sparkTableParameters = new SparkTableParameters<>(databeanClass, fielderClass,
				fieldNameToValueMap.get("tableName"), fieldNameToValueMap.get("columnNameCsv"), null,
				Integer.parseInt(fieldNameToValueMap.get("hoursToRedownload")),
				Boolean.parseBoolean(fieldNameToValueMap.get("isHibernateTable")),
				fieldNameToValueMap.get("outputCodec"));
		sparkTableParameters.setS3InputPath(fieldNameToValueMap.get("s3InputPath"));
		sparkTableParameters.setHdfsPath(fieldNameToValueMap.get("hdfsPath"));
		return sparkTableParameters;
	}

	public String getHdfsPath() {
		return hdfsPath;
	}

	public void setHdfsPath(String hdfsPath) {
		this.hdfsPath = hdfsPath;
	}

	public Class<D> getDatabeanClass() {
		return databeanClass;
	}

	public Class<DatabeanFielder<PK, D>> getFielderClass() {
		return fielderClass;
	}

	public void setS3InputPath(String s3InputPath) {
		this.s3InputPath = s3InputPath;
	}

	public String getS3InputPath() {
		return s3InputPath;
	}

	public boolean isHibernateTable() {
		return isHibernateTable;
	}

	public String getOutputCodec() {
		return outputCodec;
	}

	public SortedStorageReader<PK, D> getSortedStorageReader() {
		return sortedStorageReader;
	}
}
