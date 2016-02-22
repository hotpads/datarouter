<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<html>
<head>
	<title>Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<h2>Datarouter
		</h2>
		<a href="${contextPath}/datarouter/routers">Datarouter Home</a> &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; 
		<a href="${contextPath}/datarouter/routers?submitAction=inspectRouter&routerName=${param.routerName}">Router: ${param.routerName}</a>
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
					<td><a style="color: black;" href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&routerName=${param.routerName}
					&nodeName=${node.name}"> ${node.name} </a></td>
					<td>${node['class'].simpleName}</td>
					<td><a href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&routerName=${param.routerName}
					&nodeName=${node.name}"> data </a></td>
 					<td><a href="/admin/datarouter/export.htm?submitAction=exportToS3&routerName=${param.routerName}
 					&nodeName=${node.name}"> export to S3 </a></td>
				</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>