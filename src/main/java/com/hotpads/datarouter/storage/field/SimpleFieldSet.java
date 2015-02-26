package com.hotpads.datarouter.storage.field;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;

@SuppressWarnings("serial")
public class SimpleFieldSet<F extends FieldSet<F>> 
extends BaseFieldSet<F>{

	protected List<Field<?>> fields = DrListTool.createArrayList();
	
	public SimpleFieldSet(){
	}
	
	public SimpleFieldSet(Collection<Field<?>> fields){
		add(fields);
	}
	
	public void add(Field<?> field){
		if(field==null){ return; }
		this.fields.add(field);
	}

	public SimpleFieldSet<?> add(Collection<Field<?>> fields){
		for(Field<?> field : DrCollectionTool.nullSafe(fields)){
			this.add(field);
		}
		return this;
	}
	
	@Override
	public List<Field<?>> getFields(){
		return fields;
	}
	
	public Field<?> getFirst(){
		return DrCollectionTool.getFirst(fields);
	}
	
}
