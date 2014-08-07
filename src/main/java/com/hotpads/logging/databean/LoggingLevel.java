package com.hotpads.logging.databean;

import org.apache.logging.log4j.Level;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum LoggingLevel implements StringEnum<LoggingLevel>{
	ALL(Level.ALL),
	TRACE(Level.TRACE),
	DEBUG(Level.DEBUG),
	INFO(Level.INFO),
	WARN(Level.WARN),
	ERROR(Level.ERROR),
	OFF(Level.OFF),
	;

	private Level level;
	
	LoggingLevel(Level level){
		this.level = level;
	}

	public Level getLevel(){
		return level;
	}
	
	@Override
	public String getPersistentString(){
		return level.name();
	}

	@Override
	public LoggingLevel fromPersistentString(String s){
		return fromString(s);
	}

	public static LoggingLevel fromString(String s){
		return DataRouterEnumTool.getEnumFromString(values(), s, null);
	}

	public static int getSqlSize(){
		return 5;
	}

}
