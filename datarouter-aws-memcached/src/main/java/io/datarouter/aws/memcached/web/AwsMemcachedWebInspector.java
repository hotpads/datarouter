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
package io.datarouter.aws.memcached.web;

import static j2html.TagCreator.div;
import static j2html.TagCreator.ul;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.aws.memcached.client.MemcachedClientMode;
import io.datarouter.aws.memcached.client.options.AwsMemcachedOptions;
import io.datarouter.client.memcached.web.MemcachedWebInspector;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.LiTag;

public class AwsMemcachedWebInspector extends MemcachedWebInspector{

	@Inject
	private AwsMemcachedOptions options;

	@Override
	protected Pair<Integer,DivTag> getDetails(ClientId clientId){
		MemcachedClientMode mode = options.getClientMode(clientId.getName());
		Pair<Integer,DivTag> nodeCountByNodeTag = new Pair<>();
		if(mode == MemcachedClientMode.DYNAMIC){
			List<AwsMemcachedNodeEndpointDto> nodeEndpointDtos = Scanner.of(getClient(clientId).getAllNodeEndPoints())
					.map(nodeEndPoint -> new AwsMemcachedNodeEndpointDto(
							nodeEndPoint.getHostName(),
							nodeEndPoint.getIpAddress(),
							nodeEndPoint.getPort()))
					.list();
			var table = new J2HtmlTable<AwsMemcachedNodeEndpointDto>()
					.withClasses("sortable table table-sm table-striped my-4 border")
					.withColumn("HostName", dto -> dto.hostName)
					.withColumn("IpAddress", dto -> dto.ipAddress)
					.withColumn("Port", dto -> dto.port)
					.build(nodeEndpointDtos);
			DivTag divTable = div(table)
					.withClass("container-fluid my-4")
					.withStyle("padding-left: 0px");
			nodeCountByNodeTag.setLeft(nodeEndpointDtos.size());
			nodeCountByNodeTag.setRight(divTable);

		}else{
			List<LiTag> socketAddresses = getClient(clientId).getAvailableServers().stream()
					.map(Object::toString)
					.map(TagCreator::li)
					.collect(Collectors.toList());
			DivTag div = div(ul(socketAddresses.toArray(new ContainerTag[0])));
			nodeCountByNodeTag.setLeft(socketAddresses.size());
			nodeCountByNodeTag.setRight(div);
		}
		return nodeCountByNodeTag;
	}

	private static class AwsMemcachedNodeEndpointDto{

		private final String hostName;
		private final String ipAddress;
		private final int port;

		public AwsMemcachedNodeEndpointDto(String hostName, String ipAddress, int port){
			this.hostName = hostName;
			this.ipAddress = ipAddress;
			this.port = port;
		}

	}

}
