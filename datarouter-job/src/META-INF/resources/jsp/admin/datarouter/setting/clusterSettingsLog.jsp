<%@ include file="/WEB-INF/prelude.jspf"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<title> Cluster Settings Logs | Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>	
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<a href="?submitAction=browseSettings&name=${nodeName}" class="btn btn-primary">&rarr; Back to settings</a>
		<br>
		<h2 class="page-header">Log for clusterSetting - ${name}</h2>
	
		<table class="table table-bordered table-condensed settings-table">
			<tr style="background: black; color: white;">
				<th>Time stamp</th>
				<th>Name</th>
				<th>Scope</th>
				<th>ServerType</th>
				<th>Server Name</th>
				<th>Application</th>
				<th>Value</th>
				<th>Action</th>
				<th>Changed By</th>
			</tr>
			<c:forEach items="${records}" var="record">
				<tr>
					<td>${record.key.created}</td>
					<td>${record.key.name}</td>
					<td>${record.scope}</td>
					<td>${record.serverType}</td>
					<td>${record.serverName}</td>
					<td>${record.application}</td>
					<td>${record.value}</td>
					<td>${record.action}</td>
					<td>${record.changedBy}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>