package com.hotpads.datarouter.serialize.fielder;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;

/**
 * A Fielder is a mapping of java fields to the columns and cells in the storage repository. A Databean can contain
 * multiple Fielders, for example, to store a Date in different formats in two different tables, or to facilitate
 * migration from one table schema to another.
 * 
 * The Fielder is usually defined in the Databean as close to the java field definitions as possible. This is to reduce
 * the likelihood that someone will add a java field and forget to add it to all Fielders for the Databean.
 * 
 * Even though a Fielder is defined in a Databean, the Databean isn't aware of the Fielder until you map them together
 * in a Node.
 * 
 * @author mcorgan
 * 
 * @param <F>
 */
public interface Fielder<F extends FieldSet<F>>{

	public List<Field<?>> getFields(F fieldSet);
	
}
