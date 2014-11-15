package com.hotpads.datarouter.node.factory;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.compound.CompoundIMStorage.CompoundIMNode;
import com.hotpads.datarouter.node.compound.CompoundISMStorage.CompoundISMNode;
import com.hotpads.datarouter.node.compound.CompoundMStorage.CompoundMNode;
import com.hotpads.datarouter.node.compound.CompoundSMStorage.CompoundSMNode;
import com.hotpads.datarouter.node.compound.readwrite.CompoundIndexedRWStorage;
import com.hotpads.datarouter.node.compound.readwrite.CompoundMapRWStorage;
import com.hotpads.datarouter.node.compound.readwrite.CompoundSortedRWStorage;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class CompoundNodeFactory{
	
	private NodeFactory nodeFactory;
	
	
	@Inject
	public CompoundNodeFactory(NodeFactory nodeFactory){
		this.nodeFactory = nodeFactory;
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	CompoundMNode<PK,D> createAndRegisterMNode(
			String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		Node<PK,D> node = nodeFactory.create(clientName, databeanClass, router, true);
		router.register(node);
		return new CompoundMNode<PK,D>(node);
	}
	
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	CompoundSMNode<PK,D> createAndRegisterSMNode(
			String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		Node<PK,D> node = nodeFactory.create(clientName, databeanClass, router, true);
		router.register(node);
		return new CompoundSMNode<PK,D>(node);
	}
	
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	CompoundIMNode<PK,D> createAndRegisterIMNode(
			String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		Node<PK,D> node = nodeFactory.create(clientName, databeanClass, router, true);
		router.register(node);
		return new CompoundIMNode<PK,D>(node);
	}
	
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	CompoundISMNode<PK,D> createAndRegisterISMNode(
			String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		Node<PK,D> node = nodeFactory.create(clientName, databeanClass, router, true);
		router.register(node);
		return new CompoundISMNode<PK,D>(node);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	CompoundMapRWStorage<PK,D> createAndRegisterMap(
			String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		Node<PK,D> node = nodeFactory.create(clientName, databeanClass, router, true);
		router.register(node);
		return new CompoundMapRWStorage<PK,D>(node);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	CompoundSortedRWStorage<PK,D> createAndRegisterSorted(
			String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		Node<PK,D> node = nodeFactory.create(clientName, databeanClass, router, true);
		router.register(node);
		return new CompoundSortedRWStorage<PK,D>(node);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	CompoundIndexedRWStorage<PK,D> createAndRegisterIndexed(
			String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		Node<PK,D> node = nodeFactory.create(clientName, databeanClass, router, true);
		router.register(node);
		return new CompoundIndexedRWStorage<PK,D>(node);
	}

	
}
