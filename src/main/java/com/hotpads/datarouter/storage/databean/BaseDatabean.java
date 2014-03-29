package com.hotpads.datarouter.storage.databean;

import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.storage.field.BaseFieldSet;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ClassTool;
import com.hotpads.util.core.ListTool;


@SuppressWarnings("serial")
public abstract class BaseDatabean<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseFieldSet<D>
implements Databean<PK,D>{
	
	public static final String DEFAULT_KEY_FIELD_NAME = "key";

	/********************** databean *********************************/
	
	@Override
	public String getDatabeanName() {
		return getClass().getSimpleName();
	}
	
	
	/*************************** fields ****************************/
	
	@Override
	public String getKeyFieldName(){
		return DEFAULT_KEY_FIELD_NAME;
	}
	
	@Override
	public boolean isFieldAware(){
		return false;
	}
	
	@Override
	public MySqlCharacterSet getCharacterSet(){
		return SqlTable.getDefaultCharacterSet();
	}
	
	@Override
	public MySqlCollation getCollation(){
		return SqlTable.getDefaultCollation();
	}
	
//	@Override
//	public Class<? extends Fielder<PK>> getKeyFielderClass(){
//		return getKeyClass();
//	}
	
//	@Override
//	public Fielder<PK> getKeyFielder(){
//		return getKey();
//	}

	@Override
	public List<Field<?>> getKeyFields(){
		return FieldTool.prependPrefixes(getKeyFieldName(), getKey().getFields());
	}

	@Override
	@Deprecated//always use a Fielder
	public List<Field<?>> getNonKeyFields(){
		return new LinkedList<Field<?>>();
//		throw new NotImplementedException("not implemented");
	}

	
	//generally unused method that allows databean to implement the DatabeanFielder interface
//	@Override
//	public List<Field<?>> getKeyFields(D databean){
//		return FieldTool.prependPrefixes(getKeyFieldName(), databean.getKey().getFields());
//	}

	//generally unused method that allows databean to implement the DatabeanFielder interface
//	@Override
//	public List<Field<?>> getNonKeyFields(D databean){
//		return databean.getNonKeyFields();
//	}

	@Override
	public List<Field<?>> getFields(){
		List<Field<?>> allFields = ListTool.createLinkedList();
		allFields.addAll(getKeyFields());
		allFields.addAll(getNonKeyFields());
		return allFields;
	}

	@Override
	public Object getFieldValue(String fieldName){
		return FieldTool.getFieldValue(getFields(), fieldName);
	}
	
	
	/*************************** stringification ******************************/

	@Override
	public String getPersistentString(){  //fuse multi-column field into one string, usually with "_" characters
		return getKey().getPersistentString();
	}
	
	@Override
	public String getTypedPersistentString(){  //usually getDatabeanName()+"."+getPersistentString()
		return getClass().getSimpleName()+"."+getPersistentString();
	}
	
	
	/************************ standard java *************************/
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj){
		if(ClassTool.differentClass(this, obj)){ return false; }
		Databean<PK,D> that = (Databean<PK,D>)obj;
		return this.getKey().equals(that.getKey());
	}	
	
	@Override
	public int hashCode(){
		return getKey().hashCode();
	}
	
	@Override
	public int compareTo(FieldSet that){
		if(!(that instanceof Databean)){
			return 1;//put databeans after non-databeans.  no good reason
		}
		Databean other = (Databean)that;
		return getKey().compareTo(other.getKey());
	}

	@Override
	public String toString(){
		return getClass().getSimpleName()+"."+getKey().getPersistentString();
	}
}
