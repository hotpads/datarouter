package com.hotpads.datarouter.serialize.fieldcache;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.SimpleFieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.prefix.EmptyScatteringPrefix;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.java.ReflectionTool;

public class DatabeanFieldInfo<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> {
	static Logger logger = Logger.getLogger(DatabeanFieldInfo.class);
	
	protected Class<PK> primaryKeyClass;
	protected PK samplePrimaryKey;
	protected Class<? super D> baseDatabeanClass;
	protected Class<D> databeanClass;
	protected D sampleDatabean;
	protected String keyFieldName;
	protected List<List<Field<?>>> indexes; // !new! the indexes in the databean
	
	protected Class<? extends ScatteringPrefix> scatteringPrefixClass;
	protected ScatteringPrefix sampleScatteringPrefix;
	protected List<Field<?>> scatteringPrefixFields;
	
	protected Class<F> fielderClass;
	protected F sampleFielder;
//	protected Fielder<PK> samplePrimaryKeyFielder;

	protected boolean fieldAware;
	//these hold separate deep-copies of the fields
	protected List<Field<?>> primaryKeyFields;//no prefixes
	protected List<Field<?>> prefixedPrimaryKeyFields;//no prefixes
	protected List<Field<?>> nonKeyFields;
	protected List<Field<?>> fields;//PK fields will have prefixes in this Collection

	protected Map<String,Field<?>> nonKeyFieldByColumnName = MapTool.createHashMap();
	protected Map<String,Field<?>> fieldByColumnName = MapTool.createHashMap();
	protected Map<String,Field<?>> fieldByPrefixedName = MapTool.createHashMap();
	
	protected List<String> fieldNames = ListTool.createArrayList();
	protected List<java.lang.reflect.Field> reflectionFields = ListTool.createArrayList();
	protected Map<String,java.lang.reflect.Field> reflectionFieldByName = MapTool.createHashMap();
	
	
	public DatabeanFieldInfo(String nodeName, Class<D> databeanClass, Class<F> fielderClass){
		this.baseDatabeanClass = databeanClass;
		this.databeanClass = databeanClass;
		this.sampleDatabean = ReflectionTool.create(databeanClass);
		this.primaryKeyClass = this.sampleDatabean.getKeyClass();
		this.samplePrimaryKey = ReflectionTool.create(primaryKeyClass);
		this.keyFieldName = sampleDatabean.getKeyFieldName();
		this.fielderClass = fielderClass;
		this.fieldAware = this.sampleDatabean.isFieldAware();
		try{
			/*
			 * TODO remove duplicate logic below, but watch out for handling of non fieldAware databeans
			 */
			this.primaryKeyFields = samplePrimaryKey.getFields();
			this.prefixedPrimaryKeyFields = sampleDatabean.getKeyFields();			
			if(fielderClass==null){
				if(fieldAware){
					throw new IllegalArgumentException("could not instantiate "+nodeName
							+", fieldAware databean node must specify fielder class");
				}
				this.scatteringPrefixClass = EmptyScatteringPrefix.class;
			}else{
				this.sampleFielder = ReflectionTool.create(fielderClass);
				this.primaryKeyFields = sampleFielder.getKeyFielder().getFields(sampleDatabean.getKey());
				this.prefixedPrimaryKeyFields = sampleFielder.getKeyFields(sampleDatabean);
				
				if(fieldAware){
					this.fields = sampleFielder.getFields(sampleDatabean);//make sure there is a PK or this will NPE
					addFieldsToCollections();
					this.nonKeyFields = sampleFielder.getNonKeyFields(sampleDatabean);//only do these if the previous fields succeeded	
					addNonKeyFieldsToCollections();
					this.indexes=sampleFielder.getIndexes(sampleDatabean);
				}
				this.scatteringPrefixClass = sampleFielder.getScatteringPrefixClass();
			}
			if(fieldAware){
//				FieldTool.cacheReflectionInfo(scatteringPrefixFields, sampleScatterPrefix);
				FieldTool.cacheReflectionInfo(primaryKeyFields, samplePrimaryKey);
				FieldTool.cacheReflectionInfo(nonKeyFields, sampleDatabean);
				FieldTool.cacheReflectionInfo(fields, sampleDatabean);
			}
			this.sampleScatteringPrefix = ReflectionTool.create(scatteringPrefixClass);
			this.scatteringPrefixFields = sampleScatteringPrefix.getScatteringPrefixFields(samplePrimaryKey);
		}catch(Exception probablyNoPkInstantiated){
			throw new IllegalArgumentException("could not instantiate "+nodeName, probablyNoPkInstantiated);
		}
		assertAssertions();
	}
	
	
	/***************************** methods **************************************************/
	
