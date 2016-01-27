package com.hotpads.datarouter.config;

import java.util.HashSet;
import java.util.Set;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum PutMethod implements IntegerEnum<PutMethod>, StringEnum<PutMethod>{

	SELECT_FIRST_OR_LOOK_AT_PRIMARY_KEY(20, "selectFirstOrLookAtPrimaryKey", false),   //"pessimistic", slow but sure
	UPDATE_OR_INSERT(20, "updateOrInsert", false),  //"optimistic" when rows are usually there (Events use this)
	INSERT_OR_UPDATE(21, "insertOrUpdate", false),  // will overwrite whatever's there 
	INSERT_OR_BUST(22, "insertOrBust", true),
	UPDATE_OR_BUST(23, "updateOrBust", true),
	//use when the object could be on the session already in a different instance with the same identifier
	MERGE(24, "merge", false),
	INSERT_IGNORE(25, "insertIgnore", false),
	INSERT_ON_DUPLICATE_UPDATE(26, "insertOnDuplicateUpdate", false);
	
	//need to flush immediately so we can catch insert/update exceptions if they are thrown, 
	//   otherwise the exception will ruin the whole batch
	public static Set<PutMethod> METHODS_TO_FLUSH_IMMEDIATELY = new HashSet<>();
	static{
		METHODS_TO_FLUSH_IMMEDIATELY.add(UPDATE_OR_INSERT);
		METHODS_TO_FLUSH_IMMEDIATELY.add(INSERT_OR_UPDATE);
	}
	
	public static final PutMethod DEFAULT_PUT_METHOD = PutMethod.INSERT_ON_DUPLICATE_UPDATE;
	
	private int persistentInteger;
	private String persistentString;
	private boolean shouldAutoCommit;
	
	private PutMethod(int persistentInteger, String persistentString, boolean shouldAutoCommit){
		this.persistentInteger = persistentInteger;
		this.persistentString = persistentString;
		this.shouldAutoCommit = shouldAutoCommit;
	}
	
	public boolean getShouldAutoCommit(){
		return shouldAutoCommit;
	}

	/***************************** IntegerEnum methods ******************************/
	
	@Override
	public Integer getPersistentInteger(){
		return persistentInteger;
	}
	
	@Override
	public PutMethod fromPersistentInteger(Integer integer){
		return DatarouterEnumTool.getEnumFromInteger(values(), integer, null);
	}
	
	
	/****************************** StringEnum methods *********************************/
	
	@Override
	public String getPersistentString(){
		return persistentString;
	}
	
	@Override
	public PutMethod fromPersistentString(String string){
		return DatarouterEnumTool.getEnumFromString(values(), string, null);
	}
}
