<%@ include file="/WEB-INF/prelude.jspf"%>
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
	th{
		text-align: left;
	}
	pre {outline: 1px solid #ccc; padding: 5px; margin: 5px; }
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
			var paramUrl = '';
			var requestUrlId = 'requestUrl' + loopIndex;
			var jsonResponseId = 'jsonResponse' + loopIndex;
			var responseCodeId = 'responseCode' + loopIndex;
			var responseHeaderId = 'responseHeader' + loopIndex;
			document.getElementById(jsonResponseId).innerHTML = '';
			document.getElementById(requestUrlId).innerHTML = '';
			document.getElementById(responseCodeId).innerHTML = '';
			document.getElementById(responseHeaderId).innerHTML = '';
			$('.' + paramRowClass).each(function(index, tableRow){
				var paramName = $(tableRow).find('td.paramName').html();
				var paramValue = $(tableRow).find('input.paramValue').val();
				if(paramValue){
					var paramContent = (index == 0 ? '' : '&') + paramName + '=' + paramValue;
					paramUrl += paramContent;
				}
			});
			var requestUrl = '/listing-tools' + url + (paramUrl ? '?' + paramUrl : '');
			$.get(requestUrl, function(response, textStatus, request){
				$("#" + responseDivId).show();
				var json = JSON.stringify(response, undefined, 2);
				document.getElementById(jsonResponseId).innerHTML = syntaxHighlight(json);
				document.getElementById(requestUrlId).innerHTML = getFullRequestUrl(requestUrl);
				document.getElementById(responseCodeId).innerHTML = request.status;
				document.getElementById(responseHeaderId).innerHTML = request.getAllResponseHeaders();
			})
			.fail(function(response) {
				$("#" + responseDivId).show();
				document.getElementById(jsonResponseId).innerHTML = JSON.stringify(response.responseText);
				document.getElementById(requestUrlId).innerHTML = getFullRequestUrl(requestUrl);
				document.getElementById(responseCodeId).innerHTML = response.status;
				document.getElementById(responseHeaderId).innerHTML = response.getAllResponseHeaders();
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
		    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
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
				<c:set var="jsonResponseId" value="jsonResponse${loop.index}"></c:set>
				<c:set var="responseCodeId" value="responseCode${loop.index}"></c:set>
				<c:set var="responseHeaderId" value="responseHeader${loop.index}"></c:set>
				<c:set var="rowClass" value="rowClass${loop.index}"></c:set>
				<div class="panel panel-default" id="${panelId}">
			        <div class="panel-heading">
			             <h4 class="panel-title">
				           	<div class="clearfix">
   								<span style="float: left;"><a data-parent="#accordion" data-toggle="collapse" data-target="#${collapseId}">${endpoint.url}</a></span> 
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
												<td class="paramName">${parameter.name}</td>
												<td><input style="display:table-cell; width:100%" class="paramValue" type="text" id="${parameter.name}"  placeholder="${note}"/></td>
												<td>${parameter.type}</td>
											</tr>
										</c:forEach>
									</table>
								</c:when>
								<c:otherwise>None</c:otherwise>
							</c:choose>
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