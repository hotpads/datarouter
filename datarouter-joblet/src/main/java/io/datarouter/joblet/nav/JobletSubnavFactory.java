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
package io.datarouter.joblet.nav;

import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.joblet.config.DatarouterJobletPaths;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.nav.JobletExternalLinkBuilder.JobletExternalLinkBuilderSupplier;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.web.html.nav.Subnav;
import io.datarouter.web.html.nav.Subnav.Dropdown;
import io.datarouter.web.html.nav.Subnav.DropdownItem;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobletSubnavFactory{

	@Inject
	private DatarouterJobletPaths paths;
	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private JobletLocalLinkBuilder localLinkBuilder;
	@Inject
	private JobletExternalLinkBuilderSupplier externalLinkBuilder;

	public Subnav build(String contextPath){
		return new Subnav("Joblets", contextPath + paths.datarouter.joblets.list.toSlashedString())
				.add(monitoring(contextPath))
				.add(metrics())
				.add(status(contextPath))
				.add(requeueCreated(contextPath))
				.add(restart("Restart Failed", contextPath, JobletStatus.FAILED))
				.add(restart("Restart Interrupted", contextPath, JobletStatus.INTERRUPTED))
				.add(restart("Restart Running", contextPath, JobletStatus.RUNNING))
				.add(restart("Restart Timed Out", contextPath, JobletStatus.TIMED_OUT));
	}

	private Dropdown monitoring(String contextPath){
		return new Dropdown("Monitoring")
				.addItem("Exceptions", contextPath + paths.datarouter.joblets.exceptions.toSlashedString())
				.addItem("Local Running Joblets", contextPath + paths.datarouter.joblets.running.toSlashedString())
				.addItem("Sleeping (Test) Joblets",
						contextPath + paths.datarouter.joblets.createSleepingJoblets.toSlashedString())
				.addItem("Thread Counts", contextPath + paths.datarouter.joblets.threadCounts.toSlashedString());
	}

	private Dropdown metrics(){
		var dropdown = new Dropdown("Metrics");
		DatarouterJobletCounters.UI_LINK_NAMES_AND_PREFIXES.stream()
				.filter(twin -> externalLinkBuilder.get().counters(twin.prefix()).isPresent())
				.map(twin -> new DropdownItem(twin.linkName(), externalLinkBuilder.get().counters(
						twin.prefix()).get()))
				.forEach(dropdown::add);
		return dropdown;
	}

	private Dropdown status(String contextPath){
		var dropdown = new Dropdown("Status");
		JobletStatus.scan()
				.map(status -> new DropdownItem(
						status.persistentString,
						localLinkBuilder.listWithStatus(contextPath, status)))
				.forEach(dropdown::add);
		return dropdown;
	}

	private Dropdown requeueCreated(String contextPath){
		var dropdown = new Dropdown("Requeue Created");
		dropdown.add(new DropdownItem("All", localLinkBuilder.requeue(contextPath, null)).confirm());
		jobletTypeFactory.getAllTypes().stream()
				.map(type -> new DropdownItem(type.getDisplay(), localLinkBuilder.requeue(contextPath, type)))
				.map(DropdownItem::confirm)
				.forEach(dropdown::add);
		return dropdown;
	}

	private Dropdown restart(String dropdownName, String contextPath, JobletStatus status){
		var dropdown = new Dropdown(dropdownName);
		dropdown.add(new DropdownItem("All", localLinkBuilder.restart(contextPath, null, status)).confirm());
		jobletTypeFactory.getAllTypes().stream()
				.map(type -> new DropdownItem(type.getDisplay(), localLinkBuilder.restart(contextPath, type, status)))
				.map(DropdownItem::confirm)
				.forEach(dropdown::add);
		return dropdown;
	}

}
