package com.hotpads.datarouter.client.imp.jdbc.ddl;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.FieldSqlTableGenerator;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean2;
import com.hotpads.util.core.ListTool;

public class FieldSqlTableGeneratorTester {

////	@Test //disabling test until it Asserts something and removes System.out lines
//	public void testGenerate() {
////		DataRouter router;
////		Nodes nodes = router.getNodes();
////		List<? extends PhysicalNode<?,?>> physicalNodes = nodes.getPhysicalNodesForClient(clientName);
////		for(PhysicalNode<?,?> physicalNode : IterableTool.nullSafe(physicalNodes)){
////			DatabeanFieldInfo<?,?,?> fieldInfo = physicalNode.getFieldInfo();
////			if(fieldInfo.getFieldAware()){//use mohcine's table creator
////				List<Field<?>> primaryKeyFields = fieldInfo.getPrimaryKeyFields();
////				List<Field<?>> nonKeyFields = fieldInfo.getNonKeyFields();
////				FieldSqlTableGenerator generator = new FieldSqlTableGenerator(physicalNode.getTableName(),primaryKeyFields, nonKeyFields);
////				//need to somewhere create the table or apply the changes
////			}else{
////			}
////		}
//		String tableName = "TOTO";
//		List<Field<?>> primaryKeyFields = ListTool.create(),
//						primaryKeyFields2 = ListTool.create();
//
//		List<Field<?>> nonKeyFields = ListTool.createArrayList(),
//						nonKeyFields2 = ListTool.createArrayList();
//
//		ManyFieldTypeBean mftBean = new ManyFieldTypeBean();
//		ManyFieldTypeBean2 mftBean2 = new ManyFieldTypeBean2();
//		
//		primaryKeyFields =mftBean.getKeyFields();
//		nonKeyFields = mftBean.getNonKeyFields();
//		FieldSqlTableGenerator fstGenerator = new FieldSqlTableGenerator(tableName, primaryKeyFields, nonKeyFields);
//		SqlTable table = fstGenerator.generate();
//		SqlCreateTableGenerator ctGenerator = new SqlCreateTableGenerator(table);
//		System.out.println(ctGenerator.generate());
//		
//		primaryKeyFields2 =mftBean2.getKeyFields();
//		nonKeyFields2 = mftBean2.getNonKeyFields();
//		FieldSqlTableGenerator fstGenerator2 = new FieldSqlTableGenerator(tableName+"2", primaryKeyFields2, nonKeyFields2);
//		SqlTable table2 = fstGenerator2.generate();
//		SqlCreateTableGenerator ctGenerator2 = new SqlCreateTableGenerator(table2);
//		System.out.println(ctGenerator2.generate());
//		
//		SqlAlterTableGenerator alterGen = new SqlAlterTableGenerator(table2, table);
//		System.out.println(alterGen.getAlterTableStatement());
//	}

}