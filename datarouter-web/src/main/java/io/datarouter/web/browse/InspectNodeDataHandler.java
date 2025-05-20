/*
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.net.UrlTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.browse.dto.DatabeanJspDto;
import io.datarouter.web.browse.dto.FieldJspDto;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;

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
		mav.put("tableName", node.getPhysicalNodes().getFirst().getFieldInfo().getTableName());
		List<Field<?>> fields = getFields();
		mav.put("fields", fields);
		mav.put("keyFields", getKeyFields());
		mav.put("nonKeyFields", node.getFieldInfo().getNonKeyFields());
		mav.put("FIELD_PREFIX", FIELD_PREFIX);
		mav.put("getNodeDataPath", paths.datarouter.nodes.getData.toSlashedString() + "?nodeName=");
		mav.put("deleteNodeDataPath", paths.datarouter.nodes.deleteData.toSlashedString() + "?nodeName=");
		return mav;
	}

	protected <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	void addDatabeansToMav(Mav mav, List<D> databeans){
		ZoneId zoneId = getUserZoneId();
		List<DatabeanJspDto<PK,D>> databeanJspDtos = Scanner.of(databeans)
				.map(databean -> new DatabeanJspDto<>(databean, zoneId))
				.list();
		mav.put("databeans", databeanJspDtos);
		List<List<FieldJspDto>> rowsOfFields = new ArrayList<>();
		List<String> fieldKeysAndValues = new ArrayList<>();
		@SuppressWarnings("unchecked")
		DatabeanFielder<PK,D> fielder = (DatabeanFielder<PK,D>)node.getFieldInfo().getSampleFielder();
		if(fielder != null){
			for(D databean : databeans){
				List<FieldJspDto> fieldJspDtos = Scanner.of(fielder.getFields(databean))
						.map(field -> new FieldJspDto(field, zoneId))
						.list();
				rowsOfFields.add(fieldJspDtos);
				List<Field<?>> databeanFieldKeys = databean.getKeyFields();
				String databeanFieldKey = "";
				for(Field<?> field : databeanFieldKeys){
					String urlEncodedValue = UrlTool.encode(field.getStringEncodedValue());
					databeanFieldKey += "&" + field.getKey().getName() + "=" + urlEncodedValue;
				}
				fieldKeysAndValues.add(databeanFieldKey);
			}
			mav.put("rowsOfFields", rowsOfFields);
			mav.put("fieldKeys", fieldKeysAndValues);
		}
		mav.put("abbreviatedFieldNameByFieldName", getFieldAbbreviationByFieldName(fielder, databeans));
		if(databeans.size() >= limit){
			mav.put(PARAM_nextKey, URLEncoder.encode(
					PrimaryKeyPercentCodecTool.encode(ListTool.getLastOrNull(databeans).getKey()),
					StandardCharsets.UTF_8));
		}
	}

	private <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	Map<String,String> getFieldAbbreviationByFieldName(
			DatabeanFielder<PK,D> fielder,
			Collection<? extends D> databeans){
		if(databeans == null || databeans.isEmpty()){
			return new HashMap<>();
		}
		D first = databeans.stream().findFirst().orElse(null);
		List<String> fieldNames = Scanner.of(fielder.getFields(first))
				.map(Field::getKey)
				.map(FieldKey::getColumnName)
				.list();
		List<Integer> maxLengths = new ArrayList<>();
		IntStream.range(0, fieldNames.size()).forEach(_ -> maxLengths.add(0));

		for(D databean : databeans){
			List<?> values = Scanner.of(fielder.getFields(databean))
					.map(Field::getValue)
					.list();
			for(int i = 0; i < values.size(); ++i){
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
				.toList();
	}

}
