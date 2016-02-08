package com.hotpads.handler.datarouter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.PrimaryKeyStringConverter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.PrimaryKeyFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.mav.imp.StringMav;

public class DataBeanViewerHandler<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
		extends BaseHandler{
	private static final String URL_DATAROUTER = "/datarouter";

	@Inject
	private Datarouter datarouter;

	@Override
	protected Mav handleDefault() throws Exception{
		Mav mav = new Mav("/jsp/admin/viewDatabean.jsp");
		String[] pathInfo;
		String pathInfoStr = params.getRequest().getPathInfo();
		int offset = 1;
		if(pathInfoStr.contains(URL_DATAROUTER)){
			offset += URL_DATAROUTER.length();
		}
		pathInfo = params.getRequest().getPathInfo().substring(offset).split("/");

		Node<?,?> node = datarouter.getNodes().getNode(pathInfo[0] + "." + pathInfo[1]);
		if(node != null){
			mav.put("node", node);

			List<Field<?>> fields = node.getFields();
			mav.put("nonFieldAware", "field aware");

			if(fields == null){
				fields = new ArrayList<>();
				fields.addAll(node.getFieldInfo().getPrimaryKeyFields());
				mav.put("nonFieldAware", " non field aware");
			}

			mav.put("fields", fields);

			PK key = PrimaryKeyStringConverter.primaryKeyFromString((Class<PK>)node.getFieldInfo().getPrimaryKeyClass(),
					(PrimaryKeyFielder<PK>)node.getFieldInfo().getSamplePrimaryKey(), pathInfo[2]);
			key.fromPersistentString(pathInfo[2]);
			if(!(node instanceof MapStorageReader<?,?>)){
				return new MessageMav("Cannot browse non-SortedStorageReader"
					+ node.getClass().getSimpleName());
			}
			MapStorageReader<PK,D> mapNode = (MapStorageReader<PK,D>)node;

			D databean = mapNode.get(key, null);
			if(databean != null){
				addDatabeansToMav(mav, node, databean);
				return mav;
			}
		}
		return new StringMav("not found");
	}

	private void addDatabeansToMav(Mav mav, Node node, D databean){
		List<List<Field<?>>> rowsOfFields = new ArrayList<>();
		DatabeanFielder fielder = node.getFieldInfo().getSampleFielder();
		if(fielder != null){
			List<Field<?>> rowOfFields = fielder.getFields(databean);
			rowsOfFields.add(rowOfFields);
		}
		mav.put("rowsOfFields", rowsOfFields);
	}

}
