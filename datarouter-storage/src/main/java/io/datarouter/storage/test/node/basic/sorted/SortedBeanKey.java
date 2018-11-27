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
package io.datarouter.storage.test.node.basic.sorted;

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.positive.UInt31Field;
import io.datarouter.model.field.imp.positive.UInt31FieldKey;
import io.datarouter.model.key.primary.base.BaseEntityPrimaryKey;
import io.datarouter.model.util.CommonFieldSizes;

public class SortedBeanKey extends BaseEntityPrimaryKey<SortedBeanEntityKey,SortedBeanKey>{

	private String foo;
	private String bar;
	private Integer baz;
	private String qux;

	public static class FieldKeys{
		public static final UInt31FieldKey baz = new UInt31FieldKey("baz");
		public static final StringFieldKey qux = new StringFieldKey("qux")
				.withSize(CommonFieldSizes.MAX_KEY_LENGTH_UTF8MB4);
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList(
				new UInt31Field(FieldKeys.baz, baz),
				new StringField(FieldKeys.qux, qux));
	}

	@Override
	public SortedBeanEntityKey getEntityKey(){
		return new SortedBeanEntityKey(foo, bar);
	}

	@Override
	public String getEntityKeyName(){
		return null;
	}

	@Override
	public SortedBeanKey prefixFromEntityKey(SortedBeanEntityKey ek){
		return new SortedBeanKey(ek.getFoo(), ek.getBar(), null, null);
	}

	SortedBeanKey(){
	}

	public SortedBeanKey(String foo, String bar, Integer baz, String qux){
		this.foo = foo;
		this.bar = bar;
		this.baz = baz;
		this.qux = qux;
	}

	public String getFoo(){
		return foo;
	}

	public String getBar(){
		return bar;
	}

	public Integer getBaz(){
		return baz;
	}

	public String getQux(){
		return qux;
	}

}