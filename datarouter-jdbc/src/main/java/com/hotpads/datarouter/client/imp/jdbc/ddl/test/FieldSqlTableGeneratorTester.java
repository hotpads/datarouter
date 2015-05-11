package com.hotpads.datarouter.client.imp.jdbc.ddl.test;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlAlterTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.FieldSqlTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodecFactory;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.test.DatarouterTestModuleFactory;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean2;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean2.ManyFieldTypeBean2Fielder;

@Guice(moduleFactory = DatarouterTestModuleFactory.class)
public class FieldSqlTableGeneratorTester{
	@Inject
	private JdbcFieldCodecFactory fieldCodecFactory;

	@Test 
	public void testGenerate(){
		String tableName = "ManyFieldTypeBean";
		List<Field<?>> primaryKeyFields = new ArrayList<>();
		List<Field<?>> primaryKeyFields2 = new ArrayList<>();

		List<Field<?>> nonKeyFields = new ArrayList<>();
		List<Field<?>> nonKeyFields2 = new ArrayList<>();

		ManyFieldBean mftBean = new ManyFieldBean();
		ManyFieldTypeBean2 mftBean2 = new ManyFieldTypeBean2();
		ManyFieldTypeBeanFielder mftFielder = new ManyFieldTypeBeanFielder();
		ManyFieldTypeBean2Fielder mftFielder2 = new ManyFieldTypeBean2Fielder();
		
		primaryKeyFields = mftBean.getKeyFields();
		nonKeyFields = mftFielder.getNonKeyFields(mftBean);
		FieldSqlTableGenerator fstGenerator = new FieldSqlTableGenerator(fieldCodecFactory, tableName, primaryKeyFields, 
				nonKeyFields);
		SqlTable table = fstGenerator.generate();
		SqlCreateTableGenerator ctGenerator = new SqlCreateTableGenerator(table);
		System.out.println(ctGenerator.generateDdl());
		
		primaryKeyFields2 = mftBean2.getKeyFields();
		nonKeyFields2 = mftFielder2.getNonKeyFields(mftBean2);
		FieldSqlTableGenerator fstGenerator2 = new FieldSqlTableGenerator(fieldCodecFactory, tableName + "2",
				primaryKeyFields2, nonKeyFields2);
		SqlTable table2 = fstGenerator2.generate();
		SqlCreateTableGenerator ctGenerator2 = new SqlCreateTableGenerator(table2);
		System.out.println(ctGenerator2.generateDdl());

		SqlAlterTableGenerator alterGen = new SqlAlterTableGenerator(new SchemaUpdateOptions().setAllTrue(), table2,
				table, "db");
		//SqlColumnNameComparator c = new SqlColumnNameComparator(true);
		System.out.println(alterGen.getAlterTableStatements());
	}

}
