package com.hotpads.datarouter.node.compound;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.compound.readwrite.CompoundMapRWStorage;
import com.hotpads.datarouter.node.compound.readwrite.CompoundSortedRWStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class CompoundSMStorage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{

	protected Node<PK,D> node;
	protected CompoundMapRWStorage<PK,D> map;
	protected CompoundSortedRWStorage<PK,D> sorted;
	
	public CompoundSMStorage(Node<PK,D> node){
		this.node = node;
		this.map = new CompoundMapRWStorage<PK,D>(node);
		this.sorted = new CompoundSortedRWStorage<PK,D>(node);
	}

	public CompoundMapRWStorage<PK,D> map(){
		return map;
	}

	public CompoundSortedRWStorage<PK,D> sorted(){
		return sorted;
	}
	

	public static class CompoundSMNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends CompoundSMStorage<PK,D>{
		public CompoundSMNode(Node<PK,D> node){
			super(node);
		}
		public Node<PK,D> node(){
			return node;
		}
	}
}
