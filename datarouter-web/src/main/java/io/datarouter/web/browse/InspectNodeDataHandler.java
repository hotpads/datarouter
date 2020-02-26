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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.util.PrimaryKeyPercentCodec;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.util.http.RequestTool;

public abstract class InspectNodeDataHandler extends BaseHandler{

	private static final Integer MIN_FIELD_ABBREVIATION_LENGTH = 2;
	private static final String FIELD_PREFIX = "field_";

	protected static final String
			PARAM_startKey = "startKey",
			PARAM_nextKey = "nextKey",
			PARAM_limit = "limit";


	@Inject
	protected DatarouterNodes nodes;
	@Inject
	protected DatarouterWebFiles files;
	@Inject
	private DatarouterWebPaths paths;

	protected Node<?,?,?> node;
	protected Integer limit;

	protected abstract PathNode getFormPath();

	protected abstract List<Field<?>> getFields();

	protected abstract List<Field<?>> getKeyFields();


	@Handler(defaultHandler = true)
	protected Mav showForm(){
		Mav mav = new Mav(getFormPath());
		String nodeName = RequestTool.get(request, "nodeName");
		node = nodes.getNode(nodeName);
		if(node == null){
			return new MessageMav("Cannot find node " + nodeName);
		}
		mav.put("node", node);
		mav.put("tableName", node.getPhysicalNodes().get(0).getFieldInfo().getTableName());
		List<Field<?>> fields = getFields();
		mav.put("fields", fields);
		mav.put("keyFields", getKeyFields());
		mav.put("nonKeyFields", node.getFieldInfo().getNonKeyFields());
		mav.put("FIELD_PREFIX", FIELD_PREFIX);
		mav.put("getNodeDataPath", paths.datarouter.nodes.getData.toSlashedString() + "?nodeName=");
		mav.put("deleteNodeDataPath", paths.datarouter.nodes.deleteData.toSlashedString() + "?nodeName=");
		return mav;
	}

	protected <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> void addDatabeansToMav(Mav mav, List<D> databeans){
		mav.put("databeans", databeans);
		List<List<Field<?>>> rowsOfFields = new ArrayList<>();
		List<String> fieldKeysAndValues = new ArrayList<>();
		@SuppressWarnings("unchecked")
		DatabeanFielder<PK,D> fielder = (DatabeanFielder<PK,D>)node.getFieldInfo().getSampleFielder();
		if(fielder != null){
			for(D databean : IterableTool.nullSafe(databeans)){
				rowsOfFields.add(fielder.getFields(databean));
				List<Field<?>> databeanFieldKeys = databean.getKeyFields();
				String databeanFieldKey = "";
				for(Field<?> field : databeanFieldKeys){
					databeanFieldKey += "&" + field.getKey().getName() + "=" + field.getStringEncodedValue();
				}
				fieldKeysAndValues.add(databeanFieldKey);
			}
			mav.put("rowsOfFields", rowsOfFields);
			mav.put("fieldKeys", fieldKeysAndValues);
		}
		mav.put("abbreviatedFieldNameByFieldName", getFieldAbbreviationByFieldName(fielder, databeans));
		if(CollectionTool.size(databeans) >= limit){
			mav.put(PARAM_nextKey, PrimaryKeyPercentCodec.encode(CollectionTool.getLast(databeans).getKey()));
		}
	}

	private <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Map<String,String> getFieldAbbreviationByFieldName(
			DatabeanFielder<PK,D> fielder, Collection<? extends D> databeans){
		if(CollectionTool.isEmpty(databeans)){
			return new HashMap<>();
		}
		D first = IterableTool.first(databeans);
		List<String> fieldNames = FieldTool.getFieldNames(fielder.getFields(first));
		List<Integer> maxLengths = ListTool.createArrayListAndInitialize(fieldNames.size());
		Collections.fill(maxLengths, 0);

		for(D d : IterableTool.nullSafe(databeans)){
			List<?> values = FieldTool.getFieldValues(fielder.getFields(d));
			for(int i = 0; i < CollectionTool.size(values); ++i){
				int length = values.get(i) == null ? 0 : StringTool.length(values.get(i).toString());
				if(length > maxLengths.get(i)){
					maxLengths.set(i, length);
				}
			}
		}

		Map<String,String> abbreviatedNames = new HashMap<>();
		for(int i = 0; i < maxLengths.size(); ++i){
			int length = maxLengths.get(i);
			if(length < MIN_FIELD_ABBREVIATION_LENGTH){
				length = MIN_FIELD_ABBREVIATION_LENGTH;
			}
			String abbreviated = fieldNames.get(i);
			if(length < StringTool.length(fieldNames.get(i))){
				abbreviated = fieldNames.get(i).substring(0, length);
			}
			abbreviatedNames.put(fieldNames.get(i), abbreviated);
		}
		return abbreviatedNames;
	}

	protected List<String> getFieldValues(Node<?,?,?> node){
		return node.getFieldInfo().getSamplePrimaryKey().getFields().stream()
				.map(Field::getKey)
				.map(FieldKey::getName)
				.map(name -> FIELD_PREFIX + name)
				.map(params::required)
				.map(StringTool::nullSafe)
				.collect(Collectors.toList());
	}

}
