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
package io.datarouter.web.monitoring.latency;

import java.util.ArrayList;
import java.util.Collection;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.count.Counters;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientInitializationTracker;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.setting.impl.DatarouterClientAvailabilitySwitchThresholdSettings;
import io.datarouter.storage.metric.Metrics;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.storage.node.op.raw.SortedStorage;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.OptionalTool;
import io.datarouter.util.StreamTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.tuple.Pair;

@Singleton
public class LatencyMonitoringService{
	private static final Logger logger = LoggerFactory.getLogger(LatencyMonitoringService.class);

	private static final int MIN_LAST_CHECKS_TO_RETAIN = 15;
	private static final Config ONLY_FIRST = new Config().setLimit(1).setOutputBatchSize(1);
	private static final String METRIC_PREFIX = "Latency ";
	private static final String DR_CLIENT_PREFIX = "Client ";
	private static final String SS_CHECK_SUFIX = " findFirst";
	private static final String MS_CHECK_SUFIX = " getRandom";
	private static final boolean MAKE_GET_CHECK = false;

	@Inject
	private DatarouterClients clients;
	@Inject
	private DatarouterNodes nodes;
	@Inject
	private Metrics metrics;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private DatarouterClientAvailabilitySwitchThresholdSettings switchThresholdSettings;
	@Inject
	private ClientInitializationTracker clientInitializationTracker;

	private final Map<String,Deque<CheckResult>> lastResultsByName = new ConcurrentHashMap<>();

	private List<LatencyFuture> runningChecks = Collections.emptyList();

	public void record(LatencyCheck check, DatarouterDuration duration){
		metrics.save(METRIC_PREFIX + check.name, duration.to(TimeUnit.MICROSECONDS));
		addCheckResult(check, CheckResult.newSuccess(System.currentTimeMillis(), duration));
		logger.debug("{} - {}", check.name, duration);
	}

	private Deque<CheckResult> getLastResults(String checkName){
		return lastResultsByName.computeIfAbsent(checkName, $ -> new ConcurrentLinkedDeque<>());
	}

	private void addCheckResult(LatencyCheck check, CheckResult checkResult){
		Deque<CheckResult> lastResults = getLastResults(check.name);
		while(lastResults.size() >= getNumLastChecksToRetain(check)){
			lastResults.pollLast();
		}
		lastResults.offerFirst(checkResult);
	}

