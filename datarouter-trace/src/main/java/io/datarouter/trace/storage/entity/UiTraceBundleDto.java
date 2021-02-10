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
package io.datarouter.trace.storage.entity;

import java.util.List;

import io.datarouter.trace.storage.span.BaseTraceSpan;
import io.datarouter.trace.storage.thread.BaseTraceThread;
import io.datarouter.trace.storage.trace.BaseTrace;

public class UiTraceBundleDto{

	public final String account;
	public final BaseTrace<?,?,?> trace;
	public final List<? extends BaseTraceThread<?,?,?>> threads;
	public final List<? extends BaseTraceSpan<?,?,?,?>> spans;

	public UiTraceBundleDto(String account, BaseTrace<?,?,?> trace, List<? extends BaseTraceThread<?,?,?>> threads,
			List<? extends BaseTraceSpan<?,?,?,?>> spans){
		this.account = account;
		this.trace = trace;
		this.threads = threads;
		this.spans = spans;
	}

}
