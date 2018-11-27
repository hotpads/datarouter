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
	</style>
	<script type="text/javascript">
	require(['jquery', 'bootstrap'], function() {
		callApi = function(paramRowClass, loopIndex, url){
			var responseDivId = 'responseDiv' + loopIndex;
			$("#" + responseDivId).hide();
			var params = '';
			var requestBody;
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
			$('.' + paramRowClass).each(function(index, tableRow){
				var paramName = $(tableRow).find('td.paramName').data('name');
				var paramValueInput = $(tableRow).find('.paramValue');
				var paramValue = paramValueInput.val();
				if(paramValueInput.is("textarea")){
					requestBody = paramValue;
					return;
				}
				if(paramValue){
					var paramContent = (index == 0 ? '' : '&') + paramName + '=' + encodeURIComponent(paramValue);
					params += paramContent;
				}
			});
			var options;
			if(requestBody != null){
				options = {
					type: 'POST',
					data: requestBody,
					contentType: 'application/json'
				};
			}
			var requestUrl = '${contextPath}' + url + (params ? '?' + params : '');
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
				<div class="panel panel-default" id="${panelId}">
					<div class="panel-heading">
						 <h4 class="panel-title">
							<div class="clearfix">
								<span style="float: left;">
									<a data-parent="#accordion" data-toggle="collapse" data-target="#${collapseId}">
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
												<c:if test="${apiKey != null && parameter.name.equals(apiKeyParameterName)}">
													<c:set var="predefinedValue" value="${apiKey}"></c:set>
												</c:if>
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
													<c:if test="${parameter.description != null}">
														; ${parameter.description}
													</c:if>
												</td>
											</tr>
										</c:forEach>
									</table>
								</c:when>
								<c:otherwise>None</c:otherwise>
							</c:choose>
							<h3>Response</h3>
							<c:if test="${empty endpoint.response}">
								Nothing
							</c:if>
							<c:if test="${not empty endpoint.response}">
								<pre>${endpoint.response}</pre>
							</c:if>
							<div>
								<button class="table-box" id="sendRequest" type="button" class="btn btn-primary" onclick="callApi('${rowClass}','${loop.index}','${endpoint.url}')">Try It Out</button>
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