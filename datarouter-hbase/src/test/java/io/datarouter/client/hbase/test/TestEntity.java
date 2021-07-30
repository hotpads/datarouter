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
package io.datarouter.client.hbase.test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.entity.BaseEntity;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.field.imp.positive.UInt31Field;
import io.datarouter.model.field.imp.positive.UInt31FieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.key.entity.base.BaseEntityKey;
import io.datarouter.model.key.entity.base.BaseStringDjb16EntityPartitioner;
import io.datarouter.model.key.primary.base.BaseEntityPrimaryKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class TestEntity{

	public static class TestTraceEntityKey extends BaseEntityKey<TestTraceEntityKey>{

		private String id;

		public static class FieldKeys{
			public static final StringFieldKey id = new StringFieldKey("id");
		}

		@Override
		public List<Field<?>> getFields(){
			return List.of(new StringField(FieldKeys.id, id));
		}

		public TestTraceEntityKey(){
		}

		public TestTraceEntityKey(String traceId){
			this.id = traceId;
		}

		public String getId(){
			return id;
		}

	}

	public static class TestTraceEntity extends BaseEntity<TestTraceEntityKey>{

		public static final String QUALIFIER_PREFIX_Trace = "T";
		public static final String QUALIFIER_PREFIX_TraceThread = "TT";
		public static final String QUALIFIER_PREFIX_TraceSpan = "TS";

		public TestTraceEntity(){
			super(new TestTraceEntityKey());
		}

		public TestTraceEntity(TestTraceEntityKey key){
			super(key);
		}

		public TestTrace getTrace(){
			return getDatabeansForQualifierPrefix(TestTrace.class, QUALIFIER_PREFIX_Trace).stream()
					.findFirst()
					.orElse(null);
		}

		public ArrayList<TestTraceThread> getTraceThreads(){
			return getListDatabeansForQualifierPrefix(TestTraceThread.class, QUALIFIER_PREFIX_TraceThread);
		}

		public ArrayList<TestTraceSpan> getTraceSpans(){
			return getListDatabeansForQualifierPrefix(TestTraceSpan.class, QUALIFIER_PREFIX_TraceSpan);
		}

	}

	public static class TestTraceKey extends BaseEntityPrimaryKey<TestTraceEntityKey,TestTraceKey>{

		private TestTraceEntityKey entityKey;

		public TestTraceKey(){
			this.entityKey = new TestTraceEntityKey();
		}

		public TestTraceKey(String id){
			this.entityKey = new TestTraceEntityKey(id);
		}

		@Override
		public List<Field<?>> getPostEntityKeyFields(){
			return List.of();
		}

		@Override
		public TestTraceEntityKey getEntityKey(){
			return entityKey;
		}

		@Override
		public TestTraceKey prefixFromEntityKey(TestTraceEntityKey entityKey){
			return new TestTraceKey(entityKey.getId());
		}

		public String getId(){
			return entityKey.getId();
		}

	}

	public static class TestTrace extends BaseDatabean<TestTraceKey,TestTrace>{

		private String context;
		private String type;
		private String params;
		private Long created;
		private Long duration;

		public static class FieldKeys{
			public static final StringFieldKey context = new StringFieldKey("context");
			public static final StringFieldKey type = new StringFieldKey("type");
			public static final StringFieldKey params = new StringFieldKey("params");
			public static final UInt63FieldKey created = new UInt63FieldKey("created");
			public static final UInt63FieldKey duration = new UInt63FieldKey("duration");
		}

		public static class TestTraceFielder extends BaseDatabeanFielder<TestTraceKey,TestTrace>{

			public TestTraceFielder(){
				super(TestTraceKey::new);
			}

			@Override
			public List<Field<?>> getNonKeyFields(TestTrace databean){
				return List.of(
						new StringField(FieldKeys.context, databean.context),
						new StringField(FieldKeys.type, databean.type),
						new StringField(FieldKeys.params, databean.params),
						new UInt63Field(FieldKeys.created, databean.created),
						new UInt63Field(FieldKeys.duration, databean.duration));
			}

		}

		public TestTrace(){
			super(new TestTraceKey());
		}

		public TestTrace(String id){
			super(new TestTraceKey(id));
		}

		public TestTrace(String id, Long created){
			super(new TestTraceKey(id));
			this.created = created;
		}

		public TestTrace(TestTraceKey key){
			super(key);
		}

		@Override
		public Supplier<TestTraceKey> getKeySupplier(){
			return TestTraceKey::new;
		}

		public String getTraceId(){
			return getKey().getEntityKey().getId();
		}

		public void setParams(String params){
			this.params = params;
		}

		public void setContext(String context){
			this.context = context;
		}

		public void setType(String type){
			this.type = type;
		}

		public void setCreated(Long created){
			this.created = created;
		}

		public void setDuration(Long duration){
			this.duration = duration;
		}

	}

	public static class TestTraceSpanKey extends BaseEntityPrimaryKey<TestTraceEntityKey,TestTraceSpanKey>{

		private TestTraceEntityKey entityKey;
		private Long threadId;
		private Integer sequence;

		public static class FieldKeys{
			public static final LongFieldKey threadId = new LongFieldKey("threadId");
			public static final IntegerFieldKey sequence = new IntegerFieldKey("sequence");
		}

		public TestTraceSpanKey(){
			this.entityKey = new TestTraceEntityKey();
		}

		public TestTraceSpanKey(String id, Long threadId, Integer sequence){
			this.entityKey = new TestTraceEntityKey(id);
			this.threadId = threadId;
			this.sequence = sequence;
		}

		@Override
		public List<Field<?>> getPostEntityKeyFields(){
			return List.of(
					new LongField(FieldKeys.threadId, threadId),
					new IntegerField(FieldKeys.sequence, sequence));
		}

		@Override
		public TestTraceEntityKey getEntityKey(){
			return entityKey;
		}

		public String getId(){
			return entityKey.getId();
		}

		@Override
		public TestTraceSpanKey prefixFromEntityKey(TestTraceEntityKey entityKey){
			return new TestTraceSpanKey(entityKey.getId(), null, null);
		}

	}

	public static class TestTraceSpan extends BaseDatabean<TestTraceSpanKey,TestTraceSpan>{

		private Integer parentSequence;
		private String name;
		private Long created;
		private Long duration;
		private String info;

		public static class FieldKeys{
			public static final UInt31FieldKey parentSequence = new UInt31FieldKey("parentSequence");
			public static final StringFieldKey name = new StringFieldKey("name");
			public static final StringFieldKey info = new StringFieldKey("info");
			public static final UInt63FieldKey created = new UInt63FieldKey("created");
			public static final UInt63FieldKey duration = new UInt63FieldKey("duration");
		}

		public static class TestTraceSpanFielder extends BaseDatabeanFielder<TestTraceSpanKey,TestTraceSpan>{

			public TestTraceSpanFielder(){
				super(TestTraceSpanKey::new);
			}

			@Override
			public List<Field<?>> getNonKeyFields(TestTraceSpan databean){
				return List.of(
						new UInt31Field(FieldKeys.parentSequence, databean.parentSequence),
						new StringField(FieldKeys.name, databean.name),
						new StringField(FieldKeys.info, databean.info),
						new UInt63Field(FieldKeys.created, databean.created),
						new UInt63Field(FieldKeys.duration, databean.duration));
			}

		}

		public TestTraceSpan(){
			this(null, null, null, null);
		}

		public TestTraceSpan(String id, Long threadId, Integer sequence, Integer parentSequence){
			super(new TestTraceSpanKey(id, threadId, sequence));
			this.parentSequence = parentSequence;
		}

		@Override
		public Supplier<TestTraceSpanKey> getKeySupplier(){
			return TestTraceSpanKey::new;
		}

		public String getTraceId(){
			return getKey().getEntityKey().getId();
		}

		public void setName(String name){
			this.name = name;
		}

		public void setCreated(Long created){
			this.created = created;
		}

		public void setDuration(Long duration){
			this.duration = duration;
		}

		public void setInfo(String info){
			this.info = info;
		}

	}

	public static class TestTraceThreadKey extends BaseEntityPrimaryKey<TestTraceEntityKey,TestTraceThreadKey>{

		private TestTraceEntityKey entityKey;
		private Long threadId;

		public static class FieldKeys{
			public static final LongFieldKey threadId = new LongFieldKey("threadId");
		}

		public TestTraceThreadKey(){
			this(null, null);
		}

		public TestTraceThreadKey(String id, Long threadId){
			this.entityKey = new TestTraceEntityKey(id);
			this.threadId = threadId;
		}

		@Override
		public List<Field<?>> getPostEntityKeyFields(){
			return List.of(new LongField(FieldKeys.threadId, threadId));
		}

		@Override
		public TestTraceEntityKey getEntityKey(){
			return entityKey;
		}

		public String getId(){
			return entityKey.getId();
		}

		@Override
		public TestTraceThreadKey prefixFromEntityKey(TestTraceEntityKey entityKey){
			return new TestTraceThreadKey(entityKey.getId(), null);
		}

		public Long getThreadId(){
			return threadId;
		}

	}

	public static class TestTraceThread extends BaseDatabean<TestTraceThreadKey,TestTraceThread>{

		private Long parentId;
		private String name;
		private String info;
		private String serverId;
		private Long created;
		private Long queuedDuration;
		private Long runningDuration;

		public static class FieldKeys{
			public static final UInt63FieldKey parentId = new UInt63FieldKey("parentId");
			public static final StringFieldKey name = new StringFieldKey("name");
			public static final StringFieldKey info = new StringFieldKey("info");
			public static final StringFieldKey serverId = new StringFieldKey("serverId");
			public static final UInt63FieldKey created = new UInt63FieldKey("created");
			public static final UInt63FieldKey queuedDuration = new UInt63FieldKey("queuedDuration");
			public static final UInt63FieldKey runningDuration = new UInt63FieldKey("runningDuration");
		}

		public static class TestTraceThreadFielder extends BaseDatabeanFielder<TestTraceThreadKey,TestTraceThread>{

			public TestTraceThreadFielder(){
				super(TestTraceThreadKey::new);
			}

			@Override
			public List<Field<?>> getNonKeyFields(TestTraceThread databean){
				return List.of(
						new UInt63Field(FieldKeys.parentId, databean.parentId),
						new StringField(FieldKeys.name, databean.name),
						new StringField(FieldKeys.info, databean.info),
						new StringField(FieldKeys.serverId, databean.serverId),
						new UInt63Field(FieldKeys.created, databean.created),
						new UInt63Field(FieldKeys.queuedDuration, databean.queuedDuration),
						new UInt63Field(FieldKeys.runningDuration, databean.runningDuration));
			}

		}

		public TestTraceThread(){
			super(new TestTraceThreadKey());
		}

		public TestTraceThread(String id, Long threadId){
			super(new TestTraceThreadKey(id, threadId));
		}

		@Override
		public Supplier<TestTraceThreadKey> getKeySupplier(){
			return TestTraceThreadKey::new;
		}

		public Long getThreadId(){
			return getKey().getThreadId();
		}

		public String getTraceId(){
			return getKey().getEntityKey().getId();
		}

		public void setServerId(String serverId){
			this.serverId = serverId;
		}

		public void setParentId(Long parentId){
			this.parentId = parentId;
		}

		public void setName(String name){
			this.name = name;
		}

		public void setCreated(Long created){
			this.created = created;
		}

		public void setQueuedDuration(Long queuedDuration){
			this.queuedDuration = queuedDuration;
		}

		public void setRunningDuration(Long runningDuration){
			this.runningDuration = runningDuration;
		}

		public void setInfo(String info){
			this.info = info;
		}

	}

	public static class TestTraceEntityPartitioner extends BaseStringDjb16EntityPartitioner<TestTraceEntityKey>{

		@Override
		protected String makeStringHashInput(TestTraceEntityKey ek){
			return ek.getId();
		}

	}

}
