<%@ include file="/WEB-INF/prelude.jspf" %>

<html>
<head>
	<title>Web App Settings</title>
<style>

.settingsTable{
	display:none;
}

table{
border: 1px solid gray;
border-collapse:collapse;
}
th{
border: 1px solid gray;
padding:1px;
}
td{
border: 1px solid gray;
padding:1px;
}
</style>

	<script>
	require(["util/ext/sorttable"], function() {});
	</script>
</head>
<body>
<div class="page-container">
<h2 class="page-title">Web App Settings</h2>
<div class="clearfix"></div>
<div class="page-content-container page-content-thicktop page-single-column">
		<h1>Web Apps</h1><br/>
		<table class="sortable">
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
			<c:forEach items="${webApps}" var="webApp">
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