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
package io.datarouter.gcp.spanner;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.Credentials;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.SessionPoolOptions;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;

import io.datarouter.gcp.spanner.SpannerExecutors.SpannerEventLoopGroupExecutor;
import io.datarouter.gcp.spanner.SpannerExecutors.SpannerManagedChannelExecutor;
import io.datarouter.gcp.spanner.SpannerExecutors.SpannerManagedChannelOffloadExecutor;
import io.datarouter.gcp.spanner.client.SpannerClientOptions;
import io.datarouter.gcp.spanner.connection.SpannerDatabaseClientsHolder;
import io.datarouter.gcp.spanner.ddl.SpannerDatabaseCreator;
import io.datarouter.gcp.spanner.execute.SpannerSchemaUpdateService;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.timer.PhaseTimer;
//CHECKSTYLE:OFF
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.Channel;
import io.grpc.netty.shaded.io.netty.channel.EventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel;
//CHECKSTYLE:ON
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SpannerClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(SpannerClientManager.class);

	private static final int THREAD_COUNT_PER_EVENT_LOOP = 1;

	@Inject
	private SpannerClientOptions spannerClientOptions;
	@Inject
	private SpannerDatabaseClientsHolder databaseClientsHolder;
	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;
	@Inject
	private SpannerSchemaUpdateService schemaUpdateService;
	@Inject
	private SpannerDatabaseCreator databaseCreator;
	@Inject
	private SpannerManagedChannelExecutor spannerManagedChannelExecutor;
	@Inject
	private SpannerManagedChannelOffloadExecutor spannerManagedChannelOffloadExecutor;
	@Inject
	private SpannerEventLoopGroupExecutor spannerEventLoopGroupExecutor;

	@Override
	protected Future<Optional<SchemaUpdateResult>> doSchemaUpdate(PhysicalNode<?,?,?> node){
		if(schemaUpdateOptions.getEnabled()){
			return schemaUpdateService.queueNodeForSchemaUpdate(node.getClientId(), node);
		}
		return CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	public void gatherSchemaUpdates(){
		schemaUpdateService.gatherSchemaUpdates(true);
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		DatabaseId databaseId = DatabaseId.of(
				spannerClientOptions.projectId(clientId.getName()),
				spannerClientOptions.instanceId(clientId.getName()),
				spannerClientOptions.databaseName(clientId.getName()));
		var timer = new PhaseTimer(clientId.getName() + "-" + databaseId);

		Credentials credentials = spannerClientOptions.credentials(clientId.getName());
		timer.add("readCredentials");

		int maxSessions = spannerClientOptions.maxSessions(clientId.getName());
		int numChannels = spannerClientOptions.numChannels(clientId.getName());
		logger.warn("spannerSessionPool maxSessions={} numChannels={}", maxSessions, numChannels);
		var eventLoopGroupAndChannelType = makeEventLoopGroupAndChannelType();
		logger.warn("Using channelType={}", eventLoopGroupAndChannelType.channelType.getSimpleName());
		SessionPoolOptions sessionPoolOptions = SessionPoolOptions.newBuilder()
				.setMaxSessions(maxSessions)
				.setFailIfPoolExhausted()
				.build();
		SpannerOptions spannerOptions = SpannerOptions.newBuilder()
				//Disabled until behavior can be confirmed
				//.setCompressorName(SpannerGrpcCompressor.IDENTITY.name)
				.setCredentials(credentials)
				.setNumChannels(numChannels)
				.setSessionPoolOption(sessionPoolOptions)
				.setChannelConfigurator(managedChannelBuilder -> {
					managedChannelBuilder.executor(spannerManagedChannelExecutor);
					managedChannelBuilder.offloadExecutor(spannerManagedChannelOffloadExecutor);
					NettyChannelBuilder nettyChannelBuilder = (NettyChannelBuilder) managedChannelBuilder;
					nettyChannelBuilder.eventLoopGroup(eventLoopGroupAndChannelType.eventLoopGroup
							.apply(THREAD_COUNT_PER_EVENT_LOOP, spannerEventLoopGroupExecutor));
					nettyChannelBuilder.channelType(eventLoopGroupAndChannelType.channelType);
					return managedChannelBuilder;
				})
				.build();
		Spanner spanner = spannerOptions.getService();
		timer.add(String.format("buildSpannerService maxSessions=%s numChannels=%s", maxSessions, numChannels));

		databaseCreator.createIfMissing(spanner, databaseId, timer);

		//create the clients after the database exists or else they won't see the new database
		DatabaseAdminClient databaseAdminClient = spanner.getDatabaseAdminClient();
		DatabaseClient databaseClient = spanner.getDatabaseClient(databaseId);
		databaseClientsHolder.register(clientId, databaseAdminClient, databaseClient, databaseId);
		logger.warn("{}", timer);
	}

	@Override
	public void shutdown(ClientId clientId){
		schemaUpdateService.gatherSchemaUpdates(true);
		databaseClientsHolder.close(clientId);
	}

	public DatabaseClient getDatabaseClient(ClientId clientId){
		initClient(clientId);
		return databaseClientsHolder.getDatabaseClient(clientId);
	}

	private static EventLoopGroupAndChannelType makeEventLoopGroupAndChannelType(){
		try{
			Class.forName("io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoop");
			return new EventLoopGroupAndChannelType(EpollEventLoopGroup::new, EpollSocketChannel.class);
		}catch(Throwable e){
			return new EventLoopGroupAndChannelType(NioEventLoopGroup::new, NioSocketChannel.class);
		}
	}

	record EventLoopGroupAndChannelType(
			BiFunction<Integer,SpannerEventLoopGroupExecutor,EventLoopGroup> eventLoopGroup,
			Class<? extends Channel> channelType){
	}

}
