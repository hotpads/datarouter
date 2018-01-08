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
package io.datarouter.storage.client.imp;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import io.datarouter.storage.client.Client;
import io.datarouter.storage.config.setting.impl.ClientAvailabilitySettings;
import io.datarouter.storage.config.setting.impl.ClientAvailabilitySettings.AvailabilitySettingNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.ComparableTool;

public abstract class BaseClient
implements Client{

	private final String name;
	private final AvailabilitySettingNode availability;

	public BaseClient(String name, ClientAvailabilitySettings clientAvailabilitySettings){
		this.name = name;
		this.availability = clientAvailabilitySettings.getAvailabilityForClientName(getName());
	}

	/**************************** standard ******************************/

	@Override
	public int compareTo(Client client){
		return ComparableTool.nullFirstCompareTo(getName(), client.getName());
	}

	@Override
	public AvailabilitySettingNode getAvailability(){
		return availability;
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public Future<Optional<String>> notifyNodeRegistration(PhysicalNode<?,?,?> node){
		return CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	public String toString(){
		return name;
	}

}
