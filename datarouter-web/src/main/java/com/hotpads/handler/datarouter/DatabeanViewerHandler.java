package com.hotpads.handler.datarouter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.DatarouterNodes;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.serialize.PrimaryKeyStringConverter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.PrimaryKeyFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.Params;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.StringMav;

public class DatabeanViewerHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanViewerHandler.class);

	@Inject
	private DatarouterNodes datarouterNodes;


	/**
	 * e.g.
	 * https://localhost:8443/job/datarouter/data/stat/RentZestimate/1
	 * https://localhost:8443/job/datarouter/data/place/Area/14644
	 * https://localhost:8443/job/datarouter/data/search/Listing/1ParkPlace_0-130012758
	 * https://localhost:8443/job/datarouter/data/view/ModelIndexView16B/corporate_020121012112312100231132102_CHBO_8308
	 * https://localhost:8443/job/datarouter/data/event/TraceSpan/22858565159955332_0_1
	 *
	 */

	@Override
	protected Mav handleDefault() throws Exception{
		Mav mav = new Mav("/jsp/admin/viewDatabean.jsp");
		PathSegments pathSegments = PathSegments.parsePathSegments(params);
		List<MapStorageReaderNode<?,?>> nodes = getNodes(pathSegments.datarouterName, pathSegments.tableName);
		mav.put("nodes", nodes);
		List<DatabeanWrapper> databeanWrappers = new ArrayList<>();
		for(MapStorageReaderNode node : nodes){
			boolean fieldAware = true;
			List<Field<?>> fields = node.getFields();
			if(fields == null){
				fieldAware = false;
				fields = node.getFieldInfo().getPrimaryKeyFields();
			}
			PrimaryKey<?> key = decodePrimaryKey(node, pathSegments.databeanKey);
			Databean<?,?> databean = node.get(key, null);
			if(databean != null){
				databeanWrappers.add(new DatabeanWrapper(fields, getRowsOfFields(node, databean), node,
						fieldAware));
			}
		}
		if(databeanWrappers.size() > 0){
			mav.put("databeanWrappers", databeanWrappers);
			return mav;
		}
		return new StringMav("databean not found");
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

	private PrimaryKey<?> decodePrimaryKey(Node<?,?>node, String pkStrings){
		PrimaryKey<?> key = PrimaryKeyStringConverter.primaryKeyFromString((Class<PrimaryKey>)(node
				.getFieldInfo().getPrimaryKeyClass()), (PrimaryKeyFielder)(node.getFieldInfo()
						.getSamplePrimaryKey()), pkStrings);
		key.fromPersistentString(pkStrings);
		return key;
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
		private final String datarouterName;
		private final String tableName;
		private final String databeanKey;

		private PathSegments(String datarouterName, String tableName, String databeanKey){
			this.datarouterName = datarouterName;
			this.tableName = tableName;
			this.databeanKey = databeanKey;
		}

		public static PathSegments parsePathSegments(Params params){
			String pathInfoStr = params.getRequest().getPathInfo();
			int offset = DatarouterWebDispatcher.DATA.length() + 1;
			if(pathInfoStr.contains(DatarouterWebDispatcher.URL_DATAROUTER)){
				offset += DatarouterWebDispatcher.URL_DATAROUTER.length();
			}
			String[] pathInfo = params.getRequest().getPathInfo().substring(offset).split("/");
			if(pathInfo.length != 3){
				throw new IllegalArgumentException("The url is not correct! "
						+ "The correct url is: /datarouter/data/{router}/{table}/{databeanKey}");
			}

			return new PathSegments(pathInfo[0], pathInfo[1],  pathInfo[2]);

		}
	}

}
