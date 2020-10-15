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
package io.datarouter.exception.conveyors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.BaseConveyors;
import io.datarouter.exception.config.DatarouterExceptionSettingRoot;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordPublisherDao;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordPublisherDao;
import io.datarouter.instrumentation.exception.ExceptionRecordPublisher;
import io.datarouter.web.exception.ExceptionRecorder;

@Singleton
public class ExceptionQueueConveyors extends BaseConveyors{

	@Inject
	private DatarouterExceptionSettingRoot exceptionsSettings;
	@Inject
	private DatarouterExceptionRecordPublisherDao exceptionRecordPublisherDao;
	@Inject
	private DatarouterHttpRequestRecordPublisherDao httpRequestRecordPublisherDao;
	@Inject
	private ExceptionRecordPublisher exceptionRecordPublisher;
	@Inject
	private ExceptionRecorder exceptionRecorder;

	@Override
	public void onStartUp(){
		start(new ExceptionRecordQueueConveyor(
				"exceptionRecordQueuePublisher",
				exceptionsSettings.publishRecords,
				exceptionRecordPublisherDao.getGroupQueueConsumer(),
				exceptionRecordPublisher,
				exceptionsSettings.compactExceptionLoggingForConveyors,
				exceptionRecorder),
				1);
		start(new HttpRequestRecordQueueConveyor(
				"httpRequestRecordQueuePublisher",
				exceptionsSettings.publishRecords,
				httpRequestRecordPublisherDao.getGroupQueueConsumer(),
				exceptionRecordPublisher,
				exceptionsSettings.compactExceptionLoggingForConveyors,
				exceptionRecorder),
				1);
	}

}
