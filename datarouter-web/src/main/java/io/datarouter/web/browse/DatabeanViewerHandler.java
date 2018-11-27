/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.web.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.model.util.PercentFieldCodec;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodec;
import io.datarouter.util.OptionalTool;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.StringMav;
import io.datarouter.web.handler.params.Params;

public class DatabeanViewerHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanViewerHandler.class);

	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private DatarouterWebFiles files;

	@Handler(defaultHandler = true)
	public Mav view() throws Exception{
		Mav mav = new Mav(files.jsp.admin.viewDatabeanJsp);
		PathSegments pathSegments = PathSegments.parsePathSegments(paths.datarouter.data.toSlashedString(), params);
		List<MapStorageReaderNode<?,?,?>> nodes = getNodes(pathSegments.routerName, pathSegments.tableName);
		if(nodes == null || nodes.size() < 1){
			throw new IllegalArgumentException("Can not find a matching table."
				+ "The correct url is: /ctx/datarouter/data/{router}/{table}/{databeanKey}");
		}
		mav.put("nodes", nodes);

		List<DatabeanWrapper> databeanWrappers = nodes.stream()
				.map(node -> fetchDatabean(node, pathSegments))
				.flatMap(OptionalTool::stream)
				.collect(Collectors.toList());

		if(databeanWrappers.isEmpty()){
			return new StringMav("databean not found");
		}
		mav.put("databeanWrappers", databeanWrappers);
		return mav;
	}

	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	Optional<DatabeanWrapper> fetchDatabean(MapStorageReaderNode<PK,D,F> node, PathSegments pathSegments){
		List<Field<?>> fields = node.getFieldInfo().getFields();
		PK pk = PrimaryKeyPercentCodec.decode(node.getFieldInfo().getPrimaryKeyClass(), pathSegments.encodedPk);
		D databean = node.get(pk, null);
		if(databean != null){
			return Optional.of(new DatabeanWrapper(fields, getRowsOfFields(node, databean), node));
		}
		return Optional.empty();
	}

	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	List<Field<?>> getRowsOfFields(Node<PK,D,F> node, D databean){
		F fielder = node.getFieldInfo().getSampleFielder();
		if(fielder != null){
			return fielder.getFields(databean);
		}
		return null;
	}

	private List<MapStorageReaderNode<?,?,?>> getNodes(String routerName, String tableName){
		Collection<Node<?,?,?>> topLevelNodes = datarouterNodes.getTopLevelNodesByRouterName().get(routerName);
		List<MapStorageReaderNode<?,?,?>> nodes = new ArrayList<>();
		for(Node<?,?,?> topLevelNode : topLevelNodes){
			List<Node<?,?,?>> allTreeNodes = new ArrayList<>();
			allTreeNodes.add(topLevelNode);
			allTreeNodes.addAll(topLevelNode.getChildNodes());
			for(Node<?,?,?> node : allTreeNodes){
				String[] nodeNameSplit = node.getName().split("\\.");
				if(nodeNameSplit.length < 2){
					logger.error("There might be some problem with the nodeName, where nodeName = " + node.getName());
				}
				if(tableName.equalsIgnoreCase(nodeNameSplit[1])){
					if(!(node instanceof MapStorageReader<?,?>)){
						logger.error("Cannot browse non-MapStorageReader "
							+ node.getClass().getSimpleName());
					}else{
						nodes.add((MapStorageReaderNode<?,?,?>)node);
					}
				}
			}
		}
		return nodes;
	}


	public static class DatabeanWrapper{

		private final List<Field<?>> fields;
		private final List<Field<?>> rowOfFields;
		private final Node<?,?,?> node;

		private DatabeanWrapper(List<Field<?>> fields, List<Field<?>> rowOfFields, Node<?,?,?> node){
			this.fields = fields;
			this.rowOfFields = rowOfFields;
			this.node = node;
		}
		public List<Field<?>> getFields(){
			return fields;
		}
		public List<Field<?>> getRowOfFields(){
			return rowOfFields;
		}
		public Node<?,?,?> getNode(){
			return node;
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

		public static PathSegments parsePathSegments(String afterPath, Params params){
			String pathInfoStr = params.getRequest().getPathInfo();
			int startTrim = pathInfoStr.indexOf(afterPath);
			int endTrim = startTrim + afterPath.length();
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
