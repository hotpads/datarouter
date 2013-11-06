<%@ include file="../../../generic/prelude.jspf"%>
<html>
<head>
<title>DataRouter</title>
<c:import url="/jsp/generic/head.jsp" />
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
	require([
           "bootstrap/bootstrap"
    ], function($) {});
</script>
<c:import url="/jsp/css/css-import.jsp" />
</head>
<body>
	<c:import url="/jsp/menu/dr-navbar.jsp" />
<body>
	<div class="container">
		<h2>Datarouter
		</h2>
		<a href="${contextPath}/dr/routers">DataRouter Home</a> &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; 
		<a href="${contextPath}/dr/routers?submitAction=inspectRouter&routerName=${param.routerName}">Router: ${param.routerName}</a>
		 &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; client: <b>${client.name}</b>
		<br />
		<br />
		<h3>Hibernate Client Summary</h3>
		<b>routerName: </b>${param.routerName}<br />
		<b>clientName: </b>${param.clientName}<br />
		<h3>HibernateClientStats</h3>
		${hibernateClientStats}<br />
		<h3>SessionFactoryStats</h3>
		<table class="table table-striped table-bordered table-hover table-condensed">
			<c:forEach items="${sessionFactoryStats}" var="stat">
				<tr>
					<td>${stat[0]}</td>
					<td>${stat[1]}</td>
				</tr>
			</c:forEach>
		</table>
		<h3>Physical Nodes</h3>
		<table class="table table-striped table-bordered table-hover table-condensed">
			<c:forEach items="${nodes}" var="node">
				<tr>
					<td><a style="color: black;" href="/analytics/dr/viewNodeData?submitAction=browseData&routerName=${param.routerName}
					&nodeName=${node.name}"> ${node.name} </a></td>
					<td>${node['class'].simpleName}</td>
					<td><a href="/analytics/dr/viewNodeData?submitAction=browseData&routerName=${param.routerName}
					&nodeName=${node.name}"> data </a></td>
					<td><a href="/admin/dataRouter/export.htm?submitAction=exportToS3&routerName=${param.routerName}
					&nodeName=${node.name}"> export to S3 </a></td>
				</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>