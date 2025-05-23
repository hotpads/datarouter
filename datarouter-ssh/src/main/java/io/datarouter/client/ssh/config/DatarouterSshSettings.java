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
package io.datarouter.client.ssh.config;

import io.datarouter.secret.service.CachedSecretFactory;
import io.datarouter.secret.service.CachedSecretFactory.CachedSecret;
import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterSshSettings extends SettingRoot{

	public final CachedSecret<String> rsaPrivateKey;

	@Inject
	public DatarouterSshSettings(SettingFinder finder, CachedSecretFactory secretFactory){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterSsh.");

		rsaPrivateKey = secretFactory.cacheSecretString(registerString("rsaPrivateKeyLocation", "ssh/rsaPrivateKey"));
	}

}
