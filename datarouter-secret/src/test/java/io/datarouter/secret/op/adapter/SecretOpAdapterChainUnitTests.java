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
package io.datarouter.secret.op.adapter;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.mockito.InOrder;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.secret.client.Secret;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.op.adapter.NamespacingAdapter.NamespacingMode;
import io.datarouter.secret.op.adapter.NamespacingAdapter.SecretNamespacingAdapter;
import io.datarouter.secret.service.SecretJsonSerializer;
import io.datarouter.secret.service.SecretNamespacer;

public class SecretOpAdapterChainUnitTests{

	private static final SecretNamespacer namespacer = new SecretNamespacer.DevelopmentNamespacer();
	private static final SecretJsonSerializer jsonSerializer = new SecretJsonSerializer.GsonToolJsonSerializer();
	private static final SecretOpReason reason = SecretOpReason.automatedOp(SecretOpSerializationAdapterUnitTests.class
			.getName());
	private static final SecretOpConfig manualConfig = SecretOpConfig.builder(reason)
			.useManualNamespace("manual")
			.build();

	@Test
	public void testSingleItemChain(){
		SecretValueExtractingAdapter<String> spy = spy(new SecretValueExtractingAdapter<>());
		SecretOpAdapter<TypedSecret<String>,String> chain = new SecretOpAdapterChain<>(spy);
		Secret secret = new Secret("name", "value");
		String result = chain.adapt(secret);
		Assert.assertEquals(result, "value");

		verify(spy, times(1)).adapt(secret);
		verifyNoMoreInteractions(spy);
	}

	@Test
	public void testMultiItemChain(){
		var nsSpy = spy(new SecretNamespacingAdapter(namespacer, manualConfig, NamespacingMode.REMOVING));
		var deserializeSpy = spy(new DeserializingAdapter<>(jsonSerializer, String.class, manualConfig));
		SecretValueExtractingAdapter<String> secretValueExtractingAdapter = new SecretValueExtractingAdapter<>();
		var extractSpy = spy(secretValueExtractingAdapter);
		SecretOpAdapterChain<Secret,String> chain = new SecretOpAdapterChain<>(nsSpy)
				.chain(deserializeSpy)
				.chain(extractSpy);
		Secret secret = new Secret("manual/name", "value");
		String result = chain.adapt(secret);

		Assert.assertEquals(result, "value");
		InOrder inOrder = inOrder(nsSpy, deserializeSpy, extractSpy);
		inOrder.verify(nsSpy, times(1)).adapt(secret);
		//Secret does not define equals, so I can't check the exact input
		inOrder.verify(deserializeSpy, times(1))
				.adapt(Mockito.argThat(input -> input.getName().equals("name") && input.getValue().equals("value")));
		inOrder.verify(extractSpy, times(1))
				.adapt(Mockito.argThat(input -> input.getName().equals("name") && input.getValue().equals("value")));
		inOrder.verifyNoMoreInteractions();
	}

}
