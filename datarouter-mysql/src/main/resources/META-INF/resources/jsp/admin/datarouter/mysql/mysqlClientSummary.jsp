<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp"%>
	<div class="container">
		<h2>Datarouter
		</h2>
		<a href="${contextPath}/datarouter">Datarouter Home</a>
		 &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; client: <b>${param.clientName}</b>
		<br />
		<br />
		<h3>Client Summary</h3>
		<b>routerName: </b>${param.routerName}<br />
		<b>clientName: </b>${param.clientName}<br />

		<h3>Physical Nodes</h3>
		<table class="table table-striped table-bordered table-hover table-condensed">
			<c:forEach items="${nodes}" var="node">
				<tr>
					<td><a style="color: black;" href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&nodeName=${node.name}"> ${node.name} </a></td>
					<td>${node['class'].simpleName}</td>
					<td><a href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&nodeName=${node.name}"> data </a></td> 		
				</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>
