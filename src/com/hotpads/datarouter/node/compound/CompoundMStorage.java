package com.hotpads.datarouter.node.compound;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.compound.readwrite.CompoundMapRWStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class CompoundMStorage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{

	protected Node<PK,D> node;
	protected CompoundMapRWStorage<PK,D> map;
	
	public CompoundMStorage(Node<PK,D> node){
		this.node = node;
		this.map = new CompoundMapRWStorage<PK,D>(node);
	}

	public CompoundMapRWStorage<PK,D> map(){
		return map;
	}
	

	public static class CompoundMNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends CompoundMStorage<PK,D>{
		public CompoundMNode(Node<PK,D> node){
			super(node);
		}
		public Node<PK,D> node(){
			return node;
		}
	}
}
