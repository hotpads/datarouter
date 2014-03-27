package com.hotpads.handler.exception;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;

@SuppressWarnings("serial")
public class ExceptionRecord extends BaseDatabean<ExceptionRecordKey, ExceptionRecord> {

	public static String
	COL_id = "id",
	COL_created = "created",
	COL_serverName = "serverName",
	COL_stackTrace = "stackTrace";
	public static int
	LENGTH_id = MySqlColumnType.MAX_LENGTH_VARCHAR,
	LENGTH_servName = MySqlColumnType.MAX_LENGTH_VARCHAR,
	LENGTH_stackTrace = MySqlColumnType.MAX_LENGTH_MEDIUMTEXT;

	@Id
	@Column(nullable = false)
	private ExceptionRecordKey key;
	private Date created;
	private String serverName;
	private String stackTrace;

	ExceptionRecord() {
		this(null, null);
	}

	public ExceptionRecord(String serverName, String stackTrace) {
		key = new ExceptionRecordKey(generateUUID());
		this.created = new Date();
		this.serverName = serverName;
		this.stackTrace = stackTrace;
	}

	@Override
	public boolean isFieldAware() {
		return true;
	}

	@Override
	public List<Field<?>> getNonKeyFields() {
		return FieldTool.createList(
				new DateField(COL_created, created),
				new StringField(COL_serverName, serverName, LENGTH_servName),
				new StringField(COL_stackTrace, stackTrace, LENGTH_stackTrace)
				);
	}

	public static class ExceptionRecordFielder extends BaseDatabeanFielder<ExceptionRecordKey, ExceptionRecord> {

		ExceptionRecordFielder() {

		}

		@Override
		public Class<? extends Fielder<ExceptionRecordKey>> getKeyFielderClass() {
			return ExceptionRecordKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(ExceptionRecord databean) {
			return databean.getNonKeyFields();
		}

	}

	private static String generateUUID() {
		EthernetAddress addr = EthernetAddress.fromInterface();
		TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(addr);
		UUID uuid = uuidGenerator.generate();
		return uuid.toString();
	}

	@Override
	public Class<ExceptionRecordKey> getKeyClass() {
		return ExceptionRecordKey.class;
	}

	@Override
	public ExceptionRecordKey getKey() {
		return key;
	}

	public void setKey(ExceptionRecordKey key) {
		this.key = key;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
}
