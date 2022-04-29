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
package io.datarouter.web.monitoring.latency;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientInitializationTracker;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.setting.impl.DatarouterClientAvailabilitySwitchThresholdSettingsProvider;
import io.datarouter.storage.metric.Gauges;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.util.StreamTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import io.datarouter.web.config.service.ServiceName;

@Singleton
public class LatencyMonitoringService{
	private static final Logger logger = LoggerFactory.getLogger(LatencyMonitoringService.class);

	private static final int MIN_LAST_CHECKS_TO_RETAIN = 15;
	private static final String GAUGE_PREFIX = "Latency ";
	private static final String DATAROUTER_CLIENT_PREFIX = "Client ";
	private static final String SORTED_STORAGE_CHECK_SUFFIX = " findFirst";
	private static final String MAP_STORAGE_CHECK_SUFFIX = " getRandom";

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private Gauges gauges;
	@Inject
	private ServiceName serviceName;
	@Inject
	private DatarouterClientAvailabilitySwitchThresholdSettingsProvider availabilitySwitchThresholdSettingsProvider;
	@Inject
	private ClientInitializationTracker clientInitializationTracker;
	@Inject
	private LatencyMonitoringGraphLink latencyMonitoringGraphLink;
	@Inject
	private DatarouterWebSettingRoot datarouterWebSettingRoot;
	@Inject
	private ServerName serverName;

	private final Map<String,Deque<CheckResult>> lastResultsByName = new ConcurrentHashMap<>();

	private List<LatencyFuture> runningChecks = Collections.emptyList();

	public void record(LatencyCheck check, DatarouterDuration duration){
		saveGauge(check.name, duration);
		addCheckResult(check, CheckResult.newSuccess(System.currentTimeMillis(), duration));
	}

	public void recordFailure(LatencyCheck check, DatarouterDuration duration, Exception exception){
		saveGauge(check.name + " failure durationUs", duration);
		Counters.inc(GAUGE_PREFIX + check.name + " failure");
		addCheckResult(check, CheckResult.newFailure(System.currentTimeMillis(), exception.getMessage()));
		logger.warn("{} failed - {}", check.name, duration, exception);
	}

	private void saveGauge(String name, DatarouterDuration duration){
		if(datarouterWebSettingRoot.saveLatencyGauges.get()){
			gauges.save(GAUGE_PREFIX + name, duration.to(TimeUnit.MICROSECONDS));
		}
	}

	private void addCheckResult(LatencyCheck check, CheckResult checkResult){
		Deque<CheckResult> lastResults = getLastResults(check.name);
		while(lastResults.size() >= getNumLastChecksToRetain(check)){
			lastResults.pollLast();
		}
		lastResults.offerFirst(checkResult);
	}

	private Deque<CheckResult> getLastResults(String checkName){
		return lastResultsByName.computeIfAbsent(checkName, $ -> new ConcurrentLinkedDeque<>());
	}

	public Map<String,CheckResult> getLastResultByName(){
		return lastResultsByName.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().peekFirst(), StreamTool
						.throwingMerger(), TreeMap::new));
	}

	private int getNumLastChecksToRetain(LatencyCheck check){
		if(check instanceof DatarouterClientLatencyCheck){
			ClientId clientId = ((DatarouterClientLatencyCheck)check).getClientId();
			return Math.max(MIN_LAST_CHECKS_TO_RETAIN, 2 * availabilitySwitchThresholdSettingsProvider.get()
					.getSwitchThreshold(clientId).get());
		}
		return MIN_LAST_CHECKS_TO_RETAIN;
	}

	public Map<String,String> computeLastFiveAvg(){
		return avg(5);
	}

	public Map<String,String> computeLastFifteenAvg(){
		return avg(15);
	}

	private Map<String,String> avg(int limit){
		return lastResultsByName.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
			OptionalDouble average = entry.getValue().stream()
					.map(CheckResult::getLatency)
					.flatMap(Optional::stream)
					.limit(limit)
					.mapToLong(duration -> duration.to(TimeUnit.NANOSECONDS))
					.average();
			if(average.isPresent()){
				return new DatarouterDuration((long)average.getAsDouble(), TimeUnit.NANOSECONDS)
						.toString(TimeUnit.MICROSECONDS);
			}
			return "";
		}));
	}

	public String getCheckNameForDatarouterClient(ClientId clientId){
		return DATAROUTER_CLIENT_PREFIX + clientId.getName() + SORTED_STORAGE_CHECK_SUFFIX;
	}

	public Deque<CheckResult> getLastResultsForDatarouterClient(ClientId clientId){
		return getLastResults(getCheckNameForDatarouterClient(clientId));
	}

	public CheckResult getLastResultForDatarouterClient(ClientId clientId){
		return getLastResultsForDatarouterClient(clientId).peekFirst();
	}

	public String getGraphLink(String checkName){
		String webapps = serviceName.get();
		String servers = serverName.get();
		String counters = GAUGE_PREFIX + checkName;
		return latencyMonitoringGraphLink.getGraphLink(webapps, servers, counters);
	}

	public String getGraphLinkForDatarouterClient(ClientId clientId){
		return getGraphLink(getCheckNameForDatarouterClient(clientId));
	}

	public void setRunningChecks(List<LatencyFuture> runningChecks){
		this.runningChecks = runningChecks;
	}

	public void cancelRunningChecks(){
		Scanner.of(runningChecks)
				.exclude(check -> check.future.isDone())
				.forEach(check -> {
					logger.warn("canceling {}", check.check.name);
					recordFailure(check.check, DatarouterDuration.ZERO, new Exception("timeout"));
					check.future.cancel(true);
				});
	}

	public List<LatencyCheck> getClientChecks(){
		return Scanner.of(clientInitializationTracker.getInitializedClients())
				.map(this::makeGetCheckForClient)
				.concat(OptionalScanner::of)
				.list();
	}

	private Optional<LatencyCheck> makeGetCheckForClient(ClientId clientId){
		return Scanner.of(nodes.getPhysicalNodesForClient(clientId.getName()))
				.include(node -> node instanceof PhysicalMapStorageNode<?,?,?>)
				.findFirst()
				.map(node -> new DatarouterClientLatencyCheck(
						LatencyMonitoringService.DATAROUTER_CLIENT_PREFIX
								+ clientId.getName()
								+ LatencyMonitoringService.MAP_STORAGE_CHECK_SUFFIX,
								makeGetCheck((PhysicalMapStorageNode<?,?,?>)node),
						clientId));
	}

	private <PK extends PrimaryKey<PK>> Runnable makeGetCheck(PhysicalMapStorageNode<PK,?,?> node){
		PK pk = node.getFieldInfo().getPrimaryKeySupplier().get();
		pk.getFields()
				.forEach(field -> field.setUsingReflection(pk, field.getKey().getSampleValue()));
		return () -> node.exists(pk);
	}

}
