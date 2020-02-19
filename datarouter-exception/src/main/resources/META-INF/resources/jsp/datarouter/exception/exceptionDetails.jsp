<%@ include file="/jsp/generic/prelude.jspf"%>
<c:set var="period" value="5000"/>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
	<meta charset="utf-8">
	<script data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
	<script>
		require(["bootstrap/bootstrap"]);
	</script>
	<title>Exception records</title>
	<style>
		table.http{
			width: 100%;
			table-layout: fixed;
		}

		table.http th,
		table.http td{
			overflow-x: auto;
		}

		table.http th:first-child,
		table.http td:first-child{
			width: 300px;
		}
	</style>
	<script>
		require(['jquery'], function(){
			$(() => {
				try{
					const parsedJson = JSON.parse($('#request-body').text())
					const formattedJson = JSON.stringify(parsedJson, null, 2) // formats with tabs and newlines
					$('#request-body-formatted').text(formattedJson)
					$('#request-body-toggle').show() // show toggle button
					$('.request-body').toggle() // toggle both <pre> blocks
				}catch(e){
					console.info(e) // can't parse as json
				}
			})
		})
	</script>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
<nav class="navbar navbar-light bg-light">
	<div class="container justify-content-between">
		<span class="navbar-item"><a class="btn btn-primary" href="${contextPath}${browsePath}"><i class="fas fa-angle-left mr-1"></i>All Exceptions</a></span>
		<form class="form-inline ml-auto" action="${contextPath}${detailsPath}?exceptionRecord=${exceptionRecord}">
			<input class="form-control mr-sm-2" type="search" placeholder="exception record ID" name="exceptionRecord">
			<input type="submit" class="btn btn-primary" value="Search">
		</form>
	</div>
</nav>
<div class="container my-4">
	<c:if test="${empty exceptionRecord}">
		<div class="alert alert-danger">Exception record not found</div>
	</c:if>
	<c:if test="${not empty exceptionRecord}">
		<h2>
			Exception
			<h5 class="text-nowrap text-monospace d-block"><small class="text-muted">ID: </small>${exceptionRecord.id}</h5>
		</h2>
		<p>
			<strong>
				${exceptionRecord.type}
			</strong>
			(<a href="${exceptionRecord.metricLink}">counter</a>)
			at
			<strong>${exceptionRecord.created}</strong>
			<c:if test="${not empty httpRequestRecord}">
				after
				<strong title="Received at <fmt:formatDate pattern="HH:mm:ss.S" value="${httpRequestRecord.receivedAt}"/>">
					${httpRequestRecord.duration} ms
				</strong>
			</c:if>
			on
			<strong>${exceptionRecord.serverName}</strong>
			version
			<strong>${exceptionRecord.appVersion}</strong>
		</p>
		<a tabindex="-1" onclick="$('.stack').toggle(); $(this).blur().children().toggle()" class="btn-block w-100">
			<span>Show more</span>
			<span style="display: none">Show less</span>
		</a>
		<pre class="stack bg-light border rounded p-2">${shortStackTrace}</pre>
		<pre class="stack bg-light border rounded p-2" style="display: none;">${coloredStackTrace}</pre>
		<strong>Location: </strong>${exceptionRecord.exceptionLocation}
		<br>
		<strong>Call origin: </strong>${exceptionRecord.callOrigin}
		(<a href="${exceptionRecord.callOriginLink}">counter</a>)
		<br>
		(<a href="${exceptionRecord.exactMetricLink}">exact counter</a>)

		<c:if test="${empty httpRequestRecord}">
			<div class="alert alert-warning">No http request record found</div>
		</c:if>
		<c:if test="${not empty httpRequestRecord}">
			<h3 class="mt-5">Http request (${httpRequestRecord.httpMethod})</h3>
			<p><strong>URL: </strong><a href="${httpRequestRecord.url}">${httpRequestRecord.url}</a></p>
			<div>
				<c:set var="paramsMap" value="${httpRequestRecord.httpParamsMap}"/>
				<c:choose>
					<c:when test="${empty paramsMap}">
						<strong>No http parameters</strong>
					</c:when>
					<c:otherwise>
						<table class="http table-bordered">
							<thead class="table-info">
								<tr>
									<th>Parameter</th>
									<th>Value(s)</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="entry" items="${paramsMap}">
									<tr>
										<td>${entry.key}</td>
										<td>${fn:join(entry.value, ',')}</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</c:otherwise>
				</c:choose>
			</div>
			<div>
				<div class="d-flex justify-content-between">
					<strong>Body : </strong>
					<a id="request-body-toggle" tabindex="-1" onclick="$(this).children().toggle(); $('.request-body').toggle()" style="display: none">
						<span>Show original</span>
						<span style="display: none">Show parsed</span>
					</a>
				</div>
				<c:if test="${not empty httpRequestRecord.stringBody}">
					<pre class="bg-light p-2 rounded border request-body" id="request-body">${fn:escapeXml(httpRequestRecord.stringBody)}</pre>
					<pre class="bg-light p-2 rounded border request-body" id="request-body-formatted" style="display: none"></pre>
				</c:if>
			</div>
			<h3 class="mt-5">Client</h3>
			<p>
				<strong>Ip: </strong>${httpRequestRecord.ip}<br>
				<strong>User: </strong>${httpRequestRecord.userToken}<br>
				<strong>Roles: </strong>${httpRequestRecord.userRoles}
			</p>
			<div class="mb-4">
				<c:set var="cookiesMap" value="${httpRequestRecord.cookiesMap}"/>
				<c:choose>
					<c:when test="${empty cookiesMap}">
						<strong>No cookie</strong>
					</c:when>
					<c:otherwise>
						<table class="http table-bordered">
							<thead class="table-info">
								<tr>
									<th>Cookie</th>
									<th>Value</th>
								</tr>
							</thead>
							<tbody>
							<c:forEach var="entry" items="${cookiesMap}">
								<tr>
									<td>${entry.key}</td>
									<td>${entry.value}</td>
								</tr>
							</c:forEach>
							</tbody>
						</table>
					</c:otherwise>
				</c:choose>
			</div>
			<table class="http table-bordered">
				<thead class="table-info">
					<tr>
						<th>Header</th>
						<th>Value</th>
					</tr>
				</thead>
				<tbody>
				<c:forEach var="entry" items="${httpRequestRecord.headers}">
					<tr>
						<td>${entry.key}</td>
						<td>${entry.value}</td>
					</tr>
				</c:forEach>
				<c:set var="others" value="${httpRequestRecord.otherHeadersMap}"/>
				<c:if test="${not empty others}">
					<tr class="table-info">
						<th colspan="2">Others</th>
					</tr>
					<c:forEach var="entry" items="${others}">
						<tr>
							<td>${entry.key}</td>
							<td>${fn:join(entry.value, ',')}</td>
						</tr>
					</c:forEach>
				</c:if>
				</tbody>
			</table>
		</c:if>
	</c:if>
</div>
</body>
</html>