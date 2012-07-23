package com.hotpads.datarouter.storage.databean.update;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;

import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.storage.content.ContentHolder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.test.node.basic.backup.BackupBean;
import com.hotpads.datarouter.test.node.basic.backup.BackupBeanKey;
import com.hotpads.util.core.StringTool;

public class CaseEnforcingDatabeanUpdate <PK extends PrimaryKey<PK>, D extends Databean<PK,D> & ContentHolder<PK,D>> 
extends DatabeanUpdate<PK,D>{

	public <Storage extends MapStorage<PK,D>> CaseEnforcingDatabeanUpdate(Storage storage) {
		super(storage);
	}

	/**
	 * replace if the case of any StringField in the key is different but the case insensitive content is the same
	 */
	@Override
	protected boolean replaceInsteadOfMerge(D oldBean, D newBean) {
		return keysEqualWithDifferentCase(oldBean, newBean);
	}
	
	public static <PK extends PrimaryKey<PK>, D extends Databean<PK,D> & ContentHolder<PK,D>> boolean 
	keysEqualWithDifferentCase(D oldBean, D newBean){
		Map<String,String> oldStringFieldValues = Maps.newHashMap();
		for(Field<?> oldKeyField : oldBean.getKeyFields()){
			if( ! oldKeyField.getClass().isAssignableFrom(StringField.class)){ 
				continue;
			}
			oldStringFieldValues.put(oldKeyField.getName(), oldKeyField.getValueString());
		}
		for(Field<?> newKeyField : newBean.getKeyFields()){
			String oldValue = oldStringFieldValues.get(newKeyField.getName());
			if(StringTool.equalsCaseInsensitiveButNotCaseSensitive(oldValue, newKeyField.getValueString())){
				return true;
			}
		}
		return false;
	}
	
	/** tests ****************************************************************/
	public static class Tests{
		@Test public void testReplaceInsteadOfMerge(){
			BackupBean oldBean = new BackupBean("a","b",1,"d","f1",2L,"f3",4d);
			BackupBean newBean = new BackupBean("a","B",1,"d","f1",2L,"f3",4d);
			
			CaseEnforcingDatabeanUpdate<BackupBeanKey, BackupBean> update = 
					new CaseEnforcingDatabeanUpdate<BackupBeanKey, BackupBean>(null);
			Assert.assertTrue(update.replaceInsteadOfMerge(oldBean, newBean));
			newBean = new BackupBean("a","b",1,"d","f1",2L,"f3",4d);
			Assert.assertFalse(update.replaceInsteadOfMerge(oldBean, newBean));
			newBean = new BackupBean("A","b",1,"d","f1",2L,"f3",4d);
			Assert.assertTrue(update.replaceInsteadOfMerge(oldBean, newBean));
			oldBean = new BackupBean("A","B",1,"d","f1",2L,"f3",4d);
			Assert.assertTrue(update.replaceInsteadOfMerge(oldBean, newBean));
			oldBean = new BackupBean("A","b",1,"d","f1",2L,"f3",4d);
			Assert.assertFalse(update.replaceInsteadOfMerge(oldBean, newBean));
		}
	}
	
}