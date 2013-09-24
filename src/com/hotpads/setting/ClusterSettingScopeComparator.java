package com.hotpads.setting;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;

public class ClusterSettingScopeComparator implements Comparator<ClusterSetting> {

	@Override
	public int compare(ClusterSetting a, ClusterSetting b) {
		if(ObjectTool.bothNull(a, b)){ return 0; }
		if(ObjectTool.isOneNullButNotTheOther(a, b)){ return a==null?-1:1; }
		
		ClusterSettingScope aScope = a.getScope();
		ClusterSettingScope bScope = b.getScope();
		if(ObjectTool.bothNull(aScope, bScope)){ return 0; }
		if(ObjectTool.isOneNullButNotTheOther(aScope, bScope)){ return aScope==null?-1:1; }
		
		int c = aScope.getSpecificity() - bScope.getSpecificity();
		if(c==0){ return 0; }
		return c < 0 ? -1 : 1;		
	}
	
	/*****Test*****/
	public static class ClusterSettingComparatorIntegrationTests{
		
		private static ClusterSetting joblet1;
		private static ClusterSetting joblet2;
		private static ClusterSetting instance1;
		private static List<ClusterSetting> settings = ListTool.create();
		
		@BeforeClass 
		public static void setUpClass() throws IOException{
			joblet1 = new ClusterSetting("joblet1", ClusterSettingScope.serverType, StandardServerType.JOBLET, "" , "","0");
			joblet2 = new ClusterSetting("joblet2", ClusterSettingScope.serverType, StandardServerType.JOBLET, "", "","0");
			instance1 = new ClusterSetting("instance1", ClusterSettingScope.instance, StandardServerType.UNKNOWN, "sdfdf" ,"","1");
			settings.add(joblet1);
			settings.add(instance1);
			settings.add(joblet2);
		}
		
		@AfterClass
		public static void tearDownClass(){
		}
		
		@Test
		public void testClusterSettingComparator(){
			Collections.sort(settings, new ClusterSettingScopeComparator());
			for(ClusterSetting c : settings){
				System.out.println(c.getValue());
			}
			Assert.assertTrue(CollectionTool.getFirst(settings).getValue().equals("1"));
		}
	}
}
