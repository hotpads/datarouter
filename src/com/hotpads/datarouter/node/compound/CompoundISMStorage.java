package com.hotpads.datarouter.node.compound;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.compound.readwrite.CompoundIndexedRWStorage;
import com.hotpads.datarouter.node.compound.readwrite.CompoundMapRWStorage;
import com.hotpads.datarouter.node.compound.readwrite.CompoundSortedRWStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class CompoundISMStorage<PK extends PrimaryKey<PK>,D extends Databean<PK>>{

	protected Node<PK,D> node;
	protected CompoundMapRWStorage<PK,D> map;
	protected CompoundSortedRWStorage<PK,D> sorted;
	protected CompoundIndexedRWStorage<PK,D> indexed;
	
	public CompoundISMStorage(Node<PK,D> node){
		this.node = node;
		this.map = new CompoundMapRWStorage<PK,D>(node);
		this.sorted = new CompoundSortedRWStorage<PK,D>(node);
		this.indexed = new CompoundIndexedRWStorage<PK,D>(node);
	}

	public CompoundMapRWStorage<PK,D> map(){
		return map;
	}

	public CompoundSortedRWStorage<PK,D> sorted(){
		return sorted;
	}

	public CompoundIndexedRWStorage<PK,D> indexed(){
		return indexed;
	}
	

	public static class CompoundISMNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends CompoundISMStorage<PK,D>{
		public CompoundISMNode(Node<PK,D> node){
			super(node);
		}
		public Node<PK,D> node(){
			return node;
		}
	}
	
}
