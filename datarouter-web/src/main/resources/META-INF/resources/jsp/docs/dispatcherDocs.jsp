<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>${apiName} API Documentation</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<style>
		.string { color: green; }
		.number { color: blue; }
		.boolean { color: red; }
		.null { color: magenta; }
		.key { color: black; }
		.card-header{
			font-size: 110%;
		}
		.copyable { cursor: pointer; }
	</style>
	<script>
		function fetchCsrf(obj) {
			const url = window.location.origin + window.location.pathname + "getCsrfIv?" + $.param(obj)
			return fetch(url, {method: 'GET'})
		}

		function fetchSignature(theParams, requestBody){
			const url = window.location.origin + window.location.pathname + "getSignature?" + $.param(theParams);
			const options = !!requestBody ? {method: 'POST', body: requestBody} : {method: 'GET'}
			return fetch(url, options);
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
						var json = formatJson(request.responseText);
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

		function formatJson(jsonText){
			const mark = 'JAVASCRIPT_INTEGER_OVERFLOW_PROTECTION'
			const replacer = num => Number.isSafeInteger(num) ? num : ('"' + mark + '(' + num + ')"')
			const longSafeText = jsonText
					.replace(/:\d{15,}\b/g, num => ':' + replacer(num.slice(1)))
					.replace(/^\d{15,}\b/g, replacer)
			return JSON.stringify(JSON.parse(longSafeText), null, 2)
					.replace(new RegExp('"' + mark + '\\((\\d+)\\)"', 'g'), match => match.slice(mark.length + 2, -2))
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
			function replaceQueryParam(name, value){
				const val = encodeURIComponent(value)
				const query = window.location.search
				const match = query.match('[?&]' + name + '=[^?&]*')
				let newSearch;
				if(match){
					const matchLen = match[0].length
					const addition = val.length ? name.length + 2 : 0
					const prev = match.input.slice(0, match.index + addition) // "...&key="
					const next = match.input.slice(match.index + matchLen)
					newSearch = (prev + val + next).replace(/^&/, '?')
				}else if(val.length){
					const prefix = query.length ? '&' : '?'
					newSearch = query + prefix + name + '=' + val
				}else{
					return
				}
				history.replaceState(null, null, newSearch)
			}

			$('.paramValue').change(function(){
				const $this = $(this)
				replaceQueryParam($this.attr('name'), $this.val())
			})

			$('a[data-toggle="collapse"]').click(function(){
				const card = $(this).parents('.card')
				if(card.find('.collapse').hasClass('show')){
					history.replaceState(null, null, window.location.href.split('?')[0])
				}else{
					history.replaceState(null, null, '?endpoint=' + encodeURIComponent($(this).data('url')))
					card.find('.paramValue').change()
				}
			})

			$('pre.json').each(($, pre) => {
				pre.innerHTML = syntaxHighlight(pre.innerHTML);
			});

			$('.copyable').click(event => {
				const copyable = $(event.currentTarget);
				$('#' + copyable.data('copydest')).text(copyable.text());
			});

			const urlMatchOnPageLoad = window.location.search.match(/endpoint=([^&]*)/)
			if(urlMatchOnPageLoad){
				const component = $('[data-url="' + decodeURIComponent(urlMatchOnPageLoad[1]) + '"]')
				const card = component.closest('.card')
				window.location.search
					.replace(/^\?/, '')
					.split('&')
					.map(param => param.split('=', 2))
					.map(pair => [pair[0], decodeURIComponent(pair[1])])
					.forEach(pair => {
						card.find('[name="' + pair[0] + '"]').val(pair[1])
					})
				$('html, body').animate({
					scrollTop: component.click().parent().offset().top
				})
			}
		})
	});
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container my-5">
		<h2>${apiName} API Documentation</h2>
		<div id="accordion">
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
				<div class="card" id="${endpoint.url}">
					<div class="card-header">
						<a tabindex="0" data-toggle="collapse" data-target="#${collapseId}" data-url="${endpoint.url}">
							${endpoint.url}
						</a>
						<span class="float-right text-muted">${endpoint.description}</span>
					</div>
					<div id="${collapseId}" class="collapse" data-parent="#accordion">
						<div class="card-body">
							<b>Handler:</b> ${endpoint.implementation}
							<h3>Parameters</h3>
							<form id="parameterForm" method="POST">
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
															<c:set var="paramName" value="requestBody"></c:set>
														</c:when>
														<c:otherwise>
															${parameter.name}
															<c:set var="paramName" value="${parameter.name}"></c:set>
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
															<textarea class="form-control paramValue" style="display:table-cell; width:100%" rows="10" id="${loop.index}-${paramName}" name="requestBody">${predefinedValue}</textarea>
														</c:when>
														<c:otherwise>
															<input class="form-control paramValue" style="display:table-cell; width:100%" type="text" id="${loop.index}-${paramName}" name="${parameter.name}"  placeholder="${note}" value="${predefinedValue}"/>
														</c:otherwise>
													</c:choose>
												</td>
												<td> ${parameter.type}
													<c:if test="${not empty parameter.description}">
														; ${parameter.description}
													</c:if>
													<c:if test="${not empty parameter.example}">
														<pre
															class="bg-light border p-2 json copyable"
															data-copydest="${loop.index}-${paramName}"
															>${parameter.example}</pre>
													</c:if>
												</td>
											</tr>
										</c:forEach>
									</table>
								</c:when>
								<c:otherwise>None</c:otherwise>
							</c:choose>
							<h3>Response</h3>
							<c:out value="${endpoint.response.type}" escapeXml="true"/>
							<c:if test="${empty endpoint.response}">
								Nothing
							</c:if>
							<c:if test="${not empty endpoint.response.example}">
								<pre class="bg-light border p-2 json">${endpoint.response.example}</pre>
							</c:if>
							<div>
								<button id="sendRequest" type="button" class="btn btn-primary"
										onclick="callApi('${rowClass}','${loop.index}','${endpoint.url}',
												'${hideAuth}')">Try It Out</button>
							</div>
							<div id="${responseDivId}" class="mt-3" style="display:none;">
								<h3>Response</h3>
								<div>
									<h4>Request URL</h4>
									<pre id="${requestUrlId}" class="bg-light border p-2">
								</div>
								<div>
									<h4>Request Body</h4>
									<pre id="${requestBodyId}" class="bg-light border p-2">
								</div>
								<div>
									<h4>Response Body</h4>
									<pre id="${jsonResponseId}" class="bg-light border p-2">
								</div>
								<div>
									<h4>Response Code</h4>
									<pre id="${responseCodeId}" class="bg-light border p-2">
								</div>
								<div>
									<h4>Response Header</h4>
									<pre id="${responseHeaderId}" class="bg-light border p-2">
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
