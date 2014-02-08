package com.hotpads.datarouter.node.compound.readwrite;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public class CompoundSortedRWStorage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{

	protected Node<PK,D> node;
	protected SortedStorageReader<PK,D> reader;
	protected SortedStorageWriter<PK,D> writer;
	
	public CompoundSortedRWStorage(Node<PK,D> node){
		this.node = node;
		this.reader = BaseDataRouter.cast(node);
		this.writer = BaseDataRouter.cast(node);
	}

	public SortedStorageReader<PK,D> reader(){
		return reader;
	}

	public SortedStorageWriter<PK,D> writer(){
		return writer;
	}
	

	
	public static class CompoundSortedRWNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends CompoundSortedRWStorage<PK,D>{
		public CompoundSortedRWNode(Node<PK,D> node){
			super(node);
		}
		public Node<PK,D> node(){
			return node;
		}
	}
	
}
