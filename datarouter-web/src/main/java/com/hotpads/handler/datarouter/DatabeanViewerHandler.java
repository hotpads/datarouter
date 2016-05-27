package com.hotpads.handler.datarouter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.DatarouterNodes;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.PercentFieldCodec;
import com.hotpads.datarouter.util.PrimaryKeyPercentCodec;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.Params;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.StringMav;

public class DatabeanViewerHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanViewerHandler.class);

	@Inject
	private DatarouterNodes datarouterNodes;

	/*
	 * e.g.
	 * https://localhost:8443/job/datarouter/data/stat/RentZestimate/1
	 * https://localhost:8443/job/datarouter/data/place/Area/14644
	 * https://localhost:8443/job/datarouter/data/search/Listing/1ParkPlace_0-130012758
	 * https://localhost:8443/job/datarouter/data/view/ModelIndexView16B/corporate_020121012112312100231132102_CHBO_8308
	 * https://localhost:8443/job/datarouter/data/event/TraceSpan/22858565159955332_0_1
	 * https://localhost:8443/datarouter/data/userToken/UserItemSnapshot?userToken=nw6CbRpNHFKkZ_vt0eXYJftVsPHESiwmcPQ-OeO2QOs
	 * https://localhost:8443/job/datarouter/data/place/Area?id=14644
	 * https://localhost:8443/job/datarouter/data/search/Listing?feedId=HomeRentals&feedListingId=AL010091L
	 */

	@Override
	protected Mav handleDefault() throws Exception{
		Mav mav = new Mav("/jsp/admin/viewDatabean.jsp");
		PathSegments pathSegments = PathSegments.parsePathSegments(params);
		List<MapStorageReaderNode<?,?>> nodes = getNodes(pathSegments.routerName, pathSegments.tableName);
		if(nodes == null || nodes.size() < 1){
			throw new IllegalArgumentException("Can not find a matching table."
				+ "The correct url is: /ctx/datarouter/data/{router}/{table}/{databeanKey}");
		}
		mav.put("nodes", nodes);

		List<DatabeanWrapper> databeanWrappers = new ArrayList<>();
		for(MapStorageReaderNode node : nodes){
			boolean fieldAware = true;
			List<Field<?>> fields = node.getFields();
			if(fields == null){
				fieldAware = false;
				fields = node.getFieldInfo().getPrimaryKeyFields();
			}
			PrimaryKey<?> pk = PrimaryKeyPercentCodec.decode(node.getPrimaryKeyType(), pathSegments.encodedPk);
			Databean<?,?> databean = node.get(pk, null);
			if(databean != null){
				databeanWrappers.add(new DatabeanWrapper(fields, getRowsOfFields(node, databean), node, fieldAware));
			}
		}
		if(databeanWrappers.isEmpty()){
			return new StringMav("databean not found");
		}
		mav.put("databeanWrappers", databeanWrappers);
		return mav;
	}

	private	List<Field<?>> getRowsOfFields(Node<?,?> node, Databean<?,?> databean){
		DatabeanFielder fielder = node.getFieldInfo().getSampleFielder();
		if(fielder != null){
			return fielder.getFields(databean);
		}
		return null;
	}

	private List<MapStorageReaderNode<?,?>> getNodes(String routerName, String tableName){
		Collection<Node<?,?>> topLevelNodes = datarouterNodes.getTopLevelNodesByRouterName().get(routerName);
		List<MapStorageReaderNode<?,?>> nodes = new ArrayList<>();
		for(Node<?,?> topLevelNode : topLevelNodes){
			List<Node<?,?>> allTreeNodes = new ArrayList<>();
			allTreeNodes.add(topLevelNode);
			allTreeNodes.addAll(topLevelNode.getChildNodes());
			for(Node<?,?> node : allTreeNodes){
				String [] nodeNameSplit = node.getName().split("\\.");
				if(nodeNameSplit.length < 2){
					logger.error("There might be some problem with the nodeName, where nodeName = " + node.getName());
				}
				if(tableName.equalsIgnoreCase(nodeNameSplit[1])){
					if(!(node instanceof MapStorageReader<?,?>)){
						logger.error("Cannot browse non-MapStorageReader "
							+ node.getClass().getSimpleName());
					}else{
						nodes.add((MapStorageReaderNode<?,?>)node);
					}
				}
			}
		}
		return nodes;
	}


	public static class DatabeanWrapper{
		private final List<Field<?>> fields;
		private final List<Field<?>> rowOfFields;
		private final Node<?,?> node;
		private final boolean fieldAware;

		private DatabeanWrapper(List<Field<?>> fields, List<Field<?>> rowOfFields, Node<?,?> node, boolean fieldAware){
			this.fields = fields;
			this.rowOfFields = rowOfFields;
			this.node = node;
			this.fieldAware = fieldAware;
		}
		public List<Field<?>> getFields(){
			return fields;
		}
		public List<Field<?>> getRowOfFields(){
			return rowOfFields;
		}
		public Node<?,?> getNode(){
			return node;
		}
		public boolean getFieldAware(){
			return fieldAware;
		}
	}

	private static class PathSegments{
		public final String routerName;
		public final String tableName;
		public final String encodedPk;

		private PathSegments(String routerName, String tableName, String encodedPk){
			this.routerName = routerName;
			this.tableName = tableName;
			this.encodedPk = encodedPk;
		}

		public static PathSegments parsePathSegments(Params params){
			String pathInfoStr = params.getRequest().getPathInfo();
			int startTrim = pathInfoStr.indexOf(DatarouterWebDispatcher.PATH_data);
			int endTrim = startTrim + DatarouterWebDispatcher.PATH_data.length();
			++endTrim; //skip one more slash
			String pathParams = pathInfoStr.substring(endTrim);
			logger.warn(pathParams);
			List<String> tokens = Arrays.asList(pathParams.split(PercentFieldCodec.INTERNAL_SEPARATOR));
			List<String> pkTokens = tokens.subList(2, tokens.size());
			String encodedPk = pkTokens.stream().collect(Collectors.joining(PercentFieldCodec.INTERNAL_SEPARATOR));
			PathSegments pathSegments = new PathSegments(tokens.get(0), tokens.get(1), encodedPk);
			logger.warn(pathSegments.toString());
			return pathSegments;
		}

		@Override
		public String toString(){
			return "PathSegments [routerName=" + routerName + ", tableName=" + tableName + ", encodedPk=" + encodedPk
					+ "]";
		}

	}

}
