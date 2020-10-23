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
package io.datarouter.aws.s3.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.td;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.client.S3ClientManager;
import io.datarouter.aws.s3.config.DatarouterAwsS3Paths;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalBoolean;
import io.datarouter.web.handler.types.optional.OptionalInteger;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.ContainerTag;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3BucketHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(S3BucketHandler.class);

	public static final String P_client = "client";
	public static final String P_bucket = "bucket";
	public static final String P_prefix = "prefix";
	private static final String P_after = "after";
	private static final String P_offset = "offset";
	private static final String P_limit = "limit";
	private static final String P_prefixes = "prefixes";
	private static final String P_delimiter = "delimiter";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterClients clients;
	@Inject
	private S3ClientManager s3ClientManager;
	@Inject
	private DatarouterAwsS3Paths paths;

	@Handler(defaultHandler = true)
	public Mav index(
			@Param(P_client) String client,
			@Param(P_bucket) String bucket,
			@Param(P_prefix) OptionalString prefix,
			@Param(P_after) OptionalString after,
			@Param(P_offset) OptionalInteger offset,
			@Param(P_limit) OptionalInteger limit,
			@Param(P_prefixes) OptionalBoolean prefixes,
			@Param(P_delimiter) OptionalString delimiter){

		var form = new HtmlForm()
				.withMethod("get");
		form.addTextField()
				.withDisplay("Client")
				.withName(P_client)
				.withPlaceholder("theClientName")
				.withValue(client);
		form.addTextField()
				.withDisplay("Bucket")
				.withName(P_bucket)
				.withPlaceholder("the.bucket.name")
				.withValue(bucket);
		form.addTextField()
				.withDisplay("Prefix")
				.withName(P_prefix)
				.withValue(prefix.orElse(""));
		form.addTextField()
				.withDisplay("After")
				.withName(P_after)
				.withValue(after.orElse(""));
		form.addTextField()
				.withDisplay("Offset")
				.withName(P_offset)
				.withValue(offset.orElse(0) + "");
		form.addTextField()
				.withDisplay("Limit")
				.withName(P_limit)
				.withValue(limit.orElse(100) + "");
		form.addTextField()
				.withDisplay("Delimiter")
				.withName(P_delimiter)
				.withValue(delimiter.orElse(""));
		form.addCheckboxField()
				.withDisplay("Prefixes")
				.withName(P_prefixes)
				.withChecked(prefixes.orElse(false));
		form.addButton()
				.withDisplay("Submit")
				.withValue("");
		var htmlForm = Bootstrap4FormHtml.render(form)
				.withClass("card card-body bg-light");

		ClientId clientId = clients.getClientId(client);
		DatarouterS3Client s3Client = s3ClientManager.getClient(clientId);
		ContainerTag table = null;
		if(prefixes.orElse(false)){
			List<String> prefixesList = s3Client.scanPrefixes(
					bucket,
					prefix.orElse(null),
					after.orElse(null),
					delimiter.orElse(null))
					.skip(offset.orElse(0))
					.limit(limit.orElse(100))
					.list();
			table = new J2HtmlTable<String>()
					.withClasses("sortable table table-sm table-striped my-4 border")
					.withColumn("Prefix", rowPrefix -> rowPrefix)
					.withHtmlColumn("Count", rowPrefix -> {
						String href = new URIBuilder()
								.setPath(request.getContextPath() + paths.datarouter.clients.awsS3.countObjects
										.toSlashedString())
								.addParameter(P_client, client)
								.addParameter(P_bucket, bucket)
								.addParameter(P_prefix, rowPrefix)
								.toString();
						return td(a("Count").withHref(href));
					})
					.build(prefixesList)
					.withStyle("font-family:monospace; font-size:.9em;");
		}else{
			List<S3Object> objects = s3Client.scanObjects(
					bucket,
					prefix.orElse(null),
					after.orElse(null),
					delimiter.orElse(null))
					.skip(offset.orElse(0))
					.limit(limit.orElse(100))
					.list();
			int sizePadding = sizePadding(objects);
			table = new J2HtmlTable<S3Object>()
					.withClasses("sortable table table-sm table-striped my-4 border")
					.withColumn("Key", object -> object.key())
					.withHtmlColumn("Size", object -> {
						String commas = NumberFormatter.addCommas(object.size());
						String padded = StringTool.pad(commas, ' ', sizePadding);
						String escaped = padded.replaceAll(" ", "&nbsp;");
						return td(rawHtml(escaped));
					})
					.withColumn("Last Modified", object -> object.lastModified())
					.withColumn("Storage Class", object -> object.storageClassAsString().toLowerCase())
					.build(objects)
					.withStyle("font-family:monospace; font-size:.9em;");
		}
		var content = div(
				htmlForm,
				h4(bucket),
				table)
				.withClass("container-fluid my-4");
		return pageFactory.startBuilder(request)
				.withTitle("S3 Bucket")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	@Handler
	public Mav countObjects(
			@Param(P_client) String client,
			@Param(P_bucket) String bucket,
			@Param(P_prefix) OptionalString prefix){
		ClientId clientId = clients.getClientId(client);
		DatarouterS3Client s3Client = s3ClientManager.getClient(clientId);
		var count = new AtomicLong();
		var size = new AtomicLong();
		var message = new AtomicReference<String>();
		s3Client.scanObjects(
				bucket,
				prefix.orElse(""))
				.each($ -> count.incrementAndGet())
				.each(obj -> size.addAndGet(obj.size()))
				.sample(10_000, true)
				.map(obj -> String.format("client=%s, bucket=%s, prefix=%s, count=%s, size=%s, through=%s",
						client,
						bucket,
						prefix.orElse(null),
						NumberFormatter.addCommas(count.get()),
						NumberFormatter.addCommas(size.get()),
						obj.key()))
				.each(message::set)
				.forEach(logger::warn);
		return pageFactory.message(request, message.get());
	}

	private static int sizePadding(List<S3Object> objects){
		return Scanner.of(objects)
				.map(S3Object::size)
				.map(NumberFormatter::addCommas)
				.map(String::length)
				.max(Comparator.naturalOrder())
				.orElse(0);
	}

}
