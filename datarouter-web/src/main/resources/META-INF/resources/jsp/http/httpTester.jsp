<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
<title>http tester</title>
<%@ include file="/jsp/generic/datarouterHead.jsp"%>
</head>

<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container mt-4">

		<h2>HTTP Tester Tool</h2>
		<form id="httpForm" action="${contextPath}${path}">
			<div class="form-row">
				<div class="col-sm-2">
					<select class="form-control" id="method" name="method">
						<option value="GET">GET</option>
						<option value="POST">POST</option>
						<option value="HEAD">HEAD</option>
					</select>
				</div>
				<div class="col">
					<input type="text" class="form-control" id="url" name="url"
						placeholder="URL" value='${url}'>
				</div>
				<div class="col">
					<button type="submit" class="btn btn-primary">Execute</button>
				</div>
			</div>

			<div id="headerRows">
				<div class="form-row mt-2" id="formDiv">
					<div class="col-sm-2">
						<input type="text" class="form-control" id="headerKey0"
							name="headerKey" placeholder="key">
					</div>
					<div class="col-sm-2">
						<input type="text" class="form-control" id="headerVal0"
							name="headerValue" placeholder="value">
					</div>
					<div class="col">
						<button type="button" class="btn btn-light" onClick="addHeader()">
							add header
						</button>
					</div>
				</div>
			</div>
			<div class="form-check mt-2">
				<input type="checkbox" class="form-check-input" id="followRedirects", name="followRedirects">
				<label class="form-check-label" for="followRedirects">Follow Redirects</label>
			</div>
			<div class="form-check mt-2">
				<input type="checkbox" class="form-check-input" id="proxyCheck", name="useProxy">
				<label class="form-check-label" for="proxyCheck">Use Proxy</label>
			</div>
			<a class="btn btn-light mt-2" data-toggle="collapse"
				data-target="#requestBodyDiv">Add Request Body</a>
			<div id="requestBodyDiv" class="form-group collapse mt-2">
				<div class="row">
					<div class="col-sm-3">
						<label class="text-black-50 justify-content-start mb-0">content-type</label>
						<select class="form-control" id="contentType" name="contentType">
							<option value="" disabled selected>set content-type</option>
							<option value="application/json">application/json</option>
							<option value="text/plain">text/plain</option>
						</select>
					</div>
				</div>
				<textarea class="form-control mt-2" rows="6" id="requestBody"
					name="requestBody" placeholder="request body">${requestBody}</textarea>
			</div>
		</form>
		<c:if test="${not empty url}">
			<h5 class="mt-4">Response Details</h5>
			<div class="table-responsive">
				<table class="table table-bordered table-striped mt-2"
					style="table-layout: fixed; word-wrap: break-word;">
					<tbody>
						<tr>
							<td><b>url</b></td>
							<td>${url}</td>
						</tr>
						<tr>
							<td><b>server name</b></td>
							<td>${serverName}</td>
						</tr>
						<tr>
							<td><b>duration (ms)</b></td>
							<td>${responseMs}</td>
						</tr>
						<c:if test="${not empty statusCode}">
							<tr>
								<td><b>status code</b></td>
								<td>${statusCode}</td>
							</tr>
						</c:if>
					</tbody>
				</table>
			</div>
		</c:if>
		<c:if test="${not empty headers}">
			<h5 class="mt-4">Response Headers</h5>
			<div class="table-responsive">
				<table class="table table-bordered table-striped mt-2"
					style="table-layout: fixed; word-wrap: break-word;">
					<thead>
						<tr>
							<th width="30%">Header</th>
							<th width="70%">Value</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="entry" items="${headers}">
							<tr>
								<td>${entry.key}</td>
								<td>${entry.value}</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</c:if>
		<c:if test="${not empty stackTrace}">
			<div id="responseBodyDiv">
				<h5 class="mt-4">Stack Trace</h5>
				<pre id="stackTrace" class="mt-2 bg-light text-dark p1 border">${stackTrace}</pre>
			</div>
		</c:if>
		<c:if test="${not empty responseBody}">
			<div id="responseBodyDiv">
				<h5 class="mt-4">Response Body</h5>
				<pre id="responseBody" class="mt-2 bg-light text-dark p1 border">${fn:escapeXml(responseBody)}</pre>
			</div>
		</c:if>
	</div>
</body>


