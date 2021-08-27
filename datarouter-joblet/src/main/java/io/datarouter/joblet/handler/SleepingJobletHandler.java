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
package io.datarouter.joblet.handler;

import static j2html.TagCreator.div;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.joblet.JobletPageFactory;
import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.model.JobletPackage;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.test.SleepingJoblet;
import io.datarouter.joblet.test.SleepingJoblet.SleepingJobletParams;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalBoolean;
import io.datarouter.web.handler.types.optional.OptionalInteger;
import io.datarouter.web.handler.types.optional.OptionalLong;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

/**
 * SleepingJoblets are for testing
 */
public class SleepingJobletHandler extends BaseHandler{

	private static final String TITLE = "Create Sleeping Joblets";

	private static final String P_numJoblets = "numJoblets";
	private static final String P_sleepMs = "sleepMs";
	private static final String P_executionOrder = "executionOrder";
	private static final String P_includeFailures = "includeFailures";
	private static final String P_failEveryN = "failEveryN";
	private static final String P_submitAction = "submitAction";

	@Inject
	private JobletService jobletService;
	@Inject
	private JobletPageFactory pageFactory;

	@Handler(defaultHandler = true)
	private Mav createSleepingJoblets(
			@Param(P_numJoblets) OptionalInteger numJoblets,
			@Param(P_sleepMs) OptionalLong sleepMs,
			@Param(P_executionOrder) OptionalInteger executionOrder,
			@Param(P_includeFailures) OptionalBoolean includeFailures,
			@Param(P_failEveryN) OptionalInteger failEveryN,
			@Param(P_submitAction) OptionalString submitAction){

		var form = new HtmlForm()
				.withMethod("post");
		form.addTextField()
				.withDisplay("Number of Joblets")
				.withName(P_numJoblets)
				.withPlaceholder("1000")
				.withValue(numJoblets.map(Object::toString).orElse(1000 + ""));
		form.addTextField()
				.withDisplay("Sleep Millis")
				.withName(P_sleepMs)
				.withPlaceholder("1000")
				.withValue(sleepMs.map(Object::toString).orElse(1000 + ""));
		form.addTextField()
				.withDisplay("Execution Order")
				.withName(P_executionOrder)
				.withPlaceholder(JobletPriority.DEFAULT.getExecutionOrder() + "")
				.withValue(executionOrder
						.map(Object::toString)
						.orElse(JobletPriority.DEFAULT.getExecutionOrder() + ""));
		form.addCheckboxField()
				.withDisplay("Include Failures")
				.withName(P_includeFailures)
				.withChecked(includeFailures.orElse(true));
		form.addTextField()
				.withDisplay("Fail Every N")
				.withName(P_failEveryN)
				.withPlaceholder(100 + "")
				.withValue(failEveryN.map(Object::toString).orElse(100 + ""));
		form.addButton()
				.withDisplay("Create Joblets")
				.withValue("anything");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle(TITLE)
					.withContent(makeContent(form))
					.buildMav();
		}

		JobletPriority priority = JobletPriority.fromExecutionOrder(executionOrder.get());
		List<JobletPackage> jobletPackages = new ArrayList<>();
		for(int i = 0; i < numJoblets.get(); ++i){
			int numFailuresForThisJoblet = 0;
			if(includeFailures.orElse(false)){
				boolean failThisJoblet = i % failEveryN.orElse(10) == 0;
				if(failThisJoblet){
					numFailuresForThisJoblet = JobletRequest.MAX_FAILURES + 3;//+3 to see if it causes a problem
				}
			}
			SleepingJobletParams params = new SleepingJobletParams(String.valueOf(i), sleepMs.get(),
					numFailuresForThisJoblet);
			int batchSequence = i;//specify this so joblets execute in precise order
			JobletPackage jobletPackage = JobletPackage.createDetailed(SleepingJoblet.JOBLET_TYPE, priority, Instant
					.now(), batchSequence, true, null, null, params);
			jobletPackages.add(jobletPackage);
		}
		jobletService.submitJobletPackages(jobletPackages);
		return pageFactory.message(request, String.format("created %s @%s ms each", numJoblets.get(), sleepMs.get()));
	}

	public ContainerTag<?> makeContent(HtmlForm htmlForm){
		var form = Bootstrap4FormHtml.render(htmlForm)
				.withClass("card card-body bg-light");
		return div(TagCreator.h4(TITLE), form)
				.withClass("container my-3");
	}

}
