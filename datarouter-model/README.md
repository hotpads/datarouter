# datarouter-model

Use the classes in datarouter-model to create a representation of your data.  The classes can be used with Java 
collections which will treat them similarly to persistent datastores.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-model</artifactId>
	<version>0.0.111</version>
</dependency>
```

## Constructs
 
### PrimaryKey

##### - [Description and source code](./src/main/java/io/datarouter/model/key/primary/PrimaryKey.java)

##### - Example PrimaryKey

```java
package io.datarouter.example.storage.request;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.field.imp.comparable.InstantFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class RequestLogEntryKey extends BaseRegularPrimaryKey<RequestLogEntryKey>{

	private String path;
	private Instant date;
	private String sessionId;

	public RequestLogEntryKey(){
	}

	public RequestLogEntryKey(String path, Instant date, String sessionId){
		this.path = path;
		this.date = date;
		this.sessionId = sessionId;
	}

	public static class FieldKeys{
		public static final StringFieldKey path = new StringFieldKey("path");
		public static final InstantFieldKey date = new InstantFieldKey("date");
		public static final StringFieldKey sessionId = new StringFieldKey("sessionId");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.path, path),
				new InstantField(FieldKeys.date, date),
				new StringField(FieldKeys.sessionId, sessionId));
	}

	public String getPath(){
		return path;
	}

	public Instant getDate(){
		return date;
	}

	public String getSessionId(){
		return sessionId;
	}

}
```

### Databean

##### - [Description and source code](./src/main/java/io/datarouter/model/databean/Databean.java)

##### - Example Databean

```java
package io.datarouter.example.storage.request;

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class RequestLogEntry extends BaseDatabean<RequestLogEntryKey,RequestLogEntry>{

	private String serverId;
	private Integer responseCode;
	private Long durationMs;

	public RequestLogEntry(){
		super(new RequestLogEntryKey());
	}

	public RequestLogEntry(RequestLogEntryKey key, String serverId, Integer responseCode, Long durationMs){
		super(key);
		this.serverId = serverId;
		this.responseCode = responseCode;
		this.durationMs = durationMs;
	}

	public static class FieldKeys{
		public static final StringFieldKey serverId = new StringFieldKey("serverId");
		public static final IntegerFieldKey responseCode = new IntegerFieldKey("responseCode");
		public static final LongFieldKey durationMs = new LongFieldKey("durationMs");
	}

	public static class RequestLogEntryFielder extends BaseDatabeanFielder<RequestLogEntryKey,RequestLogEntry>{

		public RequestLogEntryFielder(){
			super(RequestLogEntryKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(RequestLogEntry entry){
			return Arrays.asList(
					new StringField(FieldKeys.serverId, entry.serverId),
					new IntegerField(FieldKeys.responseCode, entry.responseCode),
					new LongField(FieldKeys.durationMs, entry.durationMs));
		}
	}

	@Override
	public Class<RequestLogEntryKey> getKeyClass(){
		return RequestLogEntryKey.class;
	}

	public String getServerId(){
		return serverId;
	}

	public Integer getResponseCode(){
		return responseCode;
	}

	public Long getDurationMs(){
		return durationMs;
	}

}
```

### FieldlessIndexEntryPrimaryKey

##### - [source code](./src/main/java/io/datarouter/model/key/FieldlessIndexEntryPrimaryKey.java)

##### - Example FieldlessIndexEntryPrimaryKey

```java
package io.datarouter.example.storage.request;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.key.FieldlessIndexEntryPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class RequestLogEntryBySessionIdDateKey 
extends BaseRegularPrimaryKey<RequestLogEntryBySessionIdDateKey>
implements FieldlessIndexEntryPrimaryKey<
		RequestLogEntryBySessionIdDateKey,
		RequestLogEntryKey,
		RequestLogEntry>{

	private String sessionId;
	private Instant date;
	private String path;

	public RequestLogEntryBySessionIdDateKey(){
	}

	public RequestLogEntryBySessionIdDateKey(String sessionId, Instant date, String path){
		this.sessionId = sessionId;
		this.date = date;
		this.path = path;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(RequestLogEntryKey.FieldKeys.sessionId, sessionId),
				new InstantField(RequestLogEntryKey.FieldKeys.date, date),
				new StringField(RequestLogEntryKey.FieldKeys.path, path));
	}

	@Override
	public RequestLogEntryKey getTargetKey(){
		return new RequestLogEntryKey(path, date, sessionId);
	}

	@Override
	public FieldlessIndexEntry<RequestLogEntryBySessionIdDateKey,RequestLogEntryKey,RequestLogEntry> createFromDatabean(
			RequestLogEntry databean){
		RequestLogEntryBySessionIdDateKey indexEntry = new RequestLogEntryBySessionIdDateKey(
				databean.getKey().getSessionId(),
				databean.getKey().getDate(),
				databean.getKey().getPath());
		return new FieldlessIndexEntry<>(RequestLogEntryBySessionIdDateKey.class, indexEntry);
	}

}
```

### Links to further source code with comments

- [Field](./src/main/java/io/datarouter/model/field/Field.java)
- [Fielder](./src/main/java/io/datarouter/model/serialize/fielder/Fielder.java)
- [EntityKey](./src/main/java/io/datarouter/model/key/entity/EntityKey.java)
- [Entity](./src/main/java/io/datarouter/model/entity/Entity.java)

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
