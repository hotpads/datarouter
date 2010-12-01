package com.hotpads.datarouter.node.compound.readwrite;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class CompoundMapRWStorage<PK extends PrimaryKey<PK>,D extends Databean<PK>>{

	protected Node<PK,D> node;
	protected MapStorageReader<PK,D> reader;
	protected MapStorageWriter<PK,D> writer;
	
	public CompoundMapRWStorage(Node<PK,D> node){
		this.node = node;
		this.reader = BaseDataRouter.cast(node);
		this.writer = BaseDataRouter.cast(node);
	}

	public MapStorageReader<PK,D> reader(){
		return reader;
	}

	public MapStorageWriter<PK,D> writer(){
		return writer;
	}
	
	
	
	public static class CompoundMapRWNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends CompoundMapRWStorage<PK,D>{
		public CompoundMapRWNode(Node<PK,D> node){
			super(node);
		}
		public Node<PK,D> node(){
			return node;
		}
	}
	
	
}
