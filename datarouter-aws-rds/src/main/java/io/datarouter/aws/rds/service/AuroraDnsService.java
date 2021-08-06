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
package io.datarouter.aws.rds.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.aws.rds.config.DatarouterAwsRdsConfigSettings;
import io.datarouter.client.mysql.factory.MysqlOptions;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.io.ReaderTool;
import io.datarouter.util.retry.RetryableTool;
import io.datarouter.util.tuple.Pair;

@Singleton
public class AuroraDnsService{
	private static final Logger logger = LoggerFactory.getLogger(AuroraDnsService.class);

	public static final String WRITER = "Writer";
	public static final String READER = "Reader";
	public static final String IPADDRESS_PATTERN =
				"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	@Inject
	private Gson gson;
	@Inject
	private MysqlOptions mysqlOptions;
	@Inject
	private AuroraClientIdProvider clientIdProvider;
	@Inject
	private DatarouterAwsRdsConfigSettings rdsSettings;

	public Map<String,DnsHostEntryDto> getDnsEntryForClients(){
		Map<String,DnsHostEntryDto> dnsEntryByHostname = new HashMap<>();
		List<ClientId> clients = clientIdProvider.getAuroraClientIds();
		for(ClientId client : clients){
			String hostname = mysqlOptions.hostname(client);
			DnsHostEntryDto dnsEntry = dnsLookUp(client, hostname);
			if(dnsEntry.isAuroraInstance){
				dnsEntryByHostname.put(hostname, dnsEntry);
			}
		}
		return dnsEntryByHostname;
	}

	public DnsHostEntryDto dnsLookUp(ClientId clientId, String clientUrl){
		return RetryableTool.tryNTimesWithBackoffUnchecked(() -> tryDnsLookUp(clientId, clientUrl), 3, 3, true);
	}

	private DnsHostEntryDto tryDnsLookUp(ClientId clientId, String hostname) throws IOException, InterruptedException{
		String ip = null;
		String instanceHostname = null;
		String clusterHostname = null;
		boolean writer = false;
		boolean isAuroraInstance = false;
		String cmd = "dig +short " + hostname;
		Process process = Runtime.getRuntime().exec(cmd);
		StringBuilder standardOutput = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
			String line;
			while((line = reader.readLine()) != null){
				standardOutput.append(line).append("\n");
				if(line.matches(IPADDRESS_PATTERN)){
					ip = line;
				}else if(line.contains(rdsSettings.rdsClusterEndpoint.get())){
					clusterHostname = line;
					writer = true;
				}else if(line.contains(rdsSettings.rdsInstanceEndpoint.get())){
					instanceHostname = line;
					isAuroraInstance = true;
				}
			}
		}
		String errorOutput = ReaderTool.accumulateStringAndClose(process.getErrorStream());
		process.waitFor();
		int exitValue = process.exitValue();
		if(exitValue > 0){
			logger.warn("clientUrl= {} clusterHostname={} writer={} instanceHostname={} ip={} standard={} error={} "
					+ "exitValue={}", hostname, clusterHostname, writer, instanceHostname, ip, standardOutput,
					errorOutput, exitValue);
		}
		return new DnsHostEntryDto(clientId.getName(), hostname, clusterHostname, writer, instanceHostname, ip,
				isAuroraInstance);
	}

	public DnsHostEntryDto getOtherReader(ClientId clientId){
		String otherHostname = buildOtherClientUrl(clientId.getName());
		DnsHostEntryDto dnsEntry = dnsLookUp(clientId, otherHostname);
		if(dnsEntry.isAuroraInstance && dnsEntry.ip != null){
			return dnsEntry;
		}
		return null;
	}

	public List<ClientId> getPrimaryClientIds(){
		return clientIdProvider.getAuroraClientIds().stream()
				.filter(ClientId::getWritable)
				.collect(Collectors.toList());
	}

	public String buildOtherClientUrl(String clusterName){
		return clusterName + rdsSettings.dbOtherInstanceSuffix.get() + rdsSettings.dnsSuffix.get();
	}

	public Pair<Collection<DnsHostEntryDto>,List<DnsHostEntryDto>> checkReaderEndpoint(){
		Map<String,DnsHostEntryDto> dnsEntryByHostname = getDnsEntryForClients();
		logger.debug("dnsEntryByHostname={}", gson.toJson(dnsEntryByHostname));
		List<DnsHostEntryDto> mismatchedReaderEntries = new ArrayList<>();
		for(DnsHostEntryDto dnsEntry : dnsEntryByHostname.values()){
			if(dnsEntry.reader){
				DnsHostEntryDto readerEntry = dnsEntry;
				String writerClientName = readerEntry.hostname.replace("reader", "");
				DnsHostEntryDto writerEntry = dnsEntryByHostname.get(writerClientName);
				logger.debug("reader={} writer={}", gson.toJson(readerEntry), gson.toJson(writerEntry));
				if(readerEntry.ip != null && readerEntry.ip.equals(writerEntry.ip)){
					readerEntry.readerPointedToWriter = true;
					mismatchedReaderEntries.add(readerEntry);
				}
			}
		}
		return new Pair<>(dnsEntryByHostname.values(), mismatchedReaderEntries);
	}

	public static class DnsHostEntryDto{

		private final String clientName;
		private final String hostname;
		private final String clusterHostname;
		private final String replicationRole;
		private final String instanceHostname;
		private final String ip;
		private boolean isAuroraInstance = false;
		private boolean readerPointedToWriter = false;

		public final boolean reader;

		public DnsHostEntryDto(String clientName, String hostname, String clusterHostname, boolean writer,
				String instanceHostname, String ip, boolean isAuroraInstance){
			this.clientName = clientName;
			this.reader = clientName.contains(READER);
			this.hostname = hostname;
			this.clusterHostname = clusterHostname;
			this.replicationRole = writer ? WRITER : READER;
			this.instanceHostname = instanceHostname;
			this.ip = ip;
			this.isAuroraInstance = isAuroraInstance;
		}

		public String getClientName(){
			return clientName;
		}

		public String getHostname(){
			return hostname;
		}

		public String getClusterHostname(){
			return clusterHostname;
		}

		public String getReplicationRole(){
			return replicationRole;
		}

		public String getInstanceHostname(){
			return instanceHostname;
		}

		public String getIp(){
			return ip;
		}

		public boolean isReaderPointedToWriter(){
			return readerPointedToWriter;
		}

		public boolean isAuroraInstance(){
			return isAuroraInstance;
		}

	}

}
