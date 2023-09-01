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
package io.datarouter.snapshotmanager;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;

import java.util.Optional;

import io.datarouter.filesystem.snapshot.benchmark.SnapshotBenchmark;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.SnapshotGroups;
import io.datarouter.util.Require;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class DatarouterSnapshotBenchmarkHandler extends BaseHandler{

	private static final String P_group = "group";
	private static final String P_numEntries = "numEntries";
	private static final String P_persist = "persist";
	private static final String P_delete = "delete";
	private static final String P_submitAction = "submitAction";

	private static final long DEFAULT_NUM_ENTRIES = 10_000_000L;
	private static final boolean DEFAULT_PERSIST = true;
	private static final boolean DEFAULT_DELETE = true;

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private SnapshotGroups groups;

	@Handler(defaultHandler = true)
	public Mav defaultHandler(
			@Param(P_group) Optional<String> optGroup,
			@Param(P_numEntries) Optional<String> optNumEntries,
			@Param(P_persist) Optional<Boolean> optPersist,
			@Param(P_delete) Optional<Boolean> optDelete,
			@Param(P_submitAction) Optional<String> submitAction){

		SnapshotGroup group = null;
		String errorGroup = null;
		if(optGroup.isPresent()){
			try{
				group = Require.notNull(groups.getGroup(optGroup.get()));
			}catch(Exception e){
				errorGroup = "Group not found";
			}
		}

		long numEntries = DEFAULT_NUM_ENTRIES;
		String errorNumEntries = null;
		try{
			numEntries = optNumEntries.map(Long::valueOf).orElse(numEntries);
		}catch(Exception e){
			errorNumEntries = "Must be an integer (long) number";
		}

		boolean persist = DEFAULT_PERSIST;
		String errorPersist = null;
		try{
			persist = optPersist.map(Boolean::valueOf).orElse(persist);
		}catch(Exception e){
			errorPersist = "Must be \"true\" or \"false\"";
		}

		boolean delete = DEFAULT_DELETE;
		String errorDelete = null;
		try{
			delete = optDelete.map(Boolean::valueOf).orElse(delete);
		}catch(Exception e){
			errorDelete = "Must be \"true\" or \"false\"";
		}

		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addTextField()
				.withLabel("Group")
				.withError(errorGroup)
				.withName(P_group)
				.withPlaceholder("group")
				.withValue(optGroup.orElse(null));
		form.addNumberField()
				.withLabel("Num Entries")
				.withError(errorNumEntries)
				.withName(P_numEntries)
				.withPlaceholder("numEntries")
				.withValue(numEntries);
		form.addTextField()
				.withLabel("Persist")
				.withError(errorPersist)
				.withName(P_persist)
				.withPlaceholder("true")
				.withValue(Boolean.toString(persist));
		form.addTextField()
				.withLabel("Delete")
				.withError(errorDelete)
				.withName(P_delete)
				.withPlaceholder("true")
				.withValue(Boolean.toString(persist));
		form.addButton()
				.withLabel("Run")
				.withValue("anything");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Datarouter Filesystem - Benchmark")
					.withContent(Html.makeContent(form))
					.buildMav();
		}

		int numInputThreads = Runtime.getRuntime().availableProcessors();
		int numWriterThreads = Runtime.getRuntime().availableProcessors();
		var benchmark = new SnapshotBenchmark(group, numInputThreads, numWriterThreads, numEntries, 10_000, persist);
		benchmark.execute();
		if(persist && delete){
			benchmark.cleanup();
		}
		benchmark.shutdown();
		return pageFactory.message(request, "Complete.  See logs for output");
	}

	private static class Html{

		public static DivTag makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Snapshot - Benchmark"),
					form,
					br())
					.withClass("container mt-3");
		}

	}

}
