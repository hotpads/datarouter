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
package io.datarouter.instrumentation.trace;

import java.util.Collection;

public class Trace2BundleDto{

	public static final String CONSTANT = "";

	public final Trace2Dto traceDto;
	public final Collection<Trace2ThreadDto> traceThreadDtos;
	public final Collection<Trace2SpanDto> traceSpanDtos;

	public Trace2BundleDto(
			Trace2Dto traceDto,
			Collection<Trace2ThreadDto> traceThreadDtos,
			Collection<Trace2SpanDto> traceSpanDtos){
		this.traceDto = traceDto;
		this.traceThreadDtos = traceThreadDtos;
		this.traceSpanDtos = traceSpanDtos;
	}

	@SuppressWarnings("unused")
	private String nothing(){
		return new Trace2BundleDto(null, null, null).CONSTANT;
	}

}
