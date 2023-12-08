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
package io.datarouter.auth.storage.user.saml;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.types.MilliTime;

public class SamlAuthnRequestRedirectUrl
extends BaseDatabean<SamlAuthnRequestRedirectUrlKey,SamlAuthnRequestRedirectUrl>{

	private String redirectUrl;
	private MilliTime createdAt;

	public SamlAuthnRequestRedirectUrl(){
		super(new SamlAuthnRequestRedirectUrlKey());
	}

	public SamlAuthnRequestRedirectUrl(String authnRequestId, String redirectUrl){
		super(new SamlAuthnRequestRedirectUrlKey(authnRequestId));
		this.redirectUrl = redirectUrl;
		this.createdAt = MilliTime.now();
	}

	public static class FieldKeys{
		public static final StringFieldKey redirectUrl = new StringFieldKey("redirectUrl")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
		public static final LongEncodedFieldKey<MilliTime> createdAt = new LongEncodedFieldKey<>("createdAt",
				new MilliTimeFieldCodec());
	}

	public static class SamlAuthnRequestRedirectUrlFielder
	extends BaseDatabeanFielder<SamlAuthnRequestRedirectUrlKey,SamlAuthnRequestRedirectUrl>{

		public SamlAuthnRequestRedirectUrlFielder(){
			super(SamlAuthnRequestRedirectUrlKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(SamlAuthnRequestRedirectUrl databean){
			return List.of(
					new StringField(FieldKeys.redirectUrl, databean.redirectUrl),
					new LongEncodedField<>(FieldKeys.createdAt, databean.createdAt));
		}

	}

	@Override
	public Supplier<SamlAuthnRequestRedirectUrlKey> getKeySupplier(){
		return SamlAuthnRequestRedirectUrlKey::new;
	}

	public String getRedirectUrl(){
		return redirectUrl;
	}

	public MilliTime getCreated(){
		return createdAt;
	}

}
