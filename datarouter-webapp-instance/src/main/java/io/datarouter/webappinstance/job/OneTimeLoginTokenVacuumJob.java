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
package io.datarouter.webappinstance.job;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.webappinstance.storage.onetimelogintoken.DatarouterOneTimeLoginTokenDao;

public class OneTimeLoginTokenVacuumJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(OneTimeLoginTokenVacuumJob.class);

	@Inject
	private DatarouterOneTimeLoginTokenDao dao;

	@Override
	public void run(TaskTracker tracker){
		//this job just gets rid of the day's leftover tokens
		dao.deleteAll();
		logger.warn("deleted all OneTimeLoginTokens");
	}

}
