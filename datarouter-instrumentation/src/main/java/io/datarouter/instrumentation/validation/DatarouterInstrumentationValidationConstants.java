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
package io.datarouter.instrumentation.validation;

import io.datarouter.types.Ulid;

/**
 * This class defines constants specific constants used by instrumentation types
 * They are to be used on the client side to reject creation of invalid types, as well
 * as on the server side to reject insertion of these invalid types into storage layers.
 */
public class DatarouterInstrumentationValidationConstants{

	public static final class MetricInstrumentationConstants{
		public static final int MAX_SIZE_METRIC_NAME = 255;
		public static final int MAX_SIZE_SERVER_NAME = 80;
	}

	public static final class ExceptionInstrumentationConstants{
		public static final int MAX_SIZE_BINARY_BODY = 10_000;
		public static final int MAX_SIZE_CONTENT_TYPE = 255;
		public static final int MAX_SIZE_ACCEPT_CHARSET = 255;
		public static final int MAX_SIZE_X_FORWARDED_FOR = 255;
		public static final int MAX_SIZE_PATH = 255;
		public static final int MAX_SIZE_ACCEPT_LANGUAGE = 5_000;
		public static final int MAX_SIZE_ORIGIN = 255;
		public static final int MAX_SIZE_PRAGMA = 255;
		public static final int MAX_SIZE_ACCEPT = 255;
		public static final int MAX_SIZE_HTTP_PARAMS = 5_000;
	}

	public static final class RelayInstrumentationConstants{
		public static final int MAX_SIZE_THREAD_ID = Ulid.LENGTH;
		public static final int MAX_SIZE_THREAD_SUBJECT = 255;
		public static final int MAX_SIZE_MESSAGE_ID = Ulid.LENGTH;
	}

}