<script>
	var headersmap = new Map();
	var method = '${method}';
	var allMap = '${headersMap}';
	var contentType = '${contentType}';
	var useProxy = '${useProxy}';
	var followRedirects = '${followRedirects}';
	const headerKeyPrefix = 'headerKey';
	const headerValPrefix = 'headerVal';
	const headerClosePrefix = 'headerClose';
	const headerDivPrefix = 'headerDiv';

	var allHeaders = 1;

	if (method != '') {
		var selector = document.getElementById("method");
		selector.value = method;
	}
	if (contentType != '') {
		var selector = document.getElementById("contentType");
		selector.value = contentType;
	}
	if(useProxy != ''){
		var checkbox = document.getElementById("proxyCheck");
		checkbox.checked = true;
	}
	if(followRedirects != ''){
		var checkbox = document.getElementById("followRedirects");
		checkbox.checked = true;
	}
	if (allMap != '') {
		var jsons = JSON.parse(allMap);
		for ( var key in jsons) {
			addHeadersForKeyValue(key, jsons[key]);
		}
	}
	var form = document.getElementById("httpForm");
	form.addEventListener("submit", function() {
		appendHeadersToForm();
	});

	function addHeadersForKeyValue(key, value) {
		if (document.getElementById(headerKeyPrefix + '0').value != '') {
			addHeader();
		}
		let index = allHeaders - 1;
		let headerKey = document.getElementById(headerKeyPrefix + index);
		let headerValue = document.getElementById(headerValPrefix + index);
		headerKey.value = key;
		headerValue.value = value;
	}

	function removeHeader(index) {
		document.getElementById(headerDivPrefix + index).remove();
		realignHeaderIds();
		allHeaders = allHeaders - 1;
	}

	function realignHeaderIds() {
		let formDiv = document.getElementById('headerRows');
		let foundMissing = false;
		for (i = 1; i < allHeaders; i++) {
			let headerDiv = document.getElementById(headerDivPrefix + i);
			if (foundMissing) {
				let headerKey = document.getElementById(headerKeyPrefix + i);
				let headerValue = document.getElementById(headerValPrefix + i);
				let headerClose = document
						.getElementById(headerClosePrefix + i);
				let index = i - 1;
				headerDiv.setAttribute('id', headerDivPrefix + index);
				headerKey.setAttribute('id', headerKeyPrefix + index)
				headerValue.setAttribute('id', headerValPrefix + index);
				headerClose.setAttribute('id', headerClosePrefix + index);
				headerClose.onclick = function() {
					removeHeader(index);
				}
			}
			if (headerDiv == null) {
				foundMissing = true;
			}
		}
	}

	function appendHeadersToForm() {
		var inputElement = document.createElement('input');
		inputElement.setAttribute('name', 'headers');
		inputElement.setAttribute('type', 'hidden');
		let headers = {};
		let formDiv = document.getElementById('headerRows');
		let childCount = formDiv.childElementCount;
		for (i = 0; i < childCount; i++) {
			let headerKey = document.getElementById(headerKeyPrefix + i);
			let headerValue = document.getElementById(headerValPrefix + i);
			if (headerKey != null && headerKey.value != ''
					&& headerValue != null && headerValue.value != '') {
				headers[headerKey.value] = headerValue.value;
			}
		}
		inputElement.value = JSON.stringify(headers);
		form.append(inputElement);
	}

	function addHeader() {
		let formDiv = document.getElementById('headerRows');
		let newDiv = document.createElement("div");
		newDiv.setAttribute("id", headerDivPrefix + formDiv.childElementCount);
		newDiv.setAttribute('class', 'form-row mt-2');

		let colKeyDiv = document.createElement("div");
		colKeyDiv.setAttribute("class", "col-sm-2");

		let colValDiv = document.createElement("div");
		colValDiv.setAttribute("class", "col-sm-2");

		let inputKey = document.createElement("input");
		inputKey.setAttribute("type", "text");
		inputKey.setAttribute("class", "form-control");
		inputKey.setAttribute("placeholder", "key");
		inputKey
				.setAttribute("id", headerKeyPrefix + formDiv.childElementCount);

		let inputValue = document.createElement("input");
		inputValue.setAttribute("type", "text");
		inputValue.setAttribute("class", "form-control");
		inputValue.setAttribute("placeholder", "value");
		inputValue.setAttribute("id", headerValPrefix
				+ formDiv.childElementCount);

		let xbutton = createCloseButton(formDiv.childElementCount);

		colKeyDiv.appendChild(inputKey);
		colValDiv.appendChild(inputValue);
		newDiv.appendChild(colKeyDiv);
		newDiv.appendChild(colValDiv);
		newDiv.appendChild(xbutton);

		formDiv.appendChild(newDiv);
		allHeaders = allHeaders + 1;
	}

	function createCloseButton(index) {
		var closebutton = document.createElement("button");
		closebutton.className = "close";
		closebutton.setAttribute("aria-label", "Close");
		closebutton.setAttribute("type", "button");
		let id = headerClosePrefix + index;
		closebutton.setAttribute('id', id);
		closebutton.onclick = function() {
			removeHeader(index);
		}

		var spanX = document.createElement("span");
		spanX.setAttribute("aria-hidden", "true");
		spanX.innerHTML = 'x';

		closebutton.appendChild(spanX);
		return closebutton;
	}
</script>