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
package io.datarouter.web.util.http.exception;

@SuppressWarnings("serial")
public class Http400BadRequestException extends HttpException{

	private static final int CODE = 400;
	private static final String MESSAGE = "Bad Request";

	public Http400BadRequestException(){
		super(MESSAGE, CODE);
	}

	public Http400BadRequestException(String message){
		super(message, CODE);
	}

	public Http400BadRequestException(String message, Throwable cause){
		super(message, cause, CODE);
	}

	public Http400BadRequestException(Throwable cause){
		super(MESSAGE, cause, CODE);
	}

}
