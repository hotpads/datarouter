<%@ include file="/WEB-INF/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
<title>DataRouter</title>
<%@ include file="/jsp/generic/datarouterHead.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>

	<div class="container">
		<h2>Datarouter</h2>
		<a href="${contextPath}/datarouter/routers">DataRouter Home</a>
		&nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; <a
			href="${contextPath}/datarouter/routers?submitAction=inspectRouter&routerName=${param.routerName}">Router:
			${param.routerName}</a> &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; client: <b>${client.name}</b>

		<h3>Redis Client Summary</h3>
		<b>routerName: </b>${param.routerName}<br /> <b>clientName: </b>${param.clientName}<br />
	</div>
	<div class="wide-container">
		<h3>Redis stats</h3>

		<c:forEach items="${redisStats}" var="stats">
			<h4>${stats.key}</h4>
			<table
				class="table table-striped table-bordered table-hover table-condensed">
				<tbody>

					<c:forEach items="${stats.value}" var="stat">
						<tr>
							<th>${stat.key}</th>
							<td>${stat.value}</td>
						</tr>

					</c:forEach>
				</tbody>
			</table>
		</c:forEach>

	</div>
</body>
</html>