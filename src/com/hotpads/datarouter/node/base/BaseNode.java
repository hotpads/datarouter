package com.hotpads.datarouter.node.base;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseNode<PK extends PrimaryKey<PK>,D extends Databean<PK>,F extends DatabeanFielder<PK,D>> 
implements Node<PK,D>{
	protected Logger logger = Logger.getLogger(getClass());

	protected Class<PK> primaryKeyClass;
	protected PK samplePrimaryKey;
	protected Class<? super D> baseDatabeanClass;
	protected Class<D> databeanClass;
	protected D sampleDatabean;
	protected String name;
	
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
	
	public BaseNode(Class<D> databeanClass){
		this(databeanClass, null);
	}
	
	public BaseNode(Class<D> databeanClass, Class<F> fielderClass){
		this.baseDatabeanClass = databeanClass;
		this.databeanClass = databeanClass;
		this.sampleDatabean = ReflectionTool.create(databeanClass);
		this.primaryKeyClass = this.sampleDatabean.getKeyClass();
		this.samplePrimaryKey = ReflectionTool.create(primaryKeyClass);
		this.name = databeanClass.getSimpleName()+"."+this.getClass().getSimpleName();//probably never used
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
		}catch(Exception probablyNoPkInstantiated){
			if(this.sampleDatabean.isFieldAware()){
				throw new IllegalArgumentException("could not instantiate "+name, probablyNoPkInstantiated);
			}
		}
	}

	@Override
	public Class<PK> getPrimaryKeyType(){
		return this.primaryKeyClass;
	}
	
	@Override
	public Class<D> getDatabeanType() {
		return this.databeanClass;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	@Override
	public List<Field<?>> getFields(){
		return this.fields;
	}
	
	@Override
	public int compareTo(Node<PK,D> o){
		return ComparableTool.nullFirstCompareTo(getName(), o.getName());
	}
	
	@Override
	public List<Field<?>> getFields(D d){
		if(d==null){ return ListTool.createLinkedList(); }
		if(fielderClass==null){ return d.getFields(); }
		return ReflectionTool.create(fielderClass).getFields(d);
	}
}
