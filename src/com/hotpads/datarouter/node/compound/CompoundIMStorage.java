package com.hotpads.datarouter.node.compound;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.compound.readwrite.CompoundIndexedRWStorage;
import com.hotpads.datarouter.node.compound.readwrite.CompoundMapRWStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/*
 * IndexedMap storage would be for something like an index in MySQL 
 * 	with the data in Memcached or Cassandra (w/o OPP)... so, yes, an oddity
 * 
 */
public class CompoundIMStorage<PK extends PrimaryKey<PK>,D extends Databean<PK>>{

	protected Node<PK,D> node;
	protected CompoundMapRWStorage<PK,D> map;
	protected CompoundIndexedRWStorage<PK,D> indexed;
	
	public CompoundIMStorage(Node<PK,D> node){
		this.node = node;
		this.map = new CompoundMapRWStorage<PK,D>(node);
		this.indexed = new CompoundIndexedRWStorage<PK,D>(node);
	}

	public CompoundMapRWStorage<PK,D> map(){
		return map;
	}

	public CompoundIndexedRWStorage<PK,D> indexed(){
		return indexed;
	}
	

	public static class CompoundIMNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends CompoundIMStorage<PK,D>{
		public CompoundIMNode(Node<PK,D> node){
			super(node);
		}
		public Node<PK,D> node(){
			return node;
		}
	}
	
}