	public FieldSet<?> getScatteringPrefixPlusPrimaryKey(PK key){
		return new SimpleFieldSet(getKeyFieldsWithScatteringPrefix(key));
	}
	
	public List<Field<?>> getKeyFieldsWithScatteringPrefix(PK key){
		List<Field<?>> fields = ListTool.createLinkedList();
		fields.addAll(sampleScatteringPrefix.getScatteringPrefixFields(key));
		if(key==null){ return fields; }
		fields.addAll(key.getFields());
		return fields;
	}
	
	public List<Field<?>> getNonKeyFields(D d){
		if(d==null){ return ListTool.createLinkedList(); }
		if(fielderClass==null){ return d.getFields(); }
		return ReflectionTool.create(fielderClass).getNonKeyFields(d);
	}
	
	protected void addNonKeyFieldsToCollections(){
		for(Field<?> field : IterableTool.nullSafe(nonKeyFields)){
			this.nonKeyFieldByColumnName.put(field.getColumnName(), field);
		}
	}
	
	protected void addFieldsToCollections(){
		for(Field<?> field : IterableTool.nullSafe(fields)){
			this.fieldByColumnName.put(field.getColumnName(), field);
			this.fieldByPrefixedName.put(field.getPrefixedName(), field);
			this.fieldNames.add(field.getName());
			java.lang.reflect.Field reflectionField = FieldTool.getReflectionFieldForField(sampleDatabean, field);
			this.reflectionFields.add(reflectionField);
			this.reflectionFieldByName.put(field.getName(), reflectionField);
		}
	}
	
	protected void assertAssertions(){
		if(fieldAware){
			if(CollectionTool.notEmpty(nonKeyFields)){
				if(!sampleDatabean.isFieldAware()){
					logger.warn("found nonKeyFields on non-fieldAware databean "+databeanClass.getName());
				}
			}
			for(Field<?> field : primaryKeyFields){
//				if(StringTool.notEmpty(field.getPrefix())){
//					logger.warn("found unusual prefix on primaryKeyField "+primaryKeyClass.getName());
//				}
			}
		}
	}

	
	/******************************** get/set **********************************************/

	public Class<PK> getPrimaryKeyClass(){
		return primaryKeyClass;
	}


	public PK getSamplePrimaryKey(){
		return samplePrimaryKey;
	}


	public Class<? super D> getBaseDatabeanClass(){
		return baseDatabeanClass;
	}
	
	


	public void setBaseDatabeanClass(Class<? super D> baseDatabeanClass){
		this.baseDatabeanClass = baseDatabeanClass;
	}


	public Class<D> getDatabeanClass(){
		return databeanClass;
	}


	public D getSampleDatabean(){
		return sampleDatabean;
	}


	public Class<F> getFielderClass(){
		return fielderClass;
	}


	public F getSampleFielder(){
		return sampleFielder;
	}


	public boolean getFieldAware(){
		return fieldAware;
	}


	public List<Field<?>> getPrimaryKeyFields(){
		return primaryKeyFields;
	}


	public List<Field<?>> getNonKeyFields(){
		return nonKeyFields;
	}


	public List<Field<?>> getFields(){
		return fields;
	}




	public Map<String,Field<?>> getNonKeyFieldByColumnName(){
		return nonKeyFieldByColumnName;
	}


	public Map<String,Field<?>> getFieldByColumnName(){
		return fieldByColumnName;
	}


	public List<String> getFieldNames(){
		return fieldNames;
	}


	public List<java.lang.reflect.Field> getReflectionFields(){
		return reflectionFields;
	}


	public Map<String,java.lang.reflect.Field> getReflectionFieldByName(){
		return reflectionFieldByName;
	}


	public Map<String,Field<?>> getFieldByPrefixedName(){
		return fieldByPrefixedName;
	}


	public String getKeyFieldName(){
		return keyFieldName;
	}


	public List<Field<?>> getPrefixedPrimaryKeyFields(){
		return prefixedPrimaryKeyFields;
	}


	public List<Field<?>> getScatteringPrefixFields() {
		return scatteringPrefixFields;
	}


	public ScatteringPrefix getSampleScatteringPrefix() {
		return sampleScatteringPrefix;
	}


	
	public List<List<Field<?>>> getIndexes() {
		return indexes;
	}


	
	
}
