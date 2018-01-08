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
package io.datarouter.storage.client;

import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.util.concurrent.FutureTool;
import io.datarouter.util.lazy.Lazy;

/*
 * call a bunch of these in parallel
 */
public class LazyClientProvider implements Callable<Client>{

	private final Lazy<Client> client;

	public LazyClientProvider(ClientFactory clientFactory, DatarouterNodes datarouterNodes){
		this.client = Lazy.of(() -> {
			try{
				Client client = clientFactory.call();
				datarouterNodes.getPhysicalNodesForClient(client.getName()).stream()
						.map(client::notifyNodeRegistration)
						.collect(Collectors.collectingAndThen(Collectors.toList(), FutureTool::getAll));
				return client;
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public Client call(){
		return client.get();
	}

	//used by datarouterMenu.jsp
	public boolean isInitialized(){
		return client.isInitialized();
	}

	//bean accessor used by datarouterMenu.jsp
	public Client getClient(){
		return call();
	}

}
