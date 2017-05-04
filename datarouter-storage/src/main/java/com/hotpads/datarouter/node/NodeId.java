package com.hotpads.datarouter.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrStringTool;

public class NodeId<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(NodeId.class);

	private final String name;


	public NodeId(String nodeClassSimpleName, String databeanClassName, String clientName, String tableName,
			String explicitName){
		String source;
		if(DrStringTool.notEmpty(explicitName)){
			source = "explicit";
			this.name = explicitName;
		}else if(DrStringTool.notEmpty(clientName)){
			if(tableName != null){
				source = "client/table";
				this.name = clientName + "." + tableName;
			}else{
				source = "client/class";
				this.name = clientName + "." + databeanClassName;
			}
		}else{
			source = "virtual";
			this.name = databeanClassName + "." + nodeClassSimpleName;
		}
		logger.info("source={}, name={}", source, name);
	}


	public String getName(){
		return name;
	}
}
