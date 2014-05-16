package com.hotpads.handler.exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.util.UuidTool;
import com.hotpads.util.core.ClassTool;
import com.hotpads.util.core.ExceptionTool;
/**
 * The record of an Exception
 */
public class ExceptionRecord extends BaseDatabean<ExceptionRecordKey, ExceptionRecord> {

	private static int
		LENGTH_servName = MySqlColumnType.MAX_LENGTH_VARCHAR,
		LENGTH_stackTrace = MySqlColumnType.MAX_LENGTH_MEDIUMTEXT,
		LENGTH_type = MySqlColumnType.MAX_LENGTH_VARCHAR;

	/******************* fields ************************/
	
	private ExceptionRecordKey key;
	private Date created;
	private String serverName;
	private String stackTrace;
	private String type;

	public static class F {
		public static String
			id = "id",
			created = "created",
			serverName = "serverName",
			stackTrace = "stackTrace",
			type = "type";
	}

	public static class ExceptionRecordFielder extends BaseDatabeanFielder<ExceptionRecordKey, ExceptionRecord> {

		ExceptionRecordFielder() {}

		@Override
		public Class<? extends Fielder<ExceptionRecordKey>> getKeyFielderClass() {
			return ExceptionRecordKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(ExceptionRecord d) {
			return FieldTool.createList(
					new DateField(F.created, d.created),
					new StringField(F.serverName, d.serverName, LENGTH_servName),
					new StringField(F.stackTrace, d.stackTrace, LENGTH_stackTrace),
					new StringField(F.type, d.type, LENGTH_type)
					);
		}

	}

	/********************** construct ********************/

	ExceptionRecord() {
		key = new ExceptionRecordKey();
	}

	public ExceptionRecord(String serverName, String stackTrace, String type) {
		key = new ExceptionRecordKey(UuidTool.generateUuid());
		this.created = new Date();
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

	public String getColoredStackTrace() {
		return ExceptionTool.getColorized(stackTrace);
	}

	public String getShortStackTrace() {
		BufferedReader br = new BufferedReader(new StringReader(stackTrace));
		String line;
		String key = "com.hotpads";
		boolean none = false;
		int nb = 0;
		StringBuilder builder = new StringBuilder();
		try {
			while ((line = br.readLine()) != null) {
				if (line.contains(key) && nb < 10) {
					none = false;
					nb++;
					builder.append(line);
					builder.append('\n');
				} else {
					if (!none) {
						builder.append("[...]");
						builder.append('\n');
						none = true;
					}
				}
			}
			return ExceptionTool.getColorized(builder.toString());
		} catch (IOException e) {
			e.printStackTrace();
			return stackTrace;
		}
	}
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ExceptionRecord(" + key + ", " + created + ", " + serverName + ", stackTrace(" + stackTrace.length() + "))";
	}

	@Override
	public int compareTo(Databean<?, ?> that) {
		int diff = ClassTool.compareClass(this, that);
		if(diff != 0){ return diff; }
		return created.compareTo(((ExceptionRecord)that).getCreated());
	}
}
