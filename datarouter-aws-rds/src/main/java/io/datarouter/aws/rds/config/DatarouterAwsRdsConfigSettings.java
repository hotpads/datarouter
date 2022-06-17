/*
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

import io.datarouter.secret.service.CachedSecretFactory;
import io.datarouter.secret.service.CachedSecretFactory.CachedSecret;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterAwsRdsConfigSettings extends SettingNode{

	public final CachedSetting<String> rdsReadOnlyCredentialsLocation;
	public final CachedSecret<RdsCredentialsDto> rdsReadOnlyCredentials;
	public final CachedSetting<String> rdsAddTagsCredentialsLocation;
	public final CachedSecret<RdsCredentialsDto> rdsAddTagsCredentials;
	public final CachedSetting<String> rdsOtherCredentialsLocation;
	public final CachedSecret<RdsCredentialsDto> rdsOtherCredentials;
	public final CachedSetting<String> eastRegion;
	public final CachedSetting<String> westRegion;
	public final CachedSetting<String> rdsClusterEndpointEast;
	public final CachedSetting<String> rdsClusterEndpointWest;
	public final CachedSetting<String> rdsInstanceEndpoint;
	public final CachedSetting<String> rdsInstanceHostnameSuffixEast;
	public final CachedSetting<String> rdsInstanceHostnameSuffixWest;
	public final CachedSetting<String> dnsSuffix;
	public final CachedSetting<String> dbPrefix;
	public final CachedSetting<String> dbOtherInstanceSuffix;
	public final CachedSetting<String> dbOtherInstanceClass;
	public final CachedSetting<String> dbOtherEngine;
	public final CachedSetting<String> dbOtherEastParameterGroup;
	public final CachedSetting<String> dbOtherWestParameterGroup;
	public final CachedSetting<Integer> dbOtherPromotionTier;

	@Inject
	public DatarouterAwsRdsConfigSettings(SettingFinder finder, CachedSecretFactory cachedSecretFactory){
		super(finder, "datarouterAwsRds.config.");

		rdsReadOnlyCredentialsLocation = registerString("rdsReadOnlyCredentialsLocation", "placeholder");
		rdsReadOnlyCredentials = cachedSecretFactory.cacheSharedSecret(rdsReadOnlyCredentialsLocation, RdsCredentialsDto
				.class);
		rdsAddTagsCredentialsLocation = registerString("rdsAddTagsCredentialsLocation", "placeholder");
		rdsAddTagsCredentials = cachedSecretFactory.cacheSharedSecret(rdsAddTagsCredentialsLocation, RdsCredentialsDto
				.class);
		rdsOtherCredentialsLocation = registerString("rdsOtherCredentialsLocation", "placeholder");
		rdsOtherCredentials = cachedSecretFactory.cacheSharedSecret(rdsOtherCredentialsLocation, RdsCredentialsDto
				.class);
		eastRegion = registerStrings("eastRegion", defaultTo(Regions.US_EAST_1.getName()));
		westRegion = registerStrings("westRegion", defaultTo(Regions.US_WEST_2.getName()));
		dnsSuffix = registerString("dnsSuffix", "");
		rdsClusterEndpointEast = registerString("rdsClusterEndpointEast", "");
		rdsClusterEndpointWest = registerString("rdsClusterEndpointWest", "");
		rdsInstanceEndpoint = registerString("rdsInstanceEndpoint", "");
		rdsInstanceHostnameSuffixEast = registerString("rdsInstanceHostnameSuffixEast", "");
		rdsInstanceHostnameSuffixWest = registerString("rdsInstanceHostnameSuffixWest", "");
		dbPrefix = registerString("dbPrefix", "");
		dbOtherInstanceSuffix = registerString("dbOtherInstanceSuffix", "");
		dbOtherInstanceClass = registerString("dbOtherInstanceClass", "");
		dbOtherEngine = registerString("dbOtherEngine", "");
		dbOtherEastParameterGroup = registerString("dbOtherEastParameterGroup", "");
		dbOtherWestParameterGroup = registerString("dbOtherWestParameterGroup", "");
		dbOtherPromotionTier = registerInteger("dbOtherPromotionTier", 15);
	}

	public String getParameterGroup(String region){
		return region.equals(eastRegion.get()) ? dbOtherEastParameterGroup.get()
				: dbOtherWestParameterGroup.get();
	}

	public String getHostnameSuffix(String region){
		return region.equals(eastRegion.get()) ? rdsInstanceHostnameSuffixEast.get()
				: rdsInstanceHostnameSuffixWest.get();
	}

	public String getOtherSuffix(){
		return dbOtherInstanceSuffix.get();
	}

	public String getClusterEndpoint(String region){
		return region.equals(eastRegion.get()) ? rdsClusterEndpointEast.get()
				: rdsClusterEndpointWest.get();
	}

	public static class RdsCredentialsDto{

		public final String accessKey;
		public final String secretKey;

		public RdsCredentialsDto(String accessKey, String secretKey){
			this.accessKey = accessKey;
			this.secretKey = secretKey;
		}

	}

}
