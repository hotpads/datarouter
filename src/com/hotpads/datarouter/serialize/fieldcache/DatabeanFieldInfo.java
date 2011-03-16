package com.hotpads.datarouter.serialize.fieldcache;

import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.java.ReflectionTool;

public class DatabeanFieldInfo<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> {
	
	protected Class<PK> primaryKeyClass;
	protected PK samplePrimaryKey;
	protected Class<? super D> baseDatabeanClass;
	protected Class<D> databeanClass;
	protected D sampleDatabean;
	
	protected Class<F> fielderClass;
	protected F sampleFielder;
//	protected Fielder<PK> samplePrimaryKeyFielder;

	protected boolean fieldAware;
	protected List<Field<?>> primaryKeyFields;
	protected List<Field<?>> nonKeyFields;
	protected List<Field<?>> fields;
	
	protected Map<String,Field<?>> fieldByMicroName = MapTool.createHashMap();
	
	protected List<String> fieldNames = ListTool.createArrayList();
	protected List<java.lang.reflect.Field> reflectionFields = ListTool.createArrayList();
	protected Map<String,java.lang.reflect.Field> reflectionFieldByName = MapTool.createHashMap();
	
	
	public DatabeanFieldInfo(String nodeName, Class<D> databeanClass, Class<F> fielderClass){
		this.baseDatabeanClass = databeanClass;
		this.databeanClass = databeanClass;
		this.sampleDatabean = ReflectionTool.create(databeanClass);
		this.primaryKeyClass = this.sampleDatabean.getKeyClass();
		this.samplePrimaryKey = ReflectionTool.create(primaryKeyClass);
		this.fielderClass = fielderClass;
		this.fieldAware = false;//mark true if PK and nonPk fields are found
		try{
			if(fielderClass==null){
				this.primaryKeyFields = this.samplePrimaryKey.getFields();
				this.fields = this.sampleDatabean.getFields();//make sure there is a PK or this will NPE
				for(Field<?> field : this.fields){
					this.fieldByMicroName.put(field.getName(), field);
					this.fieldNames.add(field.getName());
					java.lang.reflect.Field reflectionField = FieldTool.getReflectionFieldForField(sampleDatabean, field);
					this.reflectionFields.add(reflectionField);
					this.reflectionFieldByName.put(field.getName(), reflectionField);
				}
				this.nonKeyFields = this.sampleDatabean.getNonKeyFields();//only do these if the previous fields succeeded	
			}else{
				this.sampleFielder = ReflectionTool.create(fielderClass);
//				this.samplePrimaryKeyFielder = sampleFielder.getKeyFielder();
				this.primaryKeyFields = sampleFielder.getKeyFields(sampleDatabean);
				this.fields = sampleFielder.getFields(sampleDatabean);//make sure there is a PK or this will NPE
				for(Field<?> field : this.fields){
					this.fieldByMicroName.put(field.getName(), field);
					this.fieldNames.add(field.getName());
					java.lang.reflect.Field reflectionField = FieldTool.getReflectionFieldForField(sampleDatabean, field);
					this.reflectionFields.add(reflectionField);
					this.reflectionFieldByName.put(field.getName(), reflectionField);
				}
				this.nonKeyFields = this.sampleFielder.getNonKeyFields(sampleDatabean);//only do these if the previous fields succeeded	
			}
			this.fieldAware = this.sampleDatabean.isFieldAware();//only set this if all fields were setup properly
			FieldTool.cacheReflectionInfo(fields, sampleDatabean);
		}catch(Exception probablyNoPkInstantiated){
			if(this.sampleDatabean.isFieldAware()){
				throw new IllegalArgumentException("could not instantiate "+nodeName, probablyNoPkInstantiated);
			}
		}
	}
	
	
	/***************************** methods **************************************************/
	

	
	public List<Field<?>> getFields(D d){
		if(d==null){ return ListTool.createLinkedList(); }
		if(fielderClass==null){ return d.getFields(); }
		return ReflectionTool.create(fielderClass).getFields(d);
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


	public Map<String,Field<?>> getFieldByMicroName(){
		return fieldByMicroName;
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
	
	
	
}
