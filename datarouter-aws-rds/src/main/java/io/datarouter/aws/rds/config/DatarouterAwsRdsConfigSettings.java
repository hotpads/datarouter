/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.aws.rds.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.amazonaws.regions.Regions;

import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterAwsRdsConfigSettings extends SettingNode{

	public final CachedSetting<String> iamRdsReadOnlyUserAccessKey;
	public final CachedSetting<String> iamRdsReadOnlyUserSecretKey;
	public final CachedSetting<String> iamRdsOtherCreateUserAccessKey;
	public final CachedSetting<String> iamRdsOtherCreateUserSecretKey;
	public final CachedSetting<String> region;
	public final CachedSetting<String> rdsClusterEndpoint;
	public final CachedSetting<String> rdsInstanceEndpoint;
	public final CachedSetting<String> dnsSuffix;
	public final CachedSetting<String> dbPrefix;
	public final CachedSetting<String> dbOtherInstanceSuffix;
	public final CachedSetting<String> dbOtherInstanceClass;
	public final CachedSetting<String> dbOtherEngine;
	public final CachedSetting<String> dbOtherParameterGroup;
	public final CachedSetting<Integer> dbOtherPromotionTier;

	@Inject
	public DatarouterAwsRdsConfigSettings(SettingFinder finder){
		super(finder, "datarouterAwsRds.config.");

		iamRdsReadOnlyUserAccessKey = registerStrings("iamRdsReadOnlyUserAccessKey", defaultTo(""));
		iamRdsReadOnlyUserSecretKey = registerStrings("iamRdsReadOnlyUserSecretKey", defaultTo(""));
		iamRdsOtherCreateUserAccessKey = registerStrings("iamRdsOtherCreateUserAccessKey", defaultTo(""));
		iamRdsOtherCreateUserSecretKey = registerStrings("iamRdsOtherCreateUserSecretKey", defaultTo(""));
		region = registerStrings("region", defaultTo(Regions.US_EAST_1.getName()));
		dnsSuffix = registerString("dnsSuffix", "");
		rdsClusterEndpoint = registerString("rdsClusterEndpoint", "");
		rdsInstanceEndpoint = registerString("rdsInstanceEndpoint", "");
		dbPrefix = registerString("dbPrefix", "");
		dbOtherInstanceSuffix = registerString("dbOtherInstanceSuffix", "");
		dbOtherInstanceClass = registerString("dbOtherInstanceClass", "");
		dbOtherEngine = registerString("dbOtherEngine", "");
		dbOtherParameterGroup = registerString("dbOtherParameterGroup", "");
		dbOtherPromotionTier = registerInteger("dbOtherPromotionTier", 15);
	}

}
