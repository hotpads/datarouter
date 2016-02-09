package com.hotpads.handler.datarouter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.hotpads.datarouter.node.DatarouterNodes;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.serialize.PrimaryKeyStringConverter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.PrimaryKeyFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.mav.imp.StringMav;

public class DataBeanViewerHandler <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
		extends BaseHandler{
	private static final String NON_FIELD_AWARE = "nonFieldAware";

	@Inject
	private DatarouterNodes datarouterNodes;


	/**
	 * e.g.
	 * https://localhost:8443/job/datarouter/RentZestimateEntity16/RentZestimate.RZ/1
	 * https://localhost:8443/job/datarouter/place/Area/14644
	 * https://localhost:8443/job/datarouter/search/Listing/1ParkPlace_0-130012758
	 * https://localhost:8443/job/datarouter/hbase1/ModelIndexView16B/corporate_020121012112312100231132102_CHBO_8308
	 * https://localhost:8443/job/datarouter/hbase1/TraceSpan/22858565159955332_0_1
	 *
	 */

	@Override
	protected Mav handleDefault() throws Exception{
		Mav mav = new Mav("/jsp/admin/viewDatabean.jsp");
		String pathInfoStr = params.getRequest().getPathInfo();
		int offset = 1;
		if(pathInfoStr.contains(DatarouterWebDispatcher.URL_DATAROUTER)){
			offset += DatarouterWebDispatcher.URL_DATAROUTER.length();
		}
		String[] pathInfo = params.getRequest().getPathInfo().substring(offset).split("/");
		if(pathInfo.length != 3){
			return new StringMav("The url is not correct!");
		}

		Node<PK,D> node = (Node<PK,D>)datarouterNodes.getNode(pathInfo[0] + "." + pathInfo[1]);
		if(node != null){
			if(!(node instanceof MapStorageReader<?,?>)){
				return new MessageMav("Cannot browse non-MapStorageReader "
					+ node.getClass().getSimpleName());
			}
			mav.put("node", node);
			List<Field<?>> fields = node.getFields();
			mav.put(NON_FIELD_AWARE, "field aware");
			if(fields == null){
				fields = new ArrayList<>();
				fields.addAll(node.getFieldInfo().getPrimaryKeyFields());
				mav.put(NON_FIELD_AWARE, "non field aware");
			}
			mav.put("fields", fields);
			PK key = PrimaryKeyStringConverter.primaryKeyFromString(node.getFieldInfo().getPrimaryKeyClass(),
					(PrimaryKeyFielder<PK>)node.getFieldInfo().getSamplePrimaryKey(), pathInfo[2]);
			key.fromPersistentString(pathInfo[2]);
			MapStorageReader<PK,D> mapNode = (MapStorageReader<PK,D>)node;
			D databean = mapNode.get(key, null);
			if(databean != null){
				addDatabeansToMav(mav, node, databean);
				return mav;
			}
		}
		return new StringMav("databean not found");
	}

	private	void addDatabeansToMav(Mav mav, Node<PK,D> node, D databean){
		List<List<Field<?>>> rowsOfFields = new ArrayList<>();
		DatabeanFielder<PK,D> fielder = node.getFieldInfo().getSampleFielder();
		if(fielder != null){
			List<Field<?>> rowOfFields = fielder.getFields(databean);
			rowsOfFields.add(rowOfFields);
		}
		mav.put("rowsOfFields", rowsOfFields);
	}

}
