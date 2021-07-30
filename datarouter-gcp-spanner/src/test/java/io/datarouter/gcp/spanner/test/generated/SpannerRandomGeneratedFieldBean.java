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
package io.datarouter.gcp.spanner.test.generated;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.gcp.spanner.test.generated.SpannerRandomGeneratedFieldBean.SpannerRandomGeneratedFieldBeanKey;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt63FieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class SpannerRandomGeneratedFieldBean
extends BaseDatabean<SpannerRandomGeneratedFieldBeanKey,SpannerRandomGeneratedFieldBean>{

	private String field1;

	public SpannerRandomGeneratedFieldBean(){
		this(null, null);
	}

	public SpannerRandomGeneratedFieldBean(String field1){
		this(null, field1);
	}

	public SpannerRandomGeneratedFieldBean(Long id, String field1){
		super(new SpannerRandomGeneratedFieldBeanKey(id));
		this.field1 = field1;
	}

	@Override
	public Supplier<SpannerRandomGeneratedFieldBeanKey> getKeySupplier(){
		return SpannerRandomGeneratedFieldBeanKey::new;
	}

	public static class FieldKeys{
		public static final StringFieldKey field1 = new StringFieldKey("field1").withSize(10);
	}

	public static class SpannerRandomGeneratedFieldBeanFielder
	extends BaseDatabeanFielder<SpannerRandomGeneratedFieldBeanKey,SpannerRandomGeneratedFieldBean>{

		public SpannerRandomGeneratedFieldBeanFielder(){
			super(SpannerRandomGeneratedFieldBeanKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(SpannerRandomGeneratedFieldBean val){
			return List.of(new StringField(FieldKeys.field1, val.field1));
		}

	}

	public static class SpannerRandomGeneratedFieldBeanKey
	extends BaseRegularPrimaryKey<SpannerRandomGeneratedFieldBeanKey>{

		private Long id;

		public SpannerRandomGeneratedFieldBeanKey(){
			this(null);
		}

		public SpannerRandomGeneratedFieldBeanKey(Long id){
			this.id = id;
		}

		public static class FieldKeys{
			public static final UInt63FieldKey id = new UInt63FieldKey("id")
					.withFieldGeneratorType(FieldGeneratorType.RANDOM);
		}

		@Override
		public List<Field<?>> getFields(){
			return List.of(new UInt63Field(FieldKeys.id, id));
		}

		public Long getId(){
			return id;
		}

		public void setId(Long id){
			this.id = id;
		}

	}
}
