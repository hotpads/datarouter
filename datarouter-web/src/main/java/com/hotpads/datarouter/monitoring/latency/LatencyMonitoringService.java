package com.hotpads.datarouter.monitoring.latency;

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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.WebAppName;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.LazyClientProvider;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.node.DatarouterNodes;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.profile.metrics.Metrics;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.Duration;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.java.ReflectionTool;

@Singleton
public class LatencyMonitoringService{
	private static final Logger logger = LoggerFactory.getLogger(LatencyMonitoringService.class);

	private static final int NUM_LAST_CHECKS_TO_RETAIN = 15;
	private static final Config ONLY_FIRST = new Config().setLimit(1);
	private static final String METRIC_PREFIX = "Latency ";
	private static final String DR_CLIENT_PREFIX = "Client ";
	private static final String SS_CHECK_SUFIX = " findFirst";
	private static final String MS_CHECK_SUFIX = " getRandom";

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

	private final Map<String,Deque<CheckResult>> lastResultsByName = new HashMap<>();
	private final Map<String,Deque<Duration>> lastDurationsByName = new HashMap<>();

	private List<Future<?>> runningChecks = Collections.emptyList();

	public void record(String checkName, Duration duration){
		metrics.save(METRIC_PREFIX + checkName, duration.to(TimeUnit.MICROSECONDS));
		addCheckResult(checkName, CheckResult.newSuccess(System.currentTimeMillis(), duration));
		Deque<Duration> lastDurations = getLastDurations(checkName);
		if(lastDurations.size() == NUM_LAST_CHECKS_TO_RETAIN){
			lastDurations.pollLast();
		}
		lastDurations.offerFirst(duration);
		logger.debug("{} - {}", checkName, duration);
	}

	private Deque<Duration> getLastDurations(String checkName){
		return lastDurationsByName.computeIfAbsent(checkName, $ -> new LinkedList<>());
	}

	private Deque<CheckResult> getLastResults(String checkName){
		return lastResultsByName.computeIfAbsent(checkName, $ -> new LinkedList<>());
	}

	private void addCheckResult(String checkName, CheckResult checkResult){
		Deque<CheckResult> lastResults = getLastResults(checkName);
		if(lastResults.size() == NUM_LAST_CHECKS_TO_RETAIN){
			lastResults.pollLast();
		}
		lastResults.offerFirst(checkResult);
	}

	public void recordFailure(String checkName, String failureMessage){
		addCheckResult(checkName, CheckResult.newFailure(System.currentTimeMillis(), failureMessage));
		getLastDurations(checkName).clear();
		logger.info("{} failed - {}", checkName, failureMessage);
	}

	public Map<String,CheckResult> getLastResultByName(){
		return lastResultsByName.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().peekFirst()));
	}

	public Map<String,String> computeLastFiveAvg(){
		return avg(5);
	}

	public Map<String,String> computeLastFifteenAvg(){
		return avg(15);
	}

	private Map<String,String> avg(int nb){
		return lastDurationsByName.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
			OptionalDouble average = entry.getValue().stream()
					.limit(nb)
					.mapToLong(duration -> duration.to(TimeUnit.NANOSECONDS))
					.average();
			if(average.isPresent()){
				return new Duration((long)average.getAsDouble(), TimeUnit.NANOSECONDS).toString();
			}
			return "";
		}));
	}

	private String getCheckNameForDatarouterClient(String clientName){
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
		for(Entry<String,LazyClientProvider> entry : clients.getLazyClientProviderByName().entrySet()){
			if(entry.getValue().isInitialized()){
				Client client = entry.getValue().getClient();
				Collection<PhysicalNode<?,?>> nodesForClient = nodes.getPhysicalNodesForClient(client.getName());
				Optional<PhysicalNode<?,?>> findFirst = nodesForClient.stream().findFirst();
				if(findFirst.isPresent()){
					PhysicalNode<?,?> node = findFirst.get();
					if(node instanceof PhysicalMapStorageNode){
						PhysicalMapStorageNode<?,?> ms = (PhysicalMapStorageNode<?,?>)node;
						checks.add(new LatencyCheck(LatencyMonitoringService.DR_CLIENT_PREFIX + entry.getKey()
								+ LatencyMonitoringService.MS_CHECK_SUFIX, makeGet(ms)));
					}
				}
			}
		}
		Function<Client, Stream<Pair<String,SortedStorage<?,?>>>> mapClientToFirstSortedStorageNode = client -> nodes
				.getPhysicalNodesForClient(client.getName()).stream()
				.filter(node -> node instanceof SortedStorage)
				.limit(1)
				.map(SortedStorage.class::cast)
				.map(ss -> new Pair<>(LatencyMonitoringService.DR_CLIENT_PREFIX + client.getName(), ss));

		checks.addAll(clients.getLazyClientProviderByName().values().stream()
				.filter(LazyClientProvider::isInitialized)
				.map(LazyClientProvider::getClient)
				.flatMap(mapClientToFirstSortedStorageNode)
				.map(pair -> new LatencyCheck(pair.getLeft() + LatencyMonitoringService.SS_CHECK_SUFIX, () -> pair
						.getRight().stream(null, ONLY_FIRST).findFirst()))
				.collect(Collectors.toList()));
		return checks;
	}

	private <PK extends PrimaryKey<PK>> Runnable makeGet(PhysicalMapStorageNode<PK,?> node){
		DatabeanFieldInfo<PK,?,?> fieldInfo = node.getFieldInfo();
		PK pk = ReflectionTool.create(fieldInfo.getPrimaryKeyClass());
		// assumes the node will complete a valid RPC for a PK with null fields
		return () -> node.get(pk, null);
	}

}
