package com.hotpads.salesforce.databean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.salesforce.dto.SalesforceAttributes;
import com.hotpads.util.core.ObjectTool;

public abstract class SalesforceDatabean{ //TODO extends BaseDatabean
	
	private transient SalesforceDatabeanKey key;
	private SalesforceAttributes attributes;
	private transient SortedSet<Field<?>> snapshotFields;
	
	public SalesforceDatabean(SalesforceDatabeanKey key){
		this.key = key;
		this.snapshot();
	}
	
	public SalesforceDatabeanKey getKey(){
		return key;
	}
	
	public void setKey(SalesforceDatabeanKey key){
		this.key = key;
	}
	
	public abstract Type getQueryResultType();
	public abstract TreeSet<Field<?>> getFields();

	public SalesforceAttributes getAttributes(){
		return attributes;
	}

	public void setAttributes(SalesforceAttributes attributes){
		this.attributes = attributes;
	}
	
	public List<Field<?>> getModifiedFields(){
		List<Field<?>> modifiedFields = new ArrayList<>();
		Iterator<Field<?>> currentFieldsIterator = getFields().iterator();
		Iterator<Field<?>> snapshotIterator = snapshotFields.iterator();
		while(currentFieldsIterator.hasNext() && snapshotIterator.hasNext()){
			Field<?> current = currentFieldsIterator.next();
			Field<?> snapshot = snapshotIterator.next();
			if(ObjectTool.notEquals(current.getColumnName(), snapshot.getColumnName())){
				throw new RuntimeException(getClass().getSimpleName() + "fields changed !");
			}
			if(ObjectTool.notEquals(current.getValue(), snapshot.getValue())){
				modifiedFields.add(current);
			}
		}
		return modifiedFields;
	}
	
	public void snapshot(){
		this.snapshotFields = getFields();
	}
	
}