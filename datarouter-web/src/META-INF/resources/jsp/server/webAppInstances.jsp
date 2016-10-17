<%@ include file="/WEB-INF/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Web App Settings</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<script>
	require(["sorttable"]);
	</script>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar.jsp" %>
<%@ include file="/jsp/menu/dr-navbar.jsp"%>
<div class="container">
	<h2 class="page-header">Web App Instances</h2>
	<div class="page-content-container page-content-thicktop page-single-column">
		<table class="sortable table table-bordered table-condensed"  style="border-collapse:collapse;">
			<tr>
				<th>Web App</th>
				<th>Server Name</th>
				<th>Server Type</th>
				<th>Server Public IP</th>
				<th>Startup Date</th>
				<th>Last Updated</th>
				<th>Build Date</th>
				<th>Commit Id</th>
			</tr>
			<c:forEach items="${webAppInstances}" var="webApp">
			<tr>
				<td>${webApp.key.webAppName}</td>
				<td>${webApp.key.serverName}</td>
				<td>${webApp.serverType}</td>
				<td>${webApp.serverPublicIp}</td>
				<td>${webApp.startupDatePrintable}</td>
				<td>${webApp.lastUpdatedTimeAgoPrintable}</td>
				<td>${webApp.buildDatePrintable}</td>
				<td>${webApp.commitId}</td>
			</tr>
			</c:forEach>
		</table>
	</div>
</div>
</body>
</html>