package com.hotpads.clustersetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.setting.StandardServerType;
import com.hotpads.datarouter.util.core.DrObjectTool;

public class ClusterSettingScopeComparator implements Comparator<ClusterSetting> {

	@Override
	public int compare(ClusterSetting first, ClusterSetting second) {
		if(DrObjectTool.bothNull(first, second)){
			return 0;
		}
		if(DrObjectTool.isOneNullButNotTheOther(first, second)){
			return first==null?-1:1;
		}

		ClusterSettingScope firstScope = first.getScope();
		ClusterSettingScope secondScope = second.getScope();
		if(DrObjectTool.bothNull(firstScope, secondScope)){
			return 0;
		}
		if(DrObjectTool.isOneNullButNotTheOther(firstScope, secondScope)){
			return firstScope==null?-1:1;
		}

		int difference = firstScope.getSpecificity() - secondScope.getSpecificity();
		if(difference==0){
			return 0;
		}
		return difference < 0 ? -1 : 1;
	}

	public static class ClusterSettingComparatorTests{

		@Test
		public void testClusterSettingComparator(){
			List<ClusterSetting> settings = new ArrayList<>();
			ClusterSetting serverType = new ClusterSetting("joblet1", ClusterSettingScope.serverType,
					StandardServerType.JOBLET.getPersistentString(), "", "", "");
			settings.add(serverType);
			ClusterSetting serverName = new ClusterSetting("instance1", ClusterSettingScope.serverName,
					StandardServerType.UNKNOWN.getPersistentString(), "mySevrer", "", "");
			settings.add(serverName);
			Assert.assertEquals(Collections.min(settings, new ClusterSettingScopeComparator()), serverName);
			ClusterSetting app = new ClusterSetting("instance1", ClusterSettingScope.application,
					StandardServerType.UNKNOWN.getPersistentString(), "", "myApp", "");
			settings.add(app);
			Assert.assertEquals(Collections.min(settings, new ClusterSettingScopeComparator()), app);
		}

	}

}
