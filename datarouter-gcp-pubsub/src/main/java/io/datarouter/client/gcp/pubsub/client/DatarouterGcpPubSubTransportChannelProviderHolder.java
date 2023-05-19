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

import javax.inject.Inject;
import javax.inject.Singleton;

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

@Singleton
public class DatarouterGcpPubSubTransportChannelProviderHolder{
	private static final Logger logger = LoggerFactory.getLogger(
			DatarouterGcpPubSubTransportChannelProviderHolder.class);

	private static final int GRPC_CHANNEL_COUNT = 16;
	private static final int MAX_MESSSAGE_RESPONSE_SIZE = 10 * 1024 * 1024;
	private static final int THREAD_COUNT_PER_EVENT_LOOP = 4;

	public final TransportChannelProvider transportChannelProvider;

	@Inject
	public DatarouterGcpPubSubTransportChannelProviderHolder(
			GcpPubsubTransportChannelExecutor gcpPubsubTransportChannelExecutor,
			GcpPubsubManagedChannelExecutor gcpPubsubManagedChannelExecutor,
			GcpPubsubManagedChannelOffloadExecutor gcpPubsubManagedChannelOffloadExecutor,
			GcpPubsubEventLoopGroupExecutor gcpPubsubEventLoopGroupExecutor){

		var eventLoopGroupAndChannelType = makeEventLoopGroupAndChannelType();
		logger.warn("Using channelType={}", eventLoopGroupAndChannelType.channelType.getSimpleName());

		this.transportChannelProvider = SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
				.setMaxInboundMessageSize(MAX_MESSSAGE_RESPONSE_SIZE)
				.setExecutor(gcpPubsubTransportChannelExecutor)
				.setChannelPoolSettings(ChannelPoolSettings.staticallySized(GRPC_CHANNEL_COUNT))
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