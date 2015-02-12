package com.hotpads.datarouter.storage.databean;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * A Databean is an atomic unit of serialization corresponding to a MySQL row or a Memcached item. Generally, all fields
 * of the databean are read from the datastore together even if some are not wanted, and they are written to the
 * datastore together even if some are not updated.
 * 
 * Every Databean has a single PrimaryKey which determines its uniqueness and ordering among other databeans of the same
 * type. This is determined by the hashCode(), equals(), and compareTo() methods in the PrimaryKey and should generally
 * not be modified.
 * 
 * While Databeans consist of more code than JDO or JPA style databeans, they add many rich features such as: a strongly
 * typed PrimaryKey, equality, ordering, automatic schema updating, the ability to define multiple Fielders for
 * different storage formats, and the ability to serialize to arbitrary formats like JSON, Memcached, or a file, or
 * HBase. Databeans are ususally the foundation of a project, and comprise a minority of the code so the trade-off is
 * usually worthwhile.
 * 
 * To create a databean you can copy/paste/modify a similar existing databean or use the web tool. If you prefer a
 * ligher-weight format, you can omit the Fielder definition and add Hibernate annotations, but this isn't recommended
 * as it's comparable work to define a Fielder.
 * 
 * @author mcorgan
 * 
 * @param <PK>
 * @param <D>
 */
public interface Databean<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends Comparable<Databean<?,?>>{

	String getDatabeanName();
	
	Class<PK> getKeyClass();
	String getKeyFieldName();
	PK getKey();
	
	List<Field<?>> getKeyFields();	
	
}
