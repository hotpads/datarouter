package com.hotpads.datarouter.client.imp.jdbc.ddl.test;

import java.util.List;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlAlterTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.FieldSqlTableGenerator;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean2;
import com.hotpads.util.core.ListTool;

public class FieldSqlTableGeneratorTester{

	@Test public void testGenerate(){
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
		String tableName = "ManyFieldTypeBean";
		List<Field<?>> primaryKeyFields = ListTool.create();
		List<Field<?>> primaryKeyFields2 = ListTool.create();

		List<Field<?>> nonKeyFields = ListTool.createArrayList();
		List<Field<?>> nonKeyFields2 = ListTool.createArrayList();

		ManyFieldTypeBean mftBean = new ManyFieldTypeBean();
		ManyFieldTypeBean2 mftBean2 = new ManyFieldTypeBean2();
		
		primaryKeyFields = mftBean.getKeyFields();
		nonKeyFields = mftBean.getNonKeyFields();
		FieldSqlTableGenerator fstGenerator = new FieldSqlTableGenerator(tableName, primaryKeyFields, nonKeyFields);
		SqlTable table = fstGenerator.generate();
		SqlCreateTableGenerator ctGenerator = new SqlCreateTableGenerator(table);
		System.out.println(ctGenerator.generateDdl());
		
		primaryKeyFields2 = mftBean2.getKeyFields();
		nonKeyFields2 = mftBean2.getNonKeyFields();
		FieldSqlTableGenerator fstGenerator2 = new FieldSqlTableGenerator(tableName + "2", primaryKeyFields2,
				nonKeyFields2);
		SqlTable table2 = fstGenerator2.generate();
		SqlCreateTableGenerator ctGenerator2 = new SqlCreateTableGenerator(table2);
		System.out.println(ctGenerator2.generateDdl());

		SqlAlterTableGenerator alterGen = new SqlAlterTableGenerator(new SchemaUpdateOptions().setAllTrue(), table2,
				table, "db");
		//SqlColumnNameComparator c = new SqlColumnNameComparator(true);
		System.out.println(alterGen.getAlterTableStatements());
	}

}
