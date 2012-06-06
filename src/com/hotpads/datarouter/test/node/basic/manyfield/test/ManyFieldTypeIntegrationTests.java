package com.hotpads.datarouter.test.node.basic.manyfield.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.FieldSqlTableGenerator;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter.SortedBasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean2;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBeanKey;
import com.hotpads.datarouter.test.node.basic.manyfield.TestEnum;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;
import com.hotpads.util.core.exception.NotImplementedException;

@RunWith(Parameterized.class) public class ManyFieldTypeIntegrationTests{
	static Logger logger = Logger.getLogger(ManyFieldTypeIntegrationTests.class);

	/****************************** static ***********************************/

	static Map<ClientType,BasicNodeTestRouter> routerByClientType = MapTool.create();
	static Map<ClientType,List<ManyFieldTypeBeanKey>> keysByClientType = MapTool.create();

	@Parameters
	public static Collection<Object[]> parameters(){
		return DRTestConstants.CLIENT_TYPE_OBJECT_ARRAYS;
	}

	@BeforeClass
	public static void init() throws IOException{
		Class<?> cls = ManyFieldTypeIntegrationTests.class;

		if(DRTestConstants.ALL_CLIENT_TYPES.contains(ClientType.hibernate)){
			routerByClientType.put(ClientType.hibernate, new SortedBasicNodeTestRouter(
					DRTestConstants.CLIENT_drTestHibernate0, cls));
		}

		if(DRTestConstants.ALL_CLIENT_TYPES.contains(ClientType.hbase)){
			routerByClientType.put(ClientType.hbase, new SortedBasicNodeTestRouter(DRTestConstants.CLIENT_drTestHBase,
					cls));
		}

		if(DRTestConstants.ALL_CLIENT_TYPES.contains(ClientType.memcached)){
			routerByClientType.put(ClientType.memcached, new BasicNodeTestRouter(
					DRTestConstants.CLIENT_drTestMemcached, cls));
		}

		for(ClientType clientType : routerByClientType.keySet()){
			BasicNodeTestRouter router = routerByClientType.get(clientType);
			if(ClientType.hibernate == clientType){
				messUpTable();
				// System.out.println("mess up");
			}
			if(ClientType.memcached != clientType){
				router.manyFieldTypeBean().deleteAll(null);
				Assert.assertEquals(0, CollectionTool.size(router.manyFieldTypeBean().getAll(null)));
			}
			// fixTable();
		}

	}

	private static void fixTable(){
		Connection conn = JdbcTool.openConnection("localhost", 3306, "drTest0", "root", "");
		Statement st = null;
		try{

			st = conn.createStatement();
			ResultSet rs;

			String tableName = "ManyFieldTypeBean";
			List<Field<?>> primaryKeyFields = ListTool.create(), primaryKeyFields2 = ListTool.create();

			List<Field<?>> nonKeyFields = ListTool.createArrayList(), nonKeyFields2 = ListTool.createArrayList();

			ManyFieldTypeBean mftBean = new ManyFieldTypeBean();
			ManyFieldTypeBean2 mftBean2 = new ManyFieldTypeBean2();

			primaryKeyFields = mftBean.getKeyFields();
			nonKeyFields = mftBean.getNonKeyFields();
			FieldSqlTableGenerator fstGenerator = new FieldSqlTableGenerator(tableName, primaryKeyFields, nonKeyFields);
			SqlTable table = fstGenerator.generate();
			SqlCreateTableGenerator ctGenerator = new SqlCreateTableGenerator(table);
			st.execute("drop table if exists " + tableName + ";");
			String sql = ctGenerator.generateDdl();
			// System.out.println(sql);
			st.execute(sql);
			conn.close();
		}catch(Exception e){
			e.printStackTrace();// TODO: handle exception
		}
	}