	public void recordFailure(LatencyCheck check, DatarouterDuration duration, Exception exception){
		metrics.save(METRIC_PREFIX + check.name + " failure durationUs", duration.to(TimeUnit.MICROSECONDS));
		Counters.inc(METRIC_PREFIX + check.name + " failure");
		addCheckResult(check, CheckResult.newFailure(System.currentTimeMillis(), exception.getMessage()));
		logger.info("{} failed - {}", check.name, duration, exception);
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
			return Math.max(MIN_LAST_CHECKS_TO_RETAIN, 2 * switchThresholdSettings.getSwitchThreshold(clientId).get());
		}
		return MIN_LAST_CHECKS_TO_RETAIN;
	}

	public Map<String,String> computeLastFiveAvg(){
		return avg(5);
	}

	public Map<String,String> computeLastFifteenAvg(){
		return avg(15);
	}

	private Map<String,String> avg(int nb){
		return lastResultsByName.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
			OptionalDouble average = entry.getValue().stream()
					.map(CheckResult::getLatency)
					.flatMap(OptionalTool::stream)
					.limit(nb)
					.mapToLong(duration -> duration.to(TimeUnit.NANOSECONDS))
					.average();
			if(average.isPresent()){
				return new DatarouterDuration((long)average.getAsDouble(), TimeUnit.NANOSECONDS).toString();
			}
			return "";
		}));
	}

	public String getCheckNameForDatarouterClient(ClientId clientId){
		return DR_CLIENT_PREFIX + clientId.getName() + SS_CHECK_SUFIX;
	}

	public Deque<CheckResult> getLastResultsForDatarouterClient(ClientId clientId){
		return getLastResults(getCheckNameForDatarouterClient(clientId));
	}

	public CheckResult getLastResultForDatarouterClient(ClientId clientId){
		return getLastResultsForDatarouterClient(clientId).peekFirst();
	}

	public String getGraphLink(String checkName){
		String webApps = datarouterService.getName();
		String servers = datarouterProperties.getServerName();
		String counters = METRIC_PREFIX + checkName;
		// TODO remove
		return "/analytics/counters?submitAction=viewCounters"
				+ "&webApps=" + webApps
				+ "&servers=" + servers
				+ "&periods=5000"
				+ "&counters=" + counters
				+ "&frequency=period";
	}

	public String getGraphLinkForDatarouterClient(ClientId clientId){
		return getGraphLink(getCheckNameForDatarouterClient(clientId));
	}

	public void setRunningChecks(List<LatencyFuture> runningChecks){
		this.runningChecks = runningChecks;
	}

	public void cancelRunningChecks(){
		runningChecks.stream()
				.filter(check -> !check.future.isDone())
				.forEach(check -> {
					logger.warn("canceling {}", check.check.name);
					recordFailure(check.check, DatarouterDuration.ZERO, new Exception("timeout"));
					check.future.cancel(true);
				});
	}

	public List<LatencyCheck> getClientChecks(){
		List<LatencyCheck> checks = new ArrayList<>();
		if(MAKE_GET_CHECK){
			for(ClientId clientId : clientInitializationTracker.getInitializedClients()){
				Collection<PhysicalNode<?,?,?>> nodesForClient = nodes.getPhysicalNodesForClient(clientId.getName());
				Optional<PhysicalNode<?,?,?>> findFirst = nodesForClient.stream().findFirst();
				if(findFirst.isPresent()){
					PhysicalNode<?,?,?> node = findFirst.get();
					if(node instanceof PhysicalMapStorageNode){
						PhysicalMapStorageNode<?,?,?> ms = (PhysicalMapStorageNode<?,?,?>)node;
						checks.add(new DatarouterClientLatencyCheck(LatencyMonitoringService.DR_CLIENT_PREFIX + clientId
								+ LatencyMonitoringService.MS_CHECK_SUFIX, makeGet(ms), clientId));
					}
				}
			}
		}
		Function<ClientId,Stream<Pair<ClientId,SortedStorage<?,?>>>> mapClientIdToFirstSortedStorageNode = clientId ->
				nodes.getPhysicalNodesForClient(clientId.getName()).stream()
				.filter(node -> node instanceof SortedStorage)
				.limit(1)
				.map(SortedStorage.class::cast)
				.peek(sortedStorage -> logger.info("selected SortedStorage {}", sortedStorage))
				.map(sortedStorage -> new Pair<>(clientId, sortedStorage));

		checks.addAll(clientInitializationTracker.getInitializedClients().stream()
				.filter(clientId -> clients.getClientManager(clientId).monitorLatency())
				.flatMap(mapClientIdToFirstSortedStorageNode)
				.map(pair -> new DatarouterClientLatencyCheck(getCheckNameForDatarouterClient(pair.getLeft()),
						() -> pair.getRight().streamKeys(null, ONLY_FIRST).findFirst(), pair.getLeft()))
				.collect(Collectors.toList()));
		return checks;
	}

	private <PK extends PrimaryKey<PK>> Runnable makeGet(PhysicalMapStorageNode<PK,?,?> node){
		PhysicalDatabeanFieldInfo<PK,?,?> fieldInfo = node.getFieldInfo();
		PK pk = ReflectionTool.create(fieldInfo.getPrimaryKeyClass());
		// assumes the node will complete a valid RPC for a PK with null fields
		return () -> node.exists(pk, null);
	}

}
