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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.aws.rds.config.DatarouterAwsRdsConfigSettings;
import io.datarouter.aws.rds.service.AuroraClientIdProvider.AuroraClientDto;
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
	private AuroraClientIdProvider clientIdProvider;
	@Inject
	private DatarouterAwsRdsConfigSettings rdsSettings;

	Map<String,DnsHostEntryDto> dnsEntryByHostname = new HashMap<>();

	public Map<String,DnsHostEntryDto> getDnsEntryForClients(){
		List<AuroraClientDto> auroraclientDtos = clientIdProvider.getAuroraClientDtos();
		for(AuroraClientDto dto : auroraclientDtos){
			addDnsEntry(dto.getWriterClientId().getName(), dto.getWriterDns(), true);
			for(int i = 0; i < dto.getReaderClientIds().size(); i++){
				addDnsEntry(dto.getReaderClientIds().get(i).getName(), dto.getReaderDnss().get(i), false);
			}
		}
		return dnsEntryByHostname;
	}

	private void addDnsEntry(String clientName, String clientUrl, boolean isWriter){
		DnsHostEntryDto dnsEntry = dnsLookUp(clientName, clientUrl, isWriter);
		if(dnsEntry.isAuroraInstance){
			dnsEntryByHostname.put(clientName, dnsEntry);
		}
	}

	public DnsHostEntryDto dnsLookUp(String clientName, String clientUrl, boolean isWriter){
		return RetryableTool.tryNTimesWithBackoffUnchecked(() -> tryDnsLookUp(clientName, clientUrl, isWriter), 3,
				3, true);
	}

	private DnsHostEntryDto tryDnsLookUp(String clientName, String hostname, boolean isWriter)
	throws IOException, InterruptedException{
		// TODO use DigRunner
		String ip = null;
		String instanceHostname = null;
		String clusterHostname = null;
		String clusterName = null;
		boolean writer = isWriter;
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
				}else if(line.contains(rdsSettings.rdsInstanceEndpoint.get())){
					instanceHostname = line;
					isAuroraInstance = true;
				}
			}
		}

		String instanceSuffix = rdsSettings.rdsInstanceHostnameSuffix.get();
		if(instanceHostname != null && instanceHostname.contains(instanceSuffix)){
			clusterName = instanceHostname.substring(0, instanceHostname.indexOf(instanceSuffix));
			clusterName = clusterName.replaceAll("[-?0-9$]+", "");
		}

		String errorOutput = ReaderTool.accumulateStringAndClose(process.getErrorStream());
		process.waitFor();
		int exitValue = process.exitValue();
		if(exitValue > 0){
			logger.warn("clientUrl= {} clusterHostname={} writer={} instanceHostname={} ip={} standard={} error={} "
					+ "exitValue={}", hostname, clusterHostname, writer, instanceHostname, ip, standardOutput,
					errorOutput, exitValue);
		}
		return new DnsHostEntryDto(clientName, hostname, clusterHostname, writer, instanceHostname, ip,
				clusterName, isAuroraInstance);
	}

	public DnsHostEntryDto getOtherReader(String clientName){
		String otherHostname = buildOtherClientUrl(rdsSettings.dbPrefix.get() + clientName);
		DnsHostEntryDto dnsEntry = dnsLookUp(clientName, otherHostname, false);
		if(dnsEntry.isAuroraInstance && dnsEntry.ip != null){
			return dnsEntry;
		}
		return null;
	}

	public List<ClientId> getPrimaryClientIds(){
		return clientIdProvider.getAuroraClientDtos().stream()
				.map(AuroraClientDto::getWriterClientId)
				.collect(Collectors.toList());
	}

	public String buildOtherClientUrl(String clusterName){
		return clusterName + rdsSettings.dbOtherInstanceSuffix.get() + rdsSettings.dnsSuffix.get();
	}

	public Pair<Collection<DnsHostEntryDto>,List<DnsHostEntryDto>> checkClientEndpoint(){
		Map<String,DnsHostEntryDto> dnsEntryByHostname = getDnsEntryForClients();
		Set<String> ipSet = new HashSet<>();
		logger.debug("dnsEntryByHostname={}", gson.toJson(dnsEntryByHostname));
		List<DnsHostEntryDto> mismatchedEntries = new ArrayList<>();
		for(DnsHostEntryDto dnsEntry : dnsEntryByHostname.values()){
			if(dnsEntry.ip == null){
				mismatchedEntries.add(dnsEntry);
			}
			if(dnsEntry.reader){
				DnsHostEntryDto readerEntry = dnsEntry;
				int readerIndex = readerEntry.clientName.indexOf(READER);
				String writerClientName = readerEntry.clientName.substring(0, readerIndex);
				DnsHostEntryDto writerEntry = dnsEntryByHostname.get(writerClientName);
				DnsHostEntryDto otherEntry = getOtherReader(writerClientName);
				logger.debug("reader={} writer={}", gson.toJson(readerEntry), gson.toJson(writerEntry));
				if(readerEntry.ip != null && readerEntry.ip.equals(writerEntry.ip)){
					readerEntry.setReaderPointedToWriterFlag();
					mismatchedEntries.add(readerEntry);
				}
				//check if a reader is already pointing to same instance
				if(ipSet.contains(readerEntry.ip)){
					readerEntry.setReaderPointedToReaderFlag();
					mismatchedEntries.add(readerEntry);
				}
				//check if reader entry is pointed to an other instance
				if(otherEntry != null){
					if(otherEntry.ip != null && readerEntry.ip.equals(otherEntry.ip)){
						readerEntry.setReaderPointedToOtherFlag();
						mismatchedEntries.add(readerEntry);
					}
				}
			}else if(!dnsEntry.reader && dnsEntry.clusterHostname == null){
				//if a writer is not pointed to a cluster endpoint
				dnsEntry.setWriterPointedToInstanceFlag();
				mismatchedEntries.add(dnsEntry);
			}
			ipSet.add(dnsEntry.ip);
		}
		return new Pair<>(dnsEntryByHostname.values(), mismatchedEntries);
	}

	public static class DnsHostEntryDto{

		private final String clientName;
		private final String hostname;
		private final String clusterHostname;
		private final String replicationRole;
		private final String instanceHostname;
		private final String ip;
		private final String clusterName;
		private boolean isAuroraInstance = false;
		private boolean readerPointedToWriter = false;
		private boolean readerPointedToOther = false;
		private boolean readerPointerdToReader = false;
		private boolean writerNotPointedToClusterEndpoint = false;

		public final boolean reader;

		public DnsHostEntryDto(String clientName, String hostname, String clusterHostname, boolean writer,
				String instanceHostname, String ip, String clusterName, boolean isAuroraInstance){
			this.clientName = clientName;
			this.reader = clientName.contains(READER);
			this.hostname = hostname;
			this.clusterHostname = clusterHostname;
			this.replicationRole = writer ? WRITER : READER;
			this.instanceHostname = instanceHostname;
			this.ip = ip;
			this.clusterName = clusterName;
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

		public String getClusterName(){
			return clusterName;
		}

		public boolean isReaderPointedToWriter(){
			return readerPointedToWriter;
		}

		public boolean isReaderPointedToOther(){
			return readerPointedToOther;
		}

		public boolean isAuroraInstance(){
			return isAuroraInstance;
		}

		public boolean isWriterNotPointedToClusterEndpoint(){
			return writerNotPointedToClusterEndpoint;
		}

		public void setReaderPointedToWriterFlag(){
			this.readerPointedToWriter = true;
		}

		public void setReaderPointedToOtherFlag(){
			this.readerPointedToOther = true;
		}

		public void setWriterPointedToInstanceFlag(){
			this.writerNotPointedToClusterEndpoint = true;
		}

		public void setReaderPointedToReaderFlag(){
			this.readerPointerdToReader = true;
		}


	}

}
