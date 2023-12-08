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
package io.datarouter.instrumentation.response;

public class PublishingResponseDto{

	public static final String DISCARD_MESSAGE = "discard";

	public static final PublishingResponseDto REJECT = new PublishingResponseDto(false, "reject");
	public static final PublishingResponseDto DISCARD = new PublishingResponseDto(true, DISCARD_MESSAGE);
	public static final PublishingResponseDto NO_OP = new PublishingResponseDto(null, "noOp");
	public static final PublishingResponseDto SUCCESS = new PublishingResponseDto(true, "success");
	public static final PublishingResponseDto DISABLED_ON_CLIENT = new PublishingResponseDto(null,
			"disabled on client");
	public static final PublishingResponseDto UNAVAILABLE = new PublishingResponseDto(null,
			"unavailable");

	public final Boolean success;
	public final String message;

	private PublishingResponseDto(Boolean success, String message){
		this.success = success;
		this.message = message;
	}

	public void assertSuccess(){
		if(success == null || !success){
			throw new RuntimeException(message);
		}
	}

	public static PublishingResponseDto error(String errorMessage){
		return new PublishingResponseDto(false, errorMessage);
	}

	public static PublishingResponseDto error(Exception exception){
		return error(exception.getMessage());
	}

}
