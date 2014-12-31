package com.hotpads.datarouter.node.op.raw.read;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.handler.mav.Mav;

/**
 * Methods for reading from simple key/value storage systems, supporting similar methods to a HashMap.
 * 
 * There are many possible implementations such as a HashMap, a Guava cache, Ehcache, Memcached, JDBC, Hibernate, HBase,
 * DynamoDB, S3, Google Cloud Storage, etc.
 * 
 * @author mcorgan
 * 
 * @param <PK>
 * @param <D>
 */
public interface MapStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends NodeOps<PK,D>
{
	public static final String
		OP_exists = "exists",
		OP_get = "get",
		OP_getMulti = "getMulti",
		OP_getKeys = "getKeys";
	
	boolean exists(PK key, Config config);
	List<PK> getKeys(final Collection<PK> keys, final Config config);
	
	D get(PK key, Config config);
	List<D> getMulti(Collection<PK> keys, Config config);
	
	
		
	public interface MapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends Node<PK,D>, MapStorageReader<PK,D>
	{
	}	

	public interface PhysicalMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, MapStorageReaderNode<PK,D>
	{
	}
	
	public interface MapStorageReaderHttpNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{
		Mav exists();
		Mav get();
		Mav getMulti();
		Mav getKeys();
	}
}
