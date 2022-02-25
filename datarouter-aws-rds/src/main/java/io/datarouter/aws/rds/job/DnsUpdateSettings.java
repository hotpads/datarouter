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
package io.datarouter.aws.rds.job;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;

@Singleton
public class DnsUpdateSettings extends SettingNode{

	public final Setting<String> bindAddress;
	public final Setting<String> tsigKey;
	public final Setting<String> tsigSecret;
	public final Setting<String> zone;

	@Inject
	public DnsUpdateSettings(SettingFinder finder){
		super(finder, "datarouterAwsRds.dnsUpdate.");

		this.bindAddress = registerString("bindAddress", "");
		this.tsigKey = registerString("tsigKey", "");
		this.tsigSecret = registerString("tsigSecret", "");
		this.zone = registerString("zone", "");
	}

}
