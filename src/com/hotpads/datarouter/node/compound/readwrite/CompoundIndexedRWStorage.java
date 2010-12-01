package com.hotpads.datarouter.node.compound.readwrite;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public class CompoundIndexedRWStorage<PK extends PrimaryKey<PK>,D extends Databean<PK>>{

	protected Node<PK,D> node;
	protected IndexedStorageReader<PK,D> reader;
	protected IndexedStorageWriter<PK,D> writer;
	
	public CompoundIndexedRWStorage(Node<PK,D> node){
		this.node = node;
		this.reader = BaseDataRouter.cast(node);
		this.writer = BaseDataRouter.cast(node);
	}

	public IndexedStorageReader<PK,D> reader(){
		return reader;
	}

	public IndexedStorageWriter<PK,D> writer(){
		return writer;
	}
	

	public static class CompoundIndexedRWNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends CompoundIndexedRWStorage<PK,D>{
		public CompoundIndexedRWNode(Node<PK,D> node){
			super(node);
		}
		public Node<PK,D> node(){
			return node;
		}
	}
	
}
