package com.hotpads.datarouter.node.compound.read;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class CompoundSMReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{

	protected Node<PK,D> node;
	
	public CompoundSMReader(Node<PK,D> node){
		this.node = node;
	}

	public MapStorageReader<PK,D> map(){
		return BaseDataRouter.cast(node);
	}

	public SortedStorageReader<PK,D> sorted(){
		return BaseDataRouter.cast(node);
	}
	

	public static class CompoundSMReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends CompoundSMReader<PK,D>{
		public CompoundSMReaderNode(Node<PK,D> node){
			super(node);
		}
		public MapStorageReaderNode<PK,D> node(){
			return BaseDataRouter.cast(node);
		}

		public SortedStorageReaderNode<PK,D> sorted(){
			return BaseDataRouter.cast(node);
		}
	}
}
