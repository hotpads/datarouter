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
package io.datarouter.gcp.gcs.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.td;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.gcp.gcs.client.GcsClient;
import io.datarouter.gcp.gcs.client.GcsClientManager;
import io.datarouter.gcp.gcs.config.DatarouterGcpGcsPaths;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.op.raw.read.DirectoryDto;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;

public class GcsBucketHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(GcsBucketHandler.class);

	public static final String P_client = "client";
	public static final String P_bucket = "bucket";
	public static final String P_prefix = "prefix";
	private static final String P_after = "after";
	private static final String P_offset = "offset";
	private static final String P_limit = "limit";
	public static final String P_currentDirectory = "currentDirectory";
	public static final String P_delimiter = "delimiter";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterClients clients;
	@Inject
	private GcsClientManager gcsClientManager;
	@Inject
	private DatarouterGcpGcsPaths paths;

	@Handler(defaultHandler = true)
	public Mav index(
			@Param(P_client) String client,
			@Param(P_bucket) String bucket,
			@Param(P_prefix) Optional<String> prefix,
			@Param(P_after) Optional<String> after,
			@Param(P_offset) Optional<Integer> offset,
			@Param(P_limit) Optional<Integer> limit,
			@Param(P_currentDirectory) Optional<Boolean> currentDirectory,
			@Param(P_delimiter) Optional<String> delimiter){

		var form = new HtmlForm(HtmlFormMethod.GET);
		form.addTextField()
				.withLabel("Client")
				.withName(P_client)
				.withPlaceholder("theClientName")
				.withValue(client);
		form.addTextField()
				.withLabel("Bucket")
				.withName(P_bucket)
				.withPlaceholder("the.bucket.name")
				.withValue(bucket);
		form.addTextField()
				.withLabel("Prefix")
				.withName(P_prefix)
				.withValue(prefix.orElse(""));
		form.addTextField()
				.withLabel("After")
				.withName(P_after)
				.withValue(after.orElse(""));
		form.addNumberField()
				.withLabel("Offset")
				.withName(P_offset)
				.withValue(offset.orElse(0));
		form.addNumberField()
				.withLabel("Limit")
				.withName(P_limit)
				.withValue(limit.orElse(100));
		form.addTextField()
				.withLabel("Delimiter")
				.withName(P_delimiter)
				.withValue(delimiter.orElse(""));
		form.addCheckboxField()
				.withLabel("currentDirectory")
				.withName(P_currentDirectory)
				.withChecked(currentDirectory.orElse(false));
		form.addButton()
				.withLabel("Submit")
				.withValue("");
		var htmlForm = Bootstrap4FormHtml.render(form)
				.withClass("card card-body bg-light");

		ClientId clientId = clients.getClientId(client);
		GcsClient gcsClient = gcsClientManager.getClient(clientId);
		TableTag table = null;
		List<DirectoryDto> objects = gcsClient.scanSubdirectories(
				bucket,
				prefix.orElse(null),
				after.orElse(null),
				params.optionalNotEmpty(P_delimiter).orElse(null),
				limit.orElse(100),
				currentDirectory.orElse(false))
				.list();
		int sizePadding = sizePadding(objects);
		table = new J2HtmlTable<DirectoryDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn("Key", object -> {
					String name = object.name();
					if(object.isDirectory()){
						return td(makePrefixLink(client, bucket, name, "/"));
					}
					return td(name);
				})
				.withHtmlColumn("Directory", object -> {
					boolean isDirectory = object.isDirectory();
					if(isDirectory){
						String href = new URIBuilder()
								.setPath(request.getContextPath() + paths.datarouter.clients.gcpGcs.countObjects
										.toSlashedString())
								.addParameter(P_client, client)
								.addParameter(P_bucket, bucket)
								.addParameter(P_prefix, object.name())
								.toString();
						return td(a("true, view count").withHref(href));
					}
					return td(String.valueOf(isDirectory));
				})
				.withHtmlColumn("Size", object -> {
					String commas = NumberFormatter.addCommas(object.size());
					String padded = StringTool.pad(commas, ' ', sizePadding);
					String escaped = padded.replace(" ", "&nbsp;");
					return td(rawHtml(escaped));
				})
				.withColumn("Last Modified", DirectoryDto::lastModified, Instant::toString)
				.withColumn("Storage Class", DirectoryDto::storageClass)
				.build(objects)
				.withStyle("font-family:monospace; font-size:.9em;");
		var content = div(
				htmlForm,
				h4(bucket),
				table)
				.withClass("container-fluid my-4");
		return pageFactory.startBuilder(request)
				.withTitle("Gcs Bucket")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	@Handler
	public Mav countObjects(
			@Param(P_client) String client,
			@Param(P_bucket) String bucket,
			@Param(P_prefix) Optional<String> prefix){
		ClientId clientId = clients.getClientId(client);
		GcsClient gcsClient = gcsClientManager.getClient(clientId);
		var count = new AtomicLong();
		var size = new AtomicLong();
		var message = new AtomicReference<String>();
		gcsClient.scanObjectsPaged(
				bucket,
				prefix.orElse(""))
				.concat(Scanner::of)
				.each(_ -> count.incrementAndGet())
				.each(obj -> size.addAndGet(obj.getSize()))
				.sample(10_000, true)
				.map(obj -> String.format("client=%s, bucket=%s, prefix=%s, count=%s, size=%s, through=%s",
						client,
						bucket,
						prefix.orElse(null),
						NumberFormatter.addCommas(count.get()),
						NumberFormatter.addCommas(size.get()),
						obj.getName()))
				.each(message::set)
				.forEach(logger::warn);
		return pageFactory.message(request, message.get());
	}

	private ATag makePrefixLink(String client, String bucket, String prefix, String delimiter){
		String href = new URIBuilder()
				.setPath(request.getContextPath() + paths.datarouter.clients.gcpGcs.listObjects
						.toSlashedString())
				.addParameter(P_client, client)
				.addParameter(P_bucket, bucket)
				.addParameter(P_prefix, prefix)
				.addParameter(P_delimiter, delimiter)
				.toString();
		return a(prefix)
				.withHref(href);
	}

	private static int sizePadding(List<DirectoryDto> objects){
		return Scanner.of(objects)
				.map(DirectoryDto::size)
				.map(NumberFormatter::addCommas)
				.map(String::length)
				.findMax(Comparator.naturalOrder())
				.orElse(0);
	}

}
