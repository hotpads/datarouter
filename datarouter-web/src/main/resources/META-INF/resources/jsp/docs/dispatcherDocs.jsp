<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>${apiName} API Documentation</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<style>
	table {
		border-collapse: collapse;
		font-size: 1em;
	}
	th,td {
		border-bottom: 1px #D0D0D0  solid;
		border-top: 1px #D0D0D0  solid;
		padding: 5px;
		vertical-align: top;
	}
	th {
		text-align: left;
	}
	pre {
		padding: 5px;
		margin: 5px;
	}
	.string { color: green; }
	.number { color: blue; }
	.boolean { color: red; }
	.null { color: magenta; }
	.key { color: black; }
	.panel{
		position: relative;
	}
	.panel-anchor{
		position: absolute;
		right: 100%;
		color: #eee;
	}
	.panel-anchor:hover{
		color: gray;
		text-decoration: none !important;
	}
	.panel-anchor:active, .active{
		color: #007bff !important;
	}
	</style>
	<script type="text/javascript">

		function fetchCsrf(obj) {
			var url = window.location.href + "/getCsrfIv?" + $.param(obj);
			var req = {
				method: 'GET'
			};
			return fetch(url, req);
		}

		function fetchSignature(theParams, requestBody){
			var url = window.location.href + "/getSignature?" + $.param(theParams);
			if(requestBody != null){
				var req = {
					method : 'POST',
					body : requestBody
				}
			}else {
				var req = {
					method: 'GET'
				};
			}
			return fetch(url, req);
		}

		function getJson(response){
			return response.json();
		}

		function getParameters(paramRowClass, hideAuth) {
			var paramMap = {};
			var requestBody;
			var needsCsrf = false;
			var needsSignature = false;
			$('.' + paramRowClass).each(function (index, tableRow) {
				var paramName = $(tableRow).find('td.paramName').data('name');
				var paramValueInput = $(tableRow).find('.paramValue');
				var paramValue = paramValueInput.val();
				if (paramValueInput.is("textarea")) {
					requestBody = paramValue;
				}else if(paramName === "csrfIv" && hideAuth === 'true'){
					needsCsrf = true;
				}else if(paramName === "signature" && hideAuth === 'true'){
					needsSignature = true;
				}else if (paramValue) {
					paramMap[paramName] = paramValue;
				}
			});
			var result = {};
			if(needsCsrf && needsSignature){
				result = fetchCsrf(paramMap)
					.then(getJson)
					.then(params => fetchSignature(params, requestBody))
					.then(getJson);
			}else if(needsCsrf){
				result = fetchCsrf(paramMap).then(getJson);
			}else if(needsSignature){
				result = fetchSignature(paramMap).then(getJson);
			}else{
				result = Promise.resolve(paramMap);
			}
			return result;
		}

		function getBody(paramRowClass){
			var requestBody;
			$('.' + paramRowClass).each(function (index, tableRow) {
				var paramValueInput = $(tableRow).find('.paramValue');
				var paramValue = paramValueInput.val();
				if (paramValueInput.is("textarea")) {
					requestBody = paramValue;
					return;
				}
			});
			return requestBody;

		}

	require(['jquery', 'bootstrap'], function() {
		callApi = function(paramRowClass, loopIndex, url, hideAuth){
			let promise = getParameters(paramRowClass, hideAuth);
			promise.then(function(params){
			var responseDivId = 'responseDiv' + loopIndex;
			$("#" + responseDivId).hide();
			var requestBody = getBody(paramRowClass);
			console.log("request body", requestBody);
			var requestUrlId = 'requestUrl' + loopIndex;
			var requestBodyId = 'requestBody' + loopIndex;
			var jsonResponseId = 'jsonResponse' + loopIndex;
			var responseCodeId = 'responseCode' + loopIndex;
			var responseHeaderId = 'responseHeader' + loopIndex;
			document.getElementById(jsonResponseId).innerHTML = '';
			document.getElementById(requestUrlId).innerHTML = '';
			document.getElementById(requestBodyId).innerHTML = '';
			document.getElementById(responseCodeId).innerHTML = '';
			document.getElementById(responseHeaderId).innerHTML = '';

			var options;
			if(requestBody != null){
				options = {
					type: 'POST',
					data: requestBody,
					contentType: 'application/json'
				};
			}

			var requestUrl = '${contextPath}' + url + "?" + $.param(params);
			console.log("url  =", requestUrl);
			$.ajax(requestUrl, options)
				.complete(function(request){
					$("#" + responseDivId).show();
					if(request.getResponseHeader("content-type") == 'application/json'){
						var json = JSON.stringify(JSON.parse(request.responseText), undefined, 2);
						document.getElementById(jsonResponseId).innerHTML = syntaxHighlight(json);
					}else{
						document.getElementById(jsonResponseId).innerHTML = request.responseText;
					}
					document.getElementById(requestUrlId).innerHTML = getFullRequestUrl(requestUrl);
					document.getElementById(responseCodeId).innerHTML = request.status;
					document.getElementById(responseHeaderId).innerHTML = request.getAllResponseHeaders();
					if(requestBody !== undefined){
						document.getElementById(requestBodyId).innerHTML = requestBody;
					}
				});
			});
		}

		function getFullRequestUrl(partialUrl){
			if(location.origin === undefined){
				return partialUrl;
			}
			return location.origin + partialUrl;
		}
		function syntaxHighlight(json) {
			json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
			var regex = /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g;
			return json.replace(regex, function (match) {
				var cls = 'number';
				if (/^"/.test(match)) {
					if (/:$/.test(match)) {
						cls = 'key';
					} else {
						cls = 'string';
					}
				} else if (/true|false/.test(match)) {
					cls = 'boolean';
				} else if (/null/.test(match)) {
					cls = 'null';
				}
				return '<span class="' + cls + '">' + match + '</span>';
			});
		}

		$(function(){
			$('a[data-toggle="collapse"]').click(function(){
				if($(this).parents('.panel').find('.panel-collapse').hasClass('in')){
					history.pushState(null, null, window.location.href.replace(/#.*/, ''))
				}else{
					history.pushState(null, null, '#' + $(this).data('url'))
				}
			})

			const urlOnPageLoad = window.location.hash.slice(1);
			if(urlOnPageLoad){
				// browser should already have scrolled this element into view
				$('[data-url="' + urlOnPageLoad + '"]').click()
			}
		})
	});
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp"%>
	<div class="container">
		<h3>${apiName} API Documentation</h3>
		<div class="panel-group" id="accordion">
			<c:forEach var="endpoint" items="${endpoints}" varStatus="loop">
				<c:set var="collapseId" value="collapse${loop.index}"></c:set>
				<c:set var="panelId" value="panel${loop.index}"></c:set>
				<c:set var="responseDivId" value="responseDiv${loop.index}"></c:set>
				<c:set var="requestUrlId" value="requestUrl${loop.index}"></c:set>
				<c:set var="requestBodyId" value="requestBody${loop.index}"></c:set>
				<c:set var="jsonResponseId" value="jsonResponse${loop.index}"></c:set>
				<c:set var="responseCodeId" value="responseCode${loop.index}"></c:set>
				<c:set var="responseHeaderId" value="responseHeader${loop.index}"></c:set>
				<c:set var="rowClass" value="rowClass${loop.index}"></c:set>
				<div class="panel panel-default" id="${endpoint.url}">
					<div class="panel-heading">
						<h4 class="panel-title">
							<div class="clearfix">
								<span style="float: left;">
									<a data-parent="#accordion" data-toggle="collapse" data-target="#${collapseId}" data-url="${endpoint.url}">
										${endpoint.url}
									</a>
								</span>
								<span style="float: right;">${endpoint.description}</span>
							</div>
						</h4>
					</div>
					<div id="${collapseId}" class="panel-collapse collapse">
						<div class="panel-body">
							<h3>Parameters</h3>
							<form id="parameterForm" method="post">
							<c:choose>
								<c:when test="${not empty endpoint.parameters}">
									<table class=table>
										<tr>
											<th>Parameter</th>
											<th>Value</th>
											<th>Data Type</th>
										</tr>
										<c:forEach var="parameter" items="${endpoint.parameters}">
											<c:choose>
												<c:when test="${hideAuth && parameter.hidden}">
													<c:set var="note" value="(Automatically Configured)"></c:set>
												</c:when>
												<c:when test="${parameter.required}">
													<c:set var="note" value="(required)"></c:set>
												</c:when>
												<c:otherwise>
													<c:set var="note" value="(optional)"></c:set>
												</c:otherwise>
											</c:choose>
											<tr class="${rowClass}">
												<td class="paramName" data-name="${parameter.name}">
													<c:choose>
														<c:when test="${parameter.requestBody}">
															Request body
														</c:when>
														<c:otherwise>
															${parameter.name}
														</c:otherwise>
													</c:choose>
												</td>
												<c:choose>
													<c:when test="${apiKey != null && parameter.name.equals(apiKeyParameterName)}">
														<c:set var="predefinedValue" value="${apiKey}"></c:set>
													</c:when>
													<c:otherwise>
														<c:set var="predefinedValue" value=""></c:set>
													</c:otherwise>
												</c:choose>
												<td>
													<c:choose>
														<c:when test="${parameter.requestBody}">
															<textarea style="display:table-cell; width:100%" class="paramValue" rows="10">${predefinedValue}</textarea>
														</c:when>
														<c:otherwise>
															<input style="display:table-cell; width:100%" class="paramValue" type="text" id="${parameter.name}"  placeholder="${note}" value="${predefinedValue}"/>
														</c:otherwise>
													</c:choose>
												</td>
												<td> ${parameter.type}
													<c:if test="${not empty parameter.description}">
														; ${parameter.description}
													</c:if>
													<c:if test="${not empty parameter.example}">
														<pre>${parameter.example}</pre>
													</c:if>
												</td>
											</tr>
										</c:forEach>
									</table>
								</c:when>
								<c:otherwise>None</c:otherwise>
							</c:choose>
							<h3>Response</h3>
							${endpoint.response.type}
							<c:if test="${empty endpoint.response}">
								Nothing
							</c:if>
							<c:if test="${not empty endpoint.response.example}">
								<pre>${endpoint.response.example}</pre>
							</c:if>
							<div>
								<button class="table-box" id="sendRequest" type="button" class="btn btn-primary"
										onclick="callApi('${rowClass}','${loop.index}','${endpoint.url}',
												'${hideAuth}')">Try It Out</button>
							</div>
							<div id="${responseDivId}" style="display:none;">
								<h3>Response</h3>
								<div>
									<h4>Request URL</h4>
									<pre id="${requestUrlId}">
								</div>
								<div>
									<h4>Request Body</h4>
									<pre id="${requestBodyId}">
								</div>
								<div>
									<h4>Response Body</h4>
									<pre id="${jsonResponseId}">
								</div>
								<div>
									<h4>Response Code</h4>
									<pre id="${responseCodeId}">
								</div>
								<div>
									<h4>Response Header</h4>
									<pre id="${responseHeaderId}">
								</div>
							</div>
							</pre>
						</div>
					</div>
				</div>
			</c:forEach>
		</div>
	</div>
</body>