	private static void messUpTable(){
		Connection conn = JdbcTool.openConnection("localhost", 3306, "drTest0", "root", "");
		Statement st = null;
		try{
			st = conn.createStatement();
			ResultSet rs;
			// modifying the storage engine
			// st.execute("ALTER TABLE ManyFieldTypeBean ENGINE=MYISAM;");
			// modifying the type
			// st.execute("ALTER TABLE ManyFieldTypeBean MODIFY byteField VARCHAR(200);");
			// adding a new column
			// st.execute("ALTER TABLE ManyFieldTypeBean ADD COLUMN abcd" + (int)(Math.random()*100) +
			// " VARCHAR(250);");
			// deleting an existing column l
			// st.execute("ALTER TABLE ManyFieldTypeBean DROP COLUMN varIntEnumField;");
			// deleting the primary key
			// st.execute("ALTER TABLE ManyFieldTypeBean DROP PRIMARY KEY;");

			// adding a foreign key
			st.execute(" CREATE INDEX key1 ON ManyFieldTypeBean(id, varIntField);");
			st.execute(" CREATE INDEX key2 ON ManyFieldTypeBean(characterField, doubleField);");
			// st.execute("ALTER TABLE ManyFieldTypeBean ADD COLUMN blabla BOOLEAN DEFAULT NULL;");
			conn.close();
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	/***************************** fields **************************************/

	protected ClientType clientType;
	protected BasicNodeTestRouter router;

	/***************************** constructors **************************************/

	public ManyFieldTypeIntegrationTests(ClientType clientType){
		this.clientType = clientType;
		this.router = routerByClientType.get(clientType);
		if(!keysByClientType.containsKey(clientType)){
			keysByClientType.put(clientType, new LinkedList<ManyFieldTypeBeanKey>());
		}
	}

	/***************************** tests **************************************/

	@Test
	public void testByte(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setByteField((byte)-57);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertNotSame(bean, roundTripped);
		Assert.assertEquals(bean.getByteField(), roundTripped.getByteField());
		recordKey(bean.getKey());
	}

	@Test
	public void testShort(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setShortField((short)-57);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertNotSame(bean, roundTripped);
		Assert.assertEquals(bean.getShortField(), roundTripped.getShortField());
		recordKey(bean.getKey());
	}

	@Test
	public void testInteger(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setIntegerField(-100057);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());

		bean.setIntegerField(12345);
		router.manyFieldTypeBean().put(bean, null);
		roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());

		bean.setIntegerField(-77);
		int exceptions = 0;
		try{
			router.manyFieldTypeBean().put(bean, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		}catch(Exception e){
			++exceptions;
			router.manyFieldTypeBean().put(bean, new Config().setPutMethod(PutMethod.INSERT_OR_UPDATE));
		}
		int expectedExceptions;
		if(clientType == ClientType.hibernate){
			expectedExceptions = 1;
		}else if(clientType == ClientType.hbase || isMemcached()){
			expectedExceptions = 0;
		}else{
			throw new NotImplementedException("test needs a case for this clientType=" + clientType);
		}
		Assert.assertEquals(expectedExceptions, exceptions);
		roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		Assert.assertTrue(roundTripped.getIntegerField().equals(-77));
		recordKey(bean.getKey());
	}

	@Test
	public void testLong(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		long negative6Billion = 3 * (long)Integer.MIN_VALUE;
		bean.setLongField(negative6Billion);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getLongField(), roundTripped.getLongField());
		Assert.assertTrue(negative6Billion == roundTripped.getLongField());
		recordKey(bean.getKey());
	}

