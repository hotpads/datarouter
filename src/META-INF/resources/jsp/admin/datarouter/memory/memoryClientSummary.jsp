<%@ include file="/WEB-INF/prelude.jspf"%>
<html>
<head>
<title>DataRouter</title>
<%@ include file="/jsp/generic/head.jsp"%>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
	require([
           "bootstrap/bootstrap"
    ], function($) {});
</script>
<%@ include file="/jsp/css/css-import.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<div class="container">
		<h2>Datarouter</h2>
		<a href="${contextPath}/dr/routers">DataRouter Home</a> &nbsp;&nbsp;>>&nbsp;&nbsp;
		 <a href="${contextPath}/dr/routers?submitAction=inspectRouter&routerName=${param.routerName}">Router: ${param.routerName}</a> &nbsp;&nbsp;>>&nbsp;&nbsp; client: <b>${client.name}</b>
		<h3 style="width: 100%; border-bottom: 1px solid gray;">Memory Client Summary</h3>
		<b>routerName: </b${param.routerName}<br /> 
		<b>clientName: </b${param.clientName}<br />
		<h3 style="width: 100%; border-bottom: 1px solid gray;">Stats</h3>
		TODO: track operation counts<br />
		<h3>Nodes</h3>
		<table class="table table-striped table-bordered table-hover table-condensed ">
		<thead>
			<th>
				<td>name</td>
				<td>size</td>
			</th>
			</thead>
			<tbody>
			<c:forEach items="${nodes}" var="node">
				<tr>
					<td>${node.name}</td>
					<td>${node.size}</td>
				</tr>
			</c:forEach>
			</tbody>
		</table>
	</div>
	</div>
</body>
</html>