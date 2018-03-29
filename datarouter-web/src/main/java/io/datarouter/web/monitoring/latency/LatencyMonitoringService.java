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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.client.Client;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.client.LazyClientProvider;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.setting.impl.ClientAvailabilitySwitchThresholdSettings;
import io.datarouter.storage.metric.Metrics;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.storage.node.op.raw.SortedStorage;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.StreamTool;
import io.datarouter.util.duration.Duration;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.app.WebAppName;

@Singleton
public class LatencyMonitoringService{
	private static final Logger logger = LoggerFactory.getLogger(LatencyMonitoringService.class);

	private static final int MIN_LAST_CHECKS_TO_RETAIN = 15;
	private static final Config ONLY_FIRST = new Config().setLimit(1);
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
	private WebAppName webApp;
	@Inject
	private ClientAvailabilitySwitchThresholdSettings switchThresholdSettings;

	private final Map<String,Deque<CheckResult>> lastResultsByName = new HashMap<>();

	private List<Future<?>> runningChecks = Collections.emptyList();

	public void record(LatencyCheck check, Duration duration){
		metrics.save(METRIC_PREFIX + check.name, duration.to(TimeUnit.MICROSECONDS));
		addCheckResult(check, CheckResult.newSuccess(System.currentTimeMillis(), duration));
		logger.debug("{} - {}", check.name, duration);
	}

	private Deque<CheckResult> getLastResults(String checkName){
		return lastResultsByName.computeIfAbsent(checkName, $ -> new LinkedList<>());
	}

	private void addCheckResult(LatencyCheck check, CheckResult checkResult){
		Deque<CheckResult> lastResults = getLastResults(check.name);
		synchronized(lastResults){
			while(lastResults.size() >= getNumLastChecksToRetain(check)){
				lastResults.pollLast();
			}
			lastResults.offerFirst(checkResult);
		}
	}

	public void recordFailure(LatencyCheck check, String failureMessage){
		addCheckResult(check, CheckResult.newFailure(System.currentTimeMillis(), failureMessage));
		logger.info("{} failed - {}", check.name, failureMessage);
	}

	public Map<String,CheckResult> getLastResultByName(){
		return lastResultsByName.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().peekFirst(), StreamTool
						.throwingMerger(), TreeMap::new));
	}

	private int getNumLastChecksToRetain(LatencyCheck check){
		if(check instanceof DatarouterClientLatencyCheck){
			String clientName = ((DatarouterClientLatencyCheck)check).getClientName();
			return Math.max(MIN_LAST_CHECKS_TO_RETAIN, 2 * switchThresholdSettings.getSwitchThreshold(clientName)
					.getValue());
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
					.filter(Optional::isPresent)
					.map(Optional::get)
					.limit(nb)
					.mapToLong(duration -> duration.to(TimeUnit.NANOSECONDS))
					.average();
			if(average.isPresent()){
				return new Duration((long)average.getAsDouble(), TimeUnit.NANOSECONDS).toString();
			}
			return "";
		}));
	}

	public String getCheckNameForDatarouterClient(String clientName){
		return DR_CLIENT_PREFIX + clientName + SS_CHECK_SUFIX;
	}

	public Deque<CheckResult> getLastResultsForDatarouterClient(String clientName){
		return getLastResults(getCheckNameForDatarouterClient(clientName));
	}

	public CheckResult getLastResultForDatarouterClient(String clientName){
		return getLastResultsForDatarouterClient(clientName).peekFirst();
	}

	public String getGraphLink(String checkName){
		String webApps = webApp.getName();
		String servers = datarouterProperties.getServerName();
		String counters = METRIC_PREFIX + checkName;
		return "/analytics/counters?submitAction=viewCounters"
				+ "&webApps=" + webApps
				+ "&servers=" + servers
				+ "&periods=5000"
				+ "&counters=" + counters
				+ "&frequency=period";
	}

	public String getGraphLinkForDatarouterClient(String clientName){
		return getGraphLink(getCheckNameForDatarouterClient(clientName));
	}

	public void setRunningChecks(List<Future<?>> runningChecks){
		this.runningChecks = runningChecks;
	}

	public void cancelRunningChecks(){
		runningChecks.forEach(future -> future.cancel(true));
	}

	public List<LatencyCheck> getClientChecks(){
		List<LatencyCheck> checks = new ArrayList<>();
		if(MAKE_GET_CHECK){
			for(Entry<String,LazyClientProvider> entry : clients.getLazyClientProviderByName().entrySet()){
				if(entry.getValue().isInitialized()){
					Client client = entry.getValue().getClient();
					Collection<PhysicalNode<?,?,?>> nodesForClient = nodes.getPhysicalNodesForClient(client.getName());
					Optional<PhysicalNode<?,?,?>> findFirst = nodesForClient.stream().findFirst();
					if(findFirst.isPresent()){
						PhysicalNode<?,?,?> node = findFirst.get();
						if(node instanceof PhysicalMapStorageNode){
							PhysicalMapStorageNode<?,?,?> ms = (PhysicalMapStorageNode<?,?,?>)node;
							checks.add(new DatarouterClientLatencyCheck(LatencyMonitoringService.DR_CLIENT_PREFIX
									+ entry.getKey() + LatencyMonitoringService.MS_CHECK_SUFIX, makeGet(ms), entry
									.getKey()));
						}
					}
				}
			}
		}
		Function<Client, Stream<Pair<Client,SortedStorage<?,?>>>> mapClientToFirstSortedStorageNode = client -> nodes
				.getPhysicalNodesForClient(client.getName()).stream()
				.filter(node -> node instanceof SortedStorage)
				.limit(1)
				.map(SortedStorage.class::cast)
				.map(ss -> new Pair<>(client, ss));

		checks.addAll(clients.getLazyClientProviderByName().values().stream()
				.filter(LazyClientProvider::isInitialized)
				.map(LazyClientProvider::getClient)
				.flatMap(mapClientToFirstSortedStorageNode)
				.map(pair -> new DatarouterClientLatencyCheck(getCheckNameForDatarouterClient(pair.getLeft().getName()),
						() -> pair.getRight().stream(null, ONLY_FIRST).findFirst(), pair.getLeft().getName()))
				.collect(Collectors.toList()));
		return checks;
	}

	private <PK extends PrimaryKey<PK>> Runnable makeGet(PhysicalMapStorageNode<PK,?,?> node){
		DatabeanFieldInfo<PK,?,?> fieldInfo = node.getFieldInfo();
		PK pk = ReflectionTool.create(fieldInfo.getPrimaryKeyClass());
		// assumes the node will complete a valid RPC for a PK with null fields
		return () -> node.get(pk, null);
	}

}
