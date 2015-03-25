package com.hotpads.datarouter.config;

import java.util.HashSet;
import java.util.Set;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum PutMethod implements IntegerEnum<PutMethod>, StringEnum<PutMethod>{

	SELECT_FIRST_OR_LOOK_AT_PRIMARY_KEY(20, "selectFirstOrLookAtPrimaryKey"),   //"pessimistic", slow but sure
	UPDATE_OR_INSERT(20, "updateOrInsert"),  //"optimistic" when rows are usually there (Events use this)
	INSERT_OR_UPDATE(21, "insertOrUpdate"),  // will overwrite whatever's there 
	INSERT_OR_BUST(22, "insertOrBust"),
	UPDATE_OR_BUST(23, "updateOrBust"),
	MERGE(24, "merge"),//use when the object could be on the session already in a different instance with the same identifier
	INSERT_IGNORE(25, "insertIgnore"),
	INSERT_ON_DUPLICATE_UPDATE(26, "insertOnDuplicateUpdate");
	
	//need to flush immediately so we can catch insert/update exceptions if they are thrown, 
	//   otherwise the exception will ruin the whole batch
	public static Set<PutMethod> METHODS_TO_FLUSH_IMMEDIATELY = new HashSet<>();
	static{
		METHODS_TO_FLUSH_IMMEDIATELY.add(UPDATE_OR_INSERT);
		METHODS_TO_FLUSH_IMMEDIATELY.add(INSERT_OR_UPDATE);
	}
	
	
	/********************** fields *****************************/
	
	private int persistentInteger;
	private String persistentString;
	
	
	/******************** construct *************************/
	
	private PutMethod(int persistentInteger, String persistentString){
		this.persistentInteger = persistentInteger;
		this.persistentString = persistentString;
	}

	/***************************** IntegerEnum methods ******************************/
	
	@Override
	public Integer getPersistentInteger(){
		return persistentInteger;
	}
	
	@Override
	public PutMethod fromPersistentInteger(Integer i){
		return DatarouterEnumTool.getEnumFromInteger(values(), i, null);
	}
	
	
	/****************************** StringEnum methods *********************************/
	
	@Override
	public String getPersistentString(){
		return persistentString;
	}
	
	@Override
	public PutMethod fromPersistentString(String s){
		return DatarouterEnumTool.getEnumFromString(values(), s, null);
	}
}
