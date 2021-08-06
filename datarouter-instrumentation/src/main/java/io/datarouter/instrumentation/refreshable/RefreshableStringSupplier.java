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
package io.datarouter.instrumentation.refreshable;

import java.time.Duration;
import java.util.function.Supplier;

public class RefreshableStringSupplier extends BaseMemoizedRefreshableSupplier<String>{

	private final Supplier<String> supplier;

	public RefreshableStringSupplier(Supplier<String> supplier){
		this(supplier, Duration.ofSeconds(30L));
	}

	public RefreshableStringSupplier(Supplier<String> supplier, Duration minimumTtl){
		super(minimumTtl);
		this.supplier = supplier;
		refresh();
	}

	public RefreshableStringSupplier(Supplier<String> supplier, Duration minimumTtl, Duration attemptInterval){
		super(minimumTtl, attemptInterval);
		this.supplier = supplier;
		refresh();
	}

	@Override
	protected String readNewValue(){
		return supplier.get();
	}

	@Override
	protected String getIdentifier(){
		return supplier.toString();
	}

}
