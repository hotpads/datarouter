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
package io.datarouter.trace.storage.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.entity.base.BaseEntityKey;

public class Trace2EntityKey extends BaseEntityKey<Trace2EntityKey>{

	private String traceId;

	public static class FieldKeys{
		public static final StringFieldKey traceId = new StringFieldKey("traceId");
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(new StringField(FieldKeys.traceId, traceId));
	}

	public Trace2EntityKey(){
	}

	public Trace2EntityKey(Traceparent traceparent){
		this.traceId = traceparent.traceId;
	}

	public Trace2EntityKey(String traceId){
		this.traceId = traceId;
	}

	public String getTrace2EntityId(){
		return traceId;
	}

	public Optional<Duration> getAge(){
		return new Traceparent(traceId).getInstant()
				.map(instant -> Duration.between(instant, Instant.now()));
	}

}
