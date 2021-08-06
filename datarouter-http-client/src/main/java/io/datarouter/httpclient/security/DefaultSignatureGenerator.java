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
package io.datarouter.httpclient.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import io.datarouter.instrumentation.refreshable.RefreshableStringSupplier;
import io.datarouter.instrumentation.refreshable.RefreshableSupplier;

public class DefaultSignatureGenerator implements SignatureGenerator{

	private static final String HASHING_ALGORITHM = "SHA-256";

	private final Supplier<String> saltSupplier;

	public DefaultSignatureGenerator(Supplier<String> saltSupplier){
		this.saltSupplier = saltSupplier;
	}

	@Override
	public RequestSignatureDto getHexSignature(Map<String,String> params, HttpEntity entity){
		return getHexSignatureWithoutSettingParameterOrder(new TreeMap<>(params), entity);
	}

	@Override
	public RequestSignatureDto getHexSignature(Map<String,String> params){
		return getHexSignatureWithoutSettingParameterOrder(new TreeMap<>(params), null);
	}

	public RequestSignatureDto getHexSignatureWithoutSettingParameterOrder(Map<String,String> map, HttpEntity entity){
		// TODO signature length should be constant. currently signature length is proportional to number of parameters.
		ByteArrayOutputStream signature = new ByteArrayOutputStream();
		List<String> partToEncode = new ArrayList<>();
		try{
			MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHM);
			for(Entry<String,String> entry : map.entrySet()){
				String parameterName = entry.getKey();
				if(parameterName.equals(SecurityParameters.SIGNATURE) || "submitAction".equals(parameterName)){
					continue;
				}
				String value = entry.getValue();
				String keyValue = parameterName.concat(value == null ? "" : value);
				String keyValueSalt = keyValue.concat(saltSupplier.get());
				partToEncode.add(keyValue + obfuscate(saltSupplier.get()));
				md.update(keyValueSalt.getBytes(StandardCharsets.UTF_8));
				signature.write(md.digest());
			}
			if(entity != null){
				byte[] bytes = EntityUtils.toByteArray(entity);
				md.update(bytes);
				md.update(saltSupplier.get().getBytes(StandardCharsets.UTF_8));
				signature.write(md.digest());
			}
		}catch(IOException | NoSuchAlgorithmException e){
			throw new RuntimeException(e);
		}
		return new RequestSignatureDto(Hex.encodeHexString(signature.toByteArray()), partToEncode);
	}

	private static String obfuscate(String string){
		char[] value = new char[string.length()];
		int limitToHide = string.length() - 4;
		for(int i = 0; i < string.length(); i++){
			value[i] = i < limitToHide ? '*' : string.charAt(i);
		}
		return new String(value);
	}

	public static class RefreshableDefaultSignatureGenerator extends DefaultSignatureGenerator
	implements RefreshableSignatureGenerator{

		private final RefreshableSupplier<String> supplier;

		public RefreshableDefaultSignatureGenerator(RefreshableSupplier<String> supplier){
			super(supplier);
			this.supplier = supplier;
		}

		public RefreshableDefaultSignatureGenerator(Supplier<String> supplier){
			this(new RefreshableStringSupplier(supplier::get));
		}

		@Override
		public Instant refresh(){
			return supplier.refresh();
		}

	}

}
