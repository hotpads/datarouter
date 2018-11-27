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
package io.datarouter.instrumentation.response;

public class PublishingResponseDto{

	public static final String
			SUCCESS_MESSAGE = "success",
			NO_OP_MESSAGE = "noOp",
			DISABLED_ON_CLIENT_MESSAGE = "disabled on client",
			DISABLED_ON_SERVER_MESSAGE = "disabled on server";

	public final Boolean success;
	public final String message;

	private PublishingResponseDto(Boolean success, String message){
		this.success = success;
		this.message = message;
	}

	public void assertSuccess(){
		if(!success){
			throw new RuntimeException(message);
		}
	}

	public static PublishingResponseDto success(){
		return new PublishingResponseDto(true, SUCCESS_MESSAGE);
	}

	public static PublishingResponseDto error(String errorMessage){
		return new PublishingResponseDto(false, errorMessage);
	}

	public static PublishingResponseDto noOp(){
		return new PublishingResponseDto(null, NO_OP_MESSAGE);
	}

	public static PublishingResponseDto disabledOnClient(){
		return new PublishingResponseDto(null, DISABLED_ON_CLIENT_MESSAGE);
	}

	public static PublishingResponseDto disabledOnServer(){
		return new PublishingResponseDto(null, DISABLED_ON_SERVER_MESSAGE);
	}

}
