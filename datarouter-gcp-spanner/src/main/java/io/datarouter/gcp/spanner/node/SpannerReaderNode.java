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
package io.datarouter.gcp.spanner.node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.gcp.spanner.SpannerClientManager;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.gcp.spanner.op.read.SpannerGetKeyOp;
import io.datarouter.gcp.spanner.op.read.SpannerGetOp;
import io.datarouter.gcp.spanner.scan.SpannerDatabeanScanner;
import io.datarouter.gcp.spanner.scan.SpannerKeyScanner;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.util.tuple.Range;

public class SpannerReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>, SortedStorageReader<PK,D>{
	private static final Logger logger = LoggerFactory.getLogger(SpannerReaderNode.class);

	//TEMP for tracking down uses
	@Deprecated
	public static final String INTEGER_ENUM_FIELD_KEY_NAME = "IntegerEnumFieldKey";
	@Deprecated
	public static final String STRING_ENUM_FIELD_KEY_NAME = "StringEnumFieldKey";

	protected final ManagedNodesHolder managedNodesHolder;
	protected final SpannerClientManager clientManager;
	protected final SpannerFieldCodecs fieldCodecs;

	public SpannerReaderNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			ManagedNodesHolder managedNodesHolder,
			SpannerClientManager clientManager,
			SpannerFieldCodecs spannerFieldCodecRegistry){
		super(params, clientType);
		this.managedNodesHolder = managedNodesHolder;
		this.clientManager = clientManager;
		this.fieldCodecs = spannerFieldCodecRegistry;
		D sampleDatabean = params.getDatabeanSupplier().get();
		List<Field<?>> fields = params.getFielderSupplier().get().getFields(sampleDatabean);
		Scanner.of(fields)
				.map(Field::getKey)
				.include(fieldKey -> fieldKey.getClass().getSimpleName().equals(INTEGER_ENUM_FIELD_KEY_NAME)
						|| fieldKey.getClass().getSimpleName().equals(STRING_ENUM_FIELD_KEY_NAME))
				.forEach(fieldKey -> logger.warn("SpannerEnumField type={} databean={} field={}",
						fieldKey.getClass().getSimpleName(),
						sampleDatabean.getClass().getCanonicalName(),
						fieldKey.getName()));
	}

	@Override
	public boolean exists(PK key, Config config){
		var getKeyOp = new SpannerGetKeyOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				Collections.singletonList(key),
				config,
				fieldCodecs);
		return !getKeyOp.wrappedCall().isEmpty();
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		var getKeyOp = new SpannerGetKeyOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				keys,
				config,
				fieldCodecs);
		return getKeyOp.wrappedCall();
	}

	@Override
	public D get(PK key, Config config){
		return getMulti(Collections.singletonList(key), config).stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		var getOp = new SpannerGetOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				keys,
				config,
				fieldCodecs);
		return getOp.wrappedCall();
	}

	@SuppressWarnings("resource")
	@Override
	public Scanner<D> scanRanges(Collection<Range<PK>> ranges, Config config){
		return new SpannerDatabeanScanner<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				ranges,
				config,
				fieldCodecs,
				false)
				.concat(Scanner::of);
	}

	@Override
	public Scanner<D> scan(Range<PK> range, Config config){
		return scanRanges(Collections.singletonList(range), config);
	}

	@SuppressWarnings("resource")
	@Override
	public Scanner<PK> scanRangesKeys(Collection<Range<PK>> ranges, Config config){
		return new SpannerKeyScanner<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				ranges,
				config,
				fieldCodecs,
				false)
				.concat(Scanner::of);
	}

}
