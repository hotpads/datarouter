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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.aws.rds.config.DatarouterAwsRdsConfigSettings;
import io.datarouter.aws.rds.service.AuroraClientIdProvider.AuroraClientDto;
import io.datarouter.util.io.ReaderTool;
import io.datarouter.util.retry.RetryableTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AuroraDnsService{
	private static final Logger logger = LoggerFactory.getLogger(AuroraDnsService.class);

	public static final String WRITER = "Writer";
	public static final String READER = "Reader";
	public static final String OTHER = "other";
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
			addDnsEntry(
					dto.writerClientId().getName(),
					dto.writerDns(),
					dto.clusterName(),
					dto.writerClientId().getName(),
					true);
			for(int i = 0; i < dto.readerClientIds().size(); i++){
				addDnsEntry(
						dto.readerClientIds().get(i).getName(),
						dto.readerDnss().get(i),
						dto.clusterName(),
						dto.writerClientId().getName(),
						false);
			}
			//add other entry
			addDnsEntry(
					dto.otherName(),
					dto.otherDns(),
					dto.clusterName(),
					dto.writerClientId().getName(),
					false);
		}
		return dnsEntryByHostname;
	}

	private void addDnsEntry(
			String clientName,
			String clientUrl,
			String clusterName,
			String writerClientName,
			boolean isWriter){
		logger.warn("adding dnsEntry for clientName={} clientUrl={} clusterName={}", clientName, clientUrl,
				clusterName);
		DnsHostEntryDto dnsEntry = dnsLookUp(clientName, clientUrl, clusterName, writerClientName, isWriter);
		if(dnsEntry.isAuroraInstance){
			dnsEntryByHostname.put(clientName, dnsEntry);
		}
	}

	public DnsHostEntryDto dnsLookUp(
			String clientName,
			String clientUrl,
			String clusterName,
			String writerClientName,
			boolean isWriter){
		return RetryableTool.tryNTimesWithBackoffUnchecked(() -> tryDnsLookUp(clientName, clientUrl, clusterName,
				writerClientName, isWriter), 3, 3, true);
	}

	private DnsHostEntryDto tryDnsLookUp(
			String clientName,
			String hostname,
			String clusterName,
			String writerClientName,
			boolean isWriter)
	throws IOException, InterruptedException{
		logger.warn("dnslookup for clientName={} hostname={} clusterName={}", clientName, hostname, clusterName);
		// TODO use DigRunner
		String ip = null;
		String instanceHostname = null;
		String clusterHostname = null;
		String instanceEndPointSuffix = rdsSettings.rdsInstanceHostnameSuffixEast.get();
		String region = rdsSettings.eastRegion.get();
		boolean writer = isWriter;
		boolean isAuroraInstance = false;
		String cmd = "dig +short " + hostname;
		Process process = Runtime.getRuntime().exec(cmd);
		StringBuilder standardOutput = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
			String line;
			while((line = reader.readLine()) != null){
				logger.warn("hostname={} dig output line={}", hostname, line);
				standardOutput.append(line).append("\n");
				if(line.matches(IPADDRESS_PATTERN)){
					ip = line;
				}else if(line.contains(rdsSettings.rdsClusterEndpointEast.get())
						|| line.contains(rdsSettings.rdsClusterEndpointWest.get())){
					clusterHostname = line;
				}else if(line.contains(rdsSettings.rdsInstanceEndpoint.get())){
					instanceHostname = line;
					if(instanceHostname.contains(rdsSettings.rdsInstanceHostnameSuffixWest.get())){
						region = rdsSettings.westRegion.get();
						instanceEndPointSuffix = rdsSettings.rdsInstanceHostnameSuffixWest.get();
					}
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
		return new DnsHostEntryDto(
				clientName,
				hostname,
				clusterHostname,
				writer,
				instanceHostname,
				ip,
				clusterName,
				writerClientName,
				instanceEndPointSuffix,
				region,
				isAuroraInstance);
	}

	public DnsHostEntryDto getOtherReader(String clientName, String clusterName, String writerClientName){
		String otherClientName = rdsSettings.dbPrefix.get() + clientName + rdsSettings.dbOtherInstanceSuffix.get();
		String otherHostname = otherClientName + rdsSettings.dnsSuffix.get();
		DnsHostEntryDto dnsEntry = dnsLookUp(otherClientName, otherHostname, clusterName, writerClientName, false);
		if(dnsEntry.isAuroraInstance && dnsEntry.ip != null){
			return dnsEntry;
		}
		return null;
	}

	public DnsEntryHosnamesAndMismatcheEntires checkClientEndpoint(Map<String,DnsHostEntryDto> dnsEntryByHostname){
		Set<String> ipSet = new HashSet<>();
		logger.debug("dnsEntryByHostname={}", gson.toJson(dnsEntryByHostname));
		List<DnsHostEntryDto> mismatchedEntries = new ArrayList<>();
		for(DnsHostEntryDto dnsEntry : dnsEntryByHostname.values()){
			if(dnsEntry.ip == null || dnsEntry.ip.equals("")){
				dnsEntry.setIsMissingIpFlag();
				mismatchedEntries.add(dnsEntry);
			}
			if(dnsEntry.isWriter && dnsEntry.clusterHostname == null){
				//if a writer is not pointed to a cluster endpoint
				dnsEntry.setWriterPointedToInstanceFlag();
				mismatchedEntries.add(dnsEntry);
			}else if(!dnsEntry.isWriter && !dnsEntry.isOther()){
				DnsHostEntryDto readerEntry = dnsEntry;
				String writerClientName = dnsEntry.writerClientName;
				DnsHostEntryDto writerEntry = dnsEntryByHostname.get(writerClientName);
				DnsHostEntryDto otherEntry = dnsEntryByHostname.get(writerClientName + OTHER);
				logger.debug("reader={} writer={}", gson.toJson(readerEntry), gson.toJson(writerEntry));
				if(readerEntry.ip != null && readerEntry.ip.equals(writerEntry.ip)){
					readerEntry.setReaderPointedToWriterFlag();
					mismatchedEntries.add(readerEntry);
				}else if(ipSet.contains(readerEntry.ip)){
					//check if a reader is already pointing to same instance
					readerEntry.setReaderPointedToWrongReaderFlag();
					mismatchedEntries.add(readerEntry);
				}
				//check if reader entry is pointed to an other instance
				if(otherEntry != null){
					if(otherEntry.ip != null && readerEntry.ip.equals(otherEntry.ip)){
						readerEntry.setReaderPointedToWrongReaderFlag();
						mismatchedEntries.add(readerEntry);
					}
				}
			}
			ipSet.add(dnsEntry.ip);
		}
		return new DnsEntryHosnamesAndMismatcheEntires(dnsEntryByHostname.values(), mismatchedEntries);
	}

	public record DnsEntryHosnamesAndMismatcheEntires(
			Collection<DnsHostEntryDto> hostEntryDtos,
			List<DnsHostEntryDto> mismatchedEntries){
	}

	public static class DnsHostEntryDto{

		private final String clientName;
		private final String hostname;
		private final String clusterHostname;
		private final String replicationRole;
		private final String instanceHostname;
		private final String ip;
		private final String clusterName;
		private final String instanceHostNameSuffix;
		private final String region;
		private final String writerClientName;
		private final boolean isWriter;

		private boolean isAuroraInstance = false;
		private boolean isMissingIp = false;
		private boolean readerPointedToWriter = false;
		private boolean readerPointerdToWrongReader = false;
		private boolean writerNotPointedToClusterEndpoint = false;

		//public final boolean reader;
		public String clusterEndPoint;

		public DnsHostEntryDto(String clientName, String hostname, String clusterHostname, boolean writer,
				String instanceHostname, String ip, String clusterName, String writerClientName,
				String instanceHostNameSuffix,
				String region, boolean isAuroraInstance){
			this.clientName = clientName;
			this.hostname = hostname;
			this.clusterHostname = clusterHostname;
			this.isWriter = writer;
			this.replicationRole = writer ? WRITER : READER;
			this.instanceHostname = instanceHostname;
			this.ip = ip;
			this.clusterName = clusterName;
			this.writerClientName = writerClientName;
			this.instanceHostNameSuffix = instanceHostNameSuffix;
			this.region = region;
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

		public String getInstanceHostname(){
			return instanceHostname;
		}

		public String getIp(){
			return ip;
		}

		public String getClusterName(){
			return clusterName;
		}

		public String getWriterClientName(){
			return writerClientName;
		}

		public String getRegion(){
			return region;
		}

		public String getInstanceHostNameSuffix(){
			return instanceHostNameSuffix;
		}

		public boolean isReaderPointedToWriter(){
			return readerPointedToWriter;
		}

		public boolean isReaderPointedToWrongReader(){
			return readerPointerdToWrongReader;
		}

		public boolean isAuroraInstance(){
			return isAuroraInstance;
		}

		public boolean isMissingIp(){
			return isMissingIp;
		}

		public boolean isWriterNotPointedToClusterEndpoint(){
			return writerNotPointedToClusterEndpoint;
		}

		public void setReaderPointedToWriterFlag(){
			this.readerPointedToWriter = true;
		}

		public void setWriterPointedToInstanceFlag(){
			this.writerNotPointedToClusterEndpoint = true;
		}

		public void setReaderPointedToWrongReaderFlag(){
			this.readerPointerdToWrongReader = true;
		}

		public void setIsMissingIpFlag(){
			this.isMissingIp = true;
		}

		public String getReplicationRole(){
			return replicationRole;
		}

		public boolean isOther(){
			if(clientName.endsWith(OTHER)){
				return true;
			}
			return false;
		}

		public boolean isWriter(){
			return isWriter;
		}

	}

}
