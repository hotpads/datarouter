package com.hotpads.datarouter.client.imp.jdbc.ddl;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.util.core.ListTool;

public class FieldSqlTableGeneratorTest {

	@Test public void testGenerate() {
//		DataRouter router;
//		Nodes nodes = router.getNodes();
//		List<? extends PhysicalNode<?,?>> physicalNodes = nodes.getPhysicalNodesForClient(clientName);
//		for(PhysicalNode<?,?> physicalNode : IterableTool.nullSafe(physicalNodes)){
//			DatabeanFieldInfo<?,?,?> fieldInfo = physicalNode.getFieldInfo();
//			if(fieldInfo.getFieldAware()){//use mohcine's table creator
//				List<Field<?>> primaryKeyFields = fieldInfo.getPrimaryKeyFields();
//				List<Field<?>> nonKeyFields = fieldInfo.getNonKeyFields();
//				FieldSqlTableGenerator generator = new FieldSqlTableGenerator(physicalNode.getTableName(),primaryKeyFields, nonKeyFields);
//				//need to somewhere create the table or apply the changes
//			}else{
//			}
//		}
		String tableName = "TOTO";
		List<Field<?>> primaryKeyFields = ListTool.create();
		primaryKeyFields.add(new BooleanField("b", false));
		List<Field<?>> nonKeyFields = ListTool.createArrayList();
		nonKeyFields.add(new BooleanField("b", false));
		FieldSqlTableGenerator fstGenerator = new FieldSqlTableGenerator(tableName, primaryKeyFields, nonKeyFields);
		System.out.println(fstGenerator.generate());
		
		ManyFieldTypeBean mftBean = new ManyFieldTypeBean();
		primaryKeyFields =mftBean.getKeyFields();
		nonKeyFields = mftBean.getNonKeyFields();
		fstGenerator = new FieldSqlTableGenerator(tableName, primaryKeyFields, nonKeyFields);
		System.out.println(fstGenerator.generate());
	}

}
