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
package io.datarouter.plugin.dataexport.test.storage;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.DoubleField;
import io.datarouter.model.field.imp.comparable.DoubleFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class BackupBean extends BaseDatabean<BackupBeanKey,BackupBean>{

	private String f1;
	private Long f2;
	private String f3;
	private Double f4;

	public static class FieldKeys{
		public static final StringFieldKey f1 = new StringFieldKey("f1");
		public static final LongFieldKey f2 = new LongFieldKey("f2");
		public static final StringFieldKey f3 = new StringFieldKey("f3");
		public static final DoubleFieldKey f4 = new DoubleFieldKey("f4");
	}

	public static class BackupBeanFielder extends BaseDatabeanFielder<BackupBeanKey,BackupBean>{
		public BackupBeanFielder(){
			super(BackupBeanKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(BackupBean databean){
			return List.of(
					new StringField(FieldKeys.f1, databean.f1),
					new LongField(FieldKeys.f2, databean.f2),
					new StringField(FieldKeys.f3, databean.f3),
					new DoubleField(FieldKeys.f4, databean.f4));
		}
	}

	public BackupBean(){
		super(new BackupBeanKey());
	}

	public BackupBean(
			String strA,
			String strB,
			Integer strC,
			String strD,
			String f1,
			Long f2,
			String f3,
			Double f4){
		super(new BackupBeanKey(strA, strB, strC, strD));
		this.f1 = f1;
		this.f2 = f2;
		this.f3 = f3;
		this.f4 = f4;
	}

	@Override
	public Supplier<BackupBeanKey> getKeySupplier(){
		return BackupBeanKey::new;
	}

	public String getF1(){
		return f1;
	}

	public String getF3(){
		return f3;
	}

}
