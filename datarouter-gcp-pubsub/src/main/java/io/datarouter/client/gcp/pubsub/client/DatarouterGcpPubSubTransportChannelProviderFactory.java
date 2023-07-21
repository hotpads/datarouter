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
package io.datarouter.client.gcp.pubsub.client;

import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.grpc.ChannelPoolSettings;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;

import io.datarouter.client.gcp.pubsub.GcpPubsubExecutors.GcpPubsubEventLoopGroupExecutor;
import io.datarouter.client.gcp.pubsub.GcpPubsubExecutors.GcpPubsubManagedChannelExecutor;
import io.datarouter.client.gcp.pubsub.GcpPubsubExecutors.GcpPubsubManagedChannelOffloadExecutor;
import io.datarouter.client.gcp.pubsub.GcpPubsubExecutors.GcpPubsubTransportChannelExecutor;
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
public class DatarouterGcpPubSubTransportChannelProviderFactory{
	private static final Logger logger = LoggerFactory.getLogger(
			DatarouterGcpPubSubTransportChannelProviderFactory.class);

	private static final int MAX_MESSSAGE_RESPONSE_SIZE = 10 * 1024 * 1024;
	private static final int THREAD_COUNT_PER_EVENT_LOOP = 4;

	private final GcpPubsubTransportChannelExecutor gcpPubsubTransportChannelExecutor;
	private final GcpPubsubManagedChannelExecutor gcpPubsubManagedChannelExecutor;
	private final GcpPubsubManagedChannelOffloadExecutor gcpPubsubManagedChannelOffloadExecutor;
	private final GcpPubsubEventLoopGroupExecutor gcpPubsubEventLoopGroupExecutor;
	private final EventLoopGroupAndChannelType eventLoopGroupAndChannelType;

	@Inject
	public DatarouterGcpPubSubTransportChannelProviderFactory(
			GcpPubsubTransportChannelExecutor gcpPubsubTransportChannelExecutor,
			GcpPubsubManagedChannelExecutor gcpPubsubManagedChannelExecutor,
			GcpPubsubManagedChannelOffloadExecutor gcpPubsubManagedChannelOffloadExecutor,
			GcpPubsubEventLoopGroupExecutor gcpPubsubEventLoopGroupExecutor){
		this.gcpPubsubTransportChannelExecutor = gcpPubsubTransportChannelExecutor;
		this.gcpPubsubManagedChannelExecutor = gcpPubsubManagedChannelExecutor;
		this.gcpPubsubManagedChannelOffloadExecutor = gcpPubsubManagedChannelOffloadExecutor;
		this.gcpPubsubEventLoopGroupExecutor = gcpPubsubEventLoopGroupExecutor;
		this.eventLoopGroupAndChannelType = makeEventLoopGroupAndChannelType();
		logger.warn("Using channelType={}", eventLoopGroupAndChannelType.channelType.getSimpleName());
	}

	public TransportChannelProvider make(int channelCount){
		return SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
				.setMaxInboundMessageSize(MAX_MESSSAGE_RESPONSE_SIZE)
				.setExecutor(gcpPubsubTransportChannelExecutor)
				.setChannelPoolSettings(ChannelPoolSettings.staticallySized(channelCount))
				.setChannelConfigurator(managedChannelBuilder -> {
					managedChannelBuilder.executor(gcpPubsubManagedChannelExecutor);
					managedChannelBuilder.offloadExecutor(gcpPubsubManagedChannelOffloadExecutor);
					NettyChannelBuilder nettyChannelBuilder = (NettyChannelBuilder) managedChannelBuilder;
					nettyChannelBuilder.eventLoopGroup(eventLoopGroupAndChannelType.eventLoopGroup
							.apply(THREAD_COUNT_PER_EVENT_LOOP, gcpPubsubEventLoopGroupExecutor));
					nettyChannelBuilder.channelType(eventLoopGroupAndChannelType.channelType);
					return managedChannelBuilder;
				})
				.build();
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
			BiFunction<Integer,GcpPubsubEventLoopGroupExecutor,EventLoopGroup> eventLoopGroup,
			Class<? extends Channel> channelType){
	}

}