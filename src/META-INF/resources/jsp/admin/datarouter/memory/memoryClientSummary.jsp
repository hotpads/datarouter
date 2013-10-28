<%@ include file="/WEB-INF/prelude.jspf"%>
<html>
<head>
<title>DataRouter</title>
<%@ include file="/jsp/generic/css-import.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<div class="container">
		<h2>Datarouter</h2>
		<a href="/admin/dataRouter/menu.htm">DataRouter Home</a> &nbsp;&nbsp;>>&nbsp;&nbsp;
		 <a href="/admin/dataRouter/menu.htm?submitAction=inspectRouter&routerName=${param.routerName}">Router: ${param.routerName}</a> &nbsp;&nbsp;>>&nbsp;&nbsp; client: <b>${client.name}</b>
		<h3 style="width: 100%; border-bottom: 1px solid gray;">Memory Client Summary</h3>
		routerName:${param.routerName}<br /> clientName:${param.clientName}<br />
		<h3 style="width: 100%; border-bottom: 1px solid gray;">Stats</h3>
		TODO: track operation counts<br />
		<h3>Nodes</h3>
		<table class="table table-striped table-bordered table-hover table-condensed ">
			<tr>
				<td>name</td>
				<td>size</td>
			</tr>
			<c:forEach items="${nodes}" var="node">
				<tr>
					<td>${node.name}</td>
					<td>${node.size}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
	</div>
</body>
</html>