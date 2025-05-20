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
package io.datarouter.gcp.gcs;

import io.datarouter.instrumentation.metric.Metrics;

public class DatarouterGcsCounters{

	private static final String PREFIX = "Datarouter client Gcs";
	private static final String KEYWORD_ALL = "all";
	private static final String KEYWORD_BUCKET = "bucket";

	public static void inc(String bucket, GcsCounterSuffix suffix, long by){
		incNoBucket(suffix, by);
		incBucket(bucket, suffix, by);
	}

	public static void incNoBucket(GcsCounterSuffix suffix, long by){
		String name = String.join(" ", PREFIX, KEYWORD_ALL, suffix.suffix);
		Metrics.count(name, by);
	}

	public static void incBucket(String bucket, GcsCounterSuffix suffix, long by){
		String name = String.join(" ", PREFIX, KEYWORD_BUCKET, bucket, suffix.suffix);
		Metrics.count(name, by);
	}

	public enum GcsCounterSuffix{
		MULTIPART_ABORT_REQUESTS("multipartAbort requests"),
		MULTIPART_COMPLETE_REQUESTS("multipartComplete requests"),
		MULTIPART_CREATE_REQUESTS("multipartCreate requests"),
		MULTIPART_UPLOAD_BYTES("multipartUpload bytes"),
		MULTIPART_UPLOAD_REQUESTS("multipartUpload requests");

		public final String suffix;

		GcsCounterSuffix(String suffix){
			this.suffix = suffix;
		}
	}
}
