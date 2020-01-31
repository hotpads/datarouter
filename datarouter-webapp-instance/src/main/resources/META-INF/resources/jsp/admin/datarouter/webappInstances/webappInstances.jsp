<%@ include file="/jsp/generic/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Webapp Instances</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script>
		require(["sorttable"]);
	</script>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
<div class="container-fluid">
	<h2 class="mt-5 pb-2 mb-3 border-bottom">Webapp Instances</h2>
	<c:if test="${javaVersionStats.allCommon
		or servletVersionStats.allCommon
		or publicIpStats.allCommon
		or buildDateStats.allCommon
		or commitIdStats.allCommon
		or buildIdStats.allCommon}">
		<div class="alert alert-info">
			<c:if test="${javaVersionStats.allCommon}">All instances are running java version <strong>${javaVersionStats.mostCommon}</strong><br/></c:if>
			<c:if test="${servletVersionStats.allCommon}">All instances are running servlet container <strong>${servletVersionStats.mostCommon}</strong><br/></c:if>
			<c:if test="${publicIpStats.allCommon}">All instances have public ip <strong>${publicIpStats.mostCommon}</strong><br/></c:if>
			<c:if test="${lastUpdatedStats.allCommon}">All instances were updated <strong>${lastUpdatedStats.mostCommon}</strong><br/></c:if>
			<c:if test="${buildDateStats.allCommon}">All instances were built <strong>${buildDateStats.mostCommon}</strong><br/></c:if>
			<c:if test="${buildIdStats.allCommon}">All instances have build id <strong>${buildIdStats.mostCommon}</strong><br/></c:if>
			<c:if test="${commitIdStats.allCommon}">All instances have commit id <strong>${commitIdStats.mostCommon}</strong><br/></c:if>
		</div>
	</c:if>
	<c:if test="${not javaVersionStats.allCommon
		or not servletVersionStats.allCommon
		or not publicIpStats.allCommon
		or not buildDateStats.allCommon
		or not buildIdStats.allCommon
		or (webappStats.allCommon and not commitIdStats.allCommon)}">
		<div class="alert alert-warning">
			<c:if test="${not javaVersionStats.allCommon}">
				<div>
					Multiple Java versions running across instances
					${javaVersionStats.usageBreakdownHtml}
				</div>
			</c:if>
			<c:if test="${not servletVersionStats.allCommon}">
				<div>
					Multiple servlet container versions running across instances
					${servletVersionStats.usageBreakdownHtml}
				</div>
			</c:if>
			<c:if test="${not publicIpStats.allCommon}">
				<div>
					Multiple public IPs detected across instances
					${publicIpStats.usageBreakdownHtml}
				</div>
			</c:if>
			<c:if test="${not buildDateStats.allCommon}">
				<div>
					Multiple build dates detected across instances
					${buildDateStats.usageBreakdownHtml}
				</div>
			</c:if>
			<c:if test="${not buildIdStats.allCommon}">
				<div>
					Multiple build ids detected across instances
					${buildIdStats.usageBreakdownHtml}
				</div>
			</c:if>
			<c:if test="${not webappStats.allCommon and not commitIdStats.allCommon}">
				<div>
					There are multiple commit IDs running across instances
					${webappStats.usageBreakdownHtml}
				</div>
			</c:if>
		</div>
	</c:if>
	<table class="sortable table table-bordered table-sm table-striped" style="border-collapse:collapse;">
		<c:if test="${not webappStats.allCommon}"><caption>Number of webapps reporting: ${webappStats.uniqueCount}</caption></c:if> 
		<thead>
			<tr>
				<c:if test="${not webappStats.allCommon}"><th>Webapp</th></c:if>
				<th>Server Name</th>
				<th>Server Type</th>
				<c:if test="${not publicIpStats.allCommon}"><th>Public IP</th></c:if>
				<th>Private IP</th>
				<th>Uptime</th>
				<c:if test="${not lastUpdatedStats.allCommon}"><th>Last Updated</th></c:if>
				<c:if test="${not buildDateStats.allCommon}"><th>Build Date</th></c:if>
				<c:if test="${not buildIdStats.allCommon}"><th>Build Id</th></c:if>
				<c:if test="${not commitIdStats.allCommon}"><th>Commit Id</th></c:if>
				<c:if test="${not javaVersionStats.allCommon}"><th>Java Version</th></c:if>
				<c:if test="${not servletVersionStats.allCommon}"><th>Servlet Container</th></c:if>
				<th>Logs</th>
			</tr>
		</thead>
		<c:forEach items="${webappInstances}" var="webapp">
		<tr>
			<c:if test="${not webappStats.allCommon}"><td>${webapp.webappName}</td></c:if>
			<td>${webapp.serverName}</td>
			<td>${webapp.serverType}</td>
			<c:if test="${not publicIpStats.allCommon}"><td>${webapp.serverPublicIp}</td></c:if>
			<td><a href="https://${webapp.serverPrivateIp}${webapp.servletContextPath}">${webapp.serverPrivateIp}</a></td>
			<td sorttable_customkey="${webapp.startupDate.time}" title="${webapp.startupDate}">${webapp.upTimePrintable}</td>
			<c:if test="${not lastUpdatedStats.allCommon}">
				<td sorttable_customkey="${webapp.refreshedLast.time}" ${webapp.highlightRefreshedLast ? 'class="table-warning"' : ''}>
					${webapp.lastUpdatedTimeAgoPrintable}
				</td>
			</c:if>
			<c:if test="${not buildDateStats.allCommon}">
				<td sorttable_customkey="${webapp.buildDate.time}" title="${webapp.buildDatePrintable}"
					<c:if test="${webapp.oldWebappInstance}">class="table-danger"</c:if>
					<c:if test="${webapp.staleWebappInstance}">class="table-warning"</c:if>>
					${webapp.buildTimeAgoPrintable}
				</td>
			</c:if>
			<c:if test="${not buildIdStats.allCommon}"><td>
				<c:choose>
				<c:when test="${webapp.highlightBuildId}"><span class="badge badge-warning">${webapp.buildId}</span></c:when>
				<c:otherwise>${webapp.buildId}</c:otherwise>
				</c:choose>
			</td></c:if>
			<c:if test="${not commitIdStats.allCommon}"><td>
				<c:choose>
				<c:when test="${webapp.highlightCommitId}"><span class="badge badge-warning">${webapp.commitId}</span></c:when>
				<c:otherwise>${webapp.commitId}</c:otherwise>
				</c:choose>
			</td></c:if>
			<c:if test="${not javaVersionStats.allCommon}"><td>
				<c:choose>
				<c:when test="${webapp.highlightJavaVersion}"><span class="badge badge-warning">${webapp.javaVersion}</span></c:when>
				<c:otherwise>${webapp.javaVersion}</c:otherwise>
				</c:choose>
			</td></c:if>
			<c:if test="${not servletVersionStats.allCommon}"><td>
				<c:choose>
				<c:when test="${webapp.highlightServletContainerVersion}"><span class="badge badge-warning">${webapp.servletContainerVersion}</span></c:when>
				<c:otherwise>${webapp.servletContainerVersion}</c:otherwise>
				</c:choose>
			</td></c:if>
			<td>
				<a class="btn btn-link w-100 py-0" tabindex="0" href="${contextPath}${logPath}?webappName=${webapp.webappName}&serverName=${webapp.serverName}">
					<i class="far fa-list-alt"></i>
				</a>
			</td>
		</tr>
		</c:forEach>
	</table>
	</br>
	<div class="col-sm-6 offset-sm-3">
		<table class="table table-bordered table-sm">
			<tr><th>Legend</th></tr>
			<tr class="table-warning"><td>Instance build is older than 1 day</td></tr>
			<tr class="table-danger"><td>Instance build is older than 4 days</td></tr>
			<tr><td><span class="badge badge-warning">version</span> Value differs from related instances</td></tr>
		</table>
	</div>
</div>
</body>
</html>