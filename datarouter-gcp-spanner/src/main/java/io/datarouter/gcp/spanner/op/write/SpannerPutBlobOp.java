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
package io.datarouter.gcp.spanner.op.write;

import java.time.Duration;
import java.util.List;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Options.TransactionOption;
import com.google.cloud.spanner.Struct;

import io.datarouter.gcp.spanner.field.array.SpannerByteArrayFieldCodec;
import io.datarouter.gcp.spanner.op.SpannerBaseOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class SpannerPutBlobOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseOp<Void>{

	private static final String PATH_AND_FILE = DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName();
	private static final String EXPIRATION_MS = DatabaseBlob.FieldKeys.expirationMs.getColumnName();
	private static final String SIZE = DatabaseBlob.FieldKeys.size.getColumnName();
	private static final String DATA = DatabaseBlob.FieldKeys.data.getColumnName();

	private final PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo;
	private final DatabaseClient client;
	private final PathbeanKey key;
	private final byte[] value;
	private final Config config;

	public SpannerPutBlobOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo,
			PathbeanKey key,
			byte[] value,
			Config config){
		super("SpannerPutBlob: " + fieldInfo.getTableName());
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.key = key;
		this.value = value;
		this.config = config;
	}

	@Override
	public Void wrappedCall(){
		TransactionOption[] transactionOptions = {};
		long nowMs = System.currentTimeMillis();
		Long expiresAt = config.findTtl()
				.map(Duration::toMillis)
				.map(ttlMs -> nowMs + ttlMs)
				.orElse(null);
		client.readWriteTransaction(transactionOptions)
				.run(transaction -> {
						var byteField = new ByteArrayField(DatabaseBlob.FieldKeys.data, value);
						var codec = new SpannerByteArrayFieldCodec(byteField);
						Struct row = transaction.readRow(
								fieldInfo.getTableName(),
								Key.of(key.getPathAndFile()),
								List.of(EXPIRATION_MS,
										SIZE));
						Long expiration = null;
						if(row == null || row.isNull(EXPIRATION_MS)
								|| row.getLong(EXPIRATION_MS) < nowMs){
							expiration = expiresAt;
						}else{
							expiration = row.getLong(EXPIRATION_MS);
						}
						var insertOrUpdateTally = Mutation.newInsertOrUpdateBuilder(fieldInfo.getTableName())
								.set(PATH_AND_FILE)
								.to(key.getPathAndFile())
								.set(DATA)
								.to(codec.getSpannerValue())
								.set(SIZE)
								.to(value.length)
								.set(EXPIRATION_MS)
								.to(expiration)
								.build();
						transaction.buffer(insertOrUpdateTally);
						return null;
					});
		return null;
	}

}