	@Test
	public void testFloat(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		float val = -157.34f;
		bean.setFloatField(val);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getFloatField(), roundTripped.getFloatField());
		Assert.assertTrue(val == roundTripped.getFloatField());
		recordKey(bean.getKey());
	}

	@Test
	public void testNullPrimitive(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		Float val = null;
		bean.setFloatField(val);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getFloatField(), roundTripped.getFloatField());
		Assert.assertTrue(val == roundTripped.getFloatField());
		recordKey(bean.getKey());
	}

	@Test
	public void testDouble(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		double val = -100057.3456f;
		bean.setDoubleField(val);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getDoubleField(), roundTripped.getDoubleField());
		Assert.assertTrue(val == roundTripped.getDoubleField());
		recordKey(bean.getKey());
	}

	@Test
	public void testLongDate(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		Date val = new Date();
		bean.setLongDateField(val);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getLongDateField(), roundTripped.getLongDateField());
		Assert.assertTrue(val.equals(roundTripped.getLongDateField()));
		recordKey(bean.getKey());
	}

	@Test
	public void testCharacter(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setCharacterField('Q');
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getCharacterField(), roundTripped.getCharacterField());
		Assert.assertTrue('Q' == roundTripped.getCharacterField());
		recordKey(bean.getKey());
	}

	@Test
	public void testString(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		char multiByteUtf8Char = (char)555;
		String val = "abcdef" + multiByteUtf8Char;
		bean.setStringField(val);
		bean.setStringByteField(StringByteTool.getByteArray(val, StringByteTool.CHARSET_UTF8));
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		if(ClientType.hibernate == clientType){// we're expecting the db to be in ASCII mode and strip out that weird
												// character
			Assert.assertFalse(bean.getStringField().equals(roundTripped.getStringField()));
		}else if(ClientType.hbase == clientType || isMemcached()){// byte arrays should handle any string
			Assert.assertEquals(bean.getStringField(), roundTripped.getStringField());
		}else{
			throw new NotImplementedException("test needs a case for this clientType=" + clientType);
		}
		String roundTrippedByteString = new String(roundTripped.getStringByteField(), StringByteTool.CHARSET_UTF8);
		Assert.assertEquals(val, roundTrippedByteString);
		recordKey(bean.getKey());
	}

	@Test
	public void testVarInt(){
		// 0
		ManyFieldTypeBean bean0 = new ManyFieldTypeBean();
		bean0.setVarIntField(0);
		router.manyFieldTypeBean().put(bean0, null);

		ManyFieldTypeBean roundTripped0 = router.manyFieldTypeBean().get(bean0.getKey(), null);
		Assert.assertNotSame(bean0, roundTripped0);
		Assert.assertEquals(bean0.getVarIntField(), roundTripped0.getVarIntField());
		recordKey(bean0.getKey());

		// 1234567
		ManyFieldTypeBean bean1234567 = new ManyFieldTypeBean();
		bean1234567.setVarIntField(1234567);
		router.manyFieldTypeBean().put(bean1234567, null);

		ManyFieldTypeBean roundTripped1234567 = router.manyFieldTypeBean().get(bean1234567.getKey(), null);
		Assert.assertNotSame(bean1234567, roundTripped1234567);
		Assert.assertEquals(bean1234567.getVarIntField(), roundTripped1234567.getVarIntField());
		recordKey(bean1234567.getKey());

		// Integer.MAX_VALUE
		ManyFieldTypeBean beanMax = new ManyFieldTypeBean();
		beanMax.setVarIntField(Integer.MAX_VALUE);
		router.manyFieldTypeBean().put(beanMax, null);

		ManyFieldTypeBean roundTrippedMax = router.manyFieldTypeBean().get(beanMax.getKey(), null);
		Assert.assertNotSame(beanMax, roundTrippedMax);
		Assert.assertEquals(beanMax.getVarIntField(), roundTrippedMax.getVarIntField());
		recordKey(beanMax.getKey());
	}

	@Test
	public void testIntegerEnum(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setIntEnumField(TestEnum.beast);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntEnumField(), roundTripped.getIntEnumField());
		Assert.assertTrue(TestEnum.beast == roundTripped.getIntEnumField());
		recordKey(bean.getKey());
	}

	@Test
	public void testVarIntEnum(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setVarIntEnumField(TestEnum.fish);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getVarIntEnumField(), roundTripped.getVarIntEnumField());
		Assert.assertTrue(TestEnum.fish == roundTripped.getVarIntEnumField());
		recordKey(bean.getKey());
	}

	@Test
	public void testStringEnum(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setStringEnumField(TestEnum.cat);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getStringEnumField(), roundTripped.getStringEnumField());
		Assert.assertTrue(TestEnum.cat == roundTripped.getStringEnumField());
		recordKey(bean.getKey());
	}

	@Test
	public void testBlob(){
		LongArray ids = new LongArray();
		ids.add(5L);
		ids.add(10L);
		ids.add(15L);
		ids.add(126L);
		byte[] bytes = LongByteTool.getComparableByteArray(ids);

		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setData(bytes);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertArrayEquals(ArrayTool.primitiveLongArray(ids), LongByteTool.fromComparableByteArray(roundTripped
				.getData()));
		recordKey(bean.getKey());
	}

	@Test
	public void testUInt31(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setIntegerField(7888);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		Assert.assertTrue(7888 == roundTripped.getIntegerField());
		recordKey(bean.getKey());
	}

	@Test
	public void testLongArray(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.appendToLongArrayField(Long.MAX_VALUE);
		bean.appendToLongArrayField(Integer.MAX_VALUE);
		bean.appendToLongArrayField(Short.MAX_VALUE);
		bean.appendToLongArrayField(Byte.MAX_VALUE);
		bean.appendToLongArrayField(5);
		bean.appendToLongArrayField(0);
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertTrue(0 == ListTool.compare(bean.getLongArrayField(), roundTripped.getLongArrayField()));
		recordKey(bean.getKey());
	}

	/**
	 * 
	 */
	@Test
	public void testBigLongArray(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		int numLongs = 1000000;// 8MB
		if(isMemcached()){
			numLongs = 100000;
		}// 800kb (under memcached default 1mb max size)
		for(int i = 0; i < numLongs; ++i){
			bean.appendToLongArrayField(i);
		}
		router.manyFieldTypeBean().put(bean, null);

		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertTrue(0 == ListTool.compare(bean.getLongArrayField(), roundTripped.getLongArrayField()));
		recordKey(bean.getKey());
	}

	/************************** tests for unmarshalling into databeans (a little out of place here **************/

	@Test
	public void testGetAll(){
		if(!isMemcached()){
			List<ManyFieldTypeBean> allBeans = router.manyFieldTypeBean().getAll(null);
			Assert.assertTrue(CollectionTool.sameSize(keysByClientType.get(clientType), allBeans));
		}
	}

	@Test
	public void testGetMulti(){
		List<ManyFieldTypeBean> allBeans = router.manyFieldTypeBean().getMulti(keysByClientType.get(clientType), null);
		Assert.assertTrue(CollectionTool.sameSize(keysByClientType.get(clientType), allBeans));
	}

	/************************* helpers ********************************************/

	protected void recordKey(ManyFieldTypeBeanKey key){
		keysByClientType.get(clientType).add(key);
	}

	public boolean isMemcached(){
		return ClientType.memcached == clientType;
	}
}
