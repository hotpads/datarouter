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
package io.datarouter.storage.file;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.field.imp.array.ByteArrayFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;

public class DatabaseBlob extends BaseDatabean<DatabaseBlobKey,DatabaseBlob>{

	private Long size;
	private byte[] data;
	private Long expirationMs;

	public static class FieldKeys{
		public static final LongFieldKey size = new LongFieldKey("size");
		public static final ByteArrayFieldKey data = new ByteArrayFieldKey("data")
				.withSize(CommonFieldSizes.MAX_LENGTH_LONGBLOB);
		public static final LongFieldKey expirationMs = new LongFieldKey("expirationMs");
	}

	public static class DatabaseBlobFielder extends BaseDatabeanFielder<DatabaseBlobKey,DatabaseBlob>{

		public DatabaseBlobFielder(){
			super(DatabaseBlobKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatabaseBlob databean){
			return List.of(
					new LongField(FieldKeys.size, databean.size),
					new ByteArrayField(FieldKeys.data, databean.data),
					new LongField(FieldKeys.expirationMs, databean.expirationMs));
		}

	}

	public DatabaseBlob(){
		super(new DatabaseBlobKey());
	}

	public DatabaseBlob(DatabaseBlobKey key){
		super(key);
	}

	public DatabaseBlob(DatabaseBlobKey key, Long size){
		super(key);
		this.size = size;
	}

	public DatabaseBlob(
			PathbeanKey key,
			byte[] data,
			Long expirationMs){
		this(key, data, (long)data.length, expirationMs);
	}

	public DatabaseBlob(
			PathbeanKey key,
			byte[] data,
			Long size,
			Long expirationMs){
		super(new DatabaseBlobKey(key));
		this.size = size;
		this.data = data;
		this.expirationMs = expirationMs;
	}

	@Override
	public Supplier<DatabaseBlobKey> getKeySupplier(){
		return DatabaseBlobKey::new;
	}

	public Long getSize(){
		return size;
	}

	public byte[] getData(){
		return data;
	}

	public Long getExpirationMs(){
		return expirationMs;
	}

}