<%@ include file="/WEB-INF/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter ${param.clientName}</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>

	<div class="container">
		<h2>Datarouter ${param.clientName}</h2>
		<a href="${contextPath}/datarouter/routers">
			Datarouter Home
		</a>
		&nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp;
		<a href="${contextPath}/datarouter/routers?submitAction=inspectRouter&routerName=${param.routerName}">
			Router: ${param.routerName}
		</a>
		&nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp;
		<b>Client: </b>
		${param.clientName}
		<br>
	</div>
	<div class="wide-container">
		<h3>Memcached stats</h3>

		<c:forEach items="${memcachedStats}" var="stats">
			<h4>${stats.key}</h4>
			<table
				class="table table-striped table-bordered table-hover table-condensed">
				<!-- 				<thead> -->
				<!-- 					<tr> -->
				<%-- 						<c:forEach items="${stats.value}" var="stat"> --%>

				<%-- 						</c:forEach> --%>
				<!-- 					</tr> -->
				<!-- 				</thead> -->
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