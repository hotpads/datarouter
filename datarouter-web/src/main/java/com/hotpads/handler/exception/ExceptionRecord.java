package com.hotpads.handler.exception;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.util.core.DrExceptionTool;
import com.hotpads.util.core.lang.ClassTool;
/**
 * The record of an Exception
 */
public class ExceptionRecord extends BaseDatabean<ExceptionRecordKey, ExceptionRecord> {


	private ExceptionRecordKey key;
	private Date created;
	private String serverName;
	private String stackTrace;
	private String type;

	public static class FieldKeys{
		public static final DateFieldKey created = new DateFieldKey("created");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
		public static final StringFieldKey stackTrace = new StringFieldKey("stackTrace")
				.withSize(MySqlColumnType.MAX_LENGTH_MEDIUMTEXT);
		public static final StringFieldKey type = new StringFieldKey("type");
	}

	public static class ExceptionRecordFielder extends BaseDatabeanFielder<ExceptionRecordKey, ExceptionRecord> {

		public ExceptionRecordFielder() {
			super(ExceptionRecordKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ExceptionRecord record) {
			return Arrays.asList(
					new DateField(FieldKeys.created, record.created),
					new StringField(FieldKeys.serverName, record.serverName),
					new StringField(FieldKeys.stackTrace, record.stackTrace),
					new StringField(FieldKeys.type, record.type));
		}

		@Override
		public MySqlRowFormat getRowFormat(){
			return MySqlRowFormat.COMPACT;
		}

	}

	/********************** construct ********************/

	public ExceptionRecord() {
		this.key = new ExceptionRecordKey();
	}

	public ExceptionRecord(String serverName, String stackTrace, String type) {
		this(System.currentTimeMillis(), serverName, stackTrace, type);
	}

	public ExceptionRecord(long dateMs, String serverName, String stackTrace, String type) {
		this.key = ExceptionRecordKey.generate();
		this.created = new Date(dateMs);
		this.serverName = serverName;
		this.stackTrace = stackTrace;
		this.type = type;
	}

	@Override
	public Class<ExceptionRecordKey> getKeyClass() {
		return ExceptionRecordKey.class;
	}

	/*************** getters / setters ******************/

	@Override
	public ExceptionRecordKey getKey() {
		return key;
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

	public String getStackTrace() {
		return stackTrace;
	}

	public String getColoredStackTrace() {
		return DrExceptionTool.getColorized(stackTrace);
	}

	public String getShortStackTrace() {
		return DrExceptionTool.getShortStackTrace(stackTrace);
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString(){
		return "ExceptionRecord(" + key + ", " + created + ", " + serverName + ", stackTrace(" + stackTrace.length()
				+ "))";
	}

	@Override
	public int compareTo(Databean<?, ?> that) {
		int diff = ClassTool.compareClass(this, that);
		if(diff != 0){
			return diff;
		}
		return created.compareTo(((ExceptionRecord)that).getCreated());
	}
}
