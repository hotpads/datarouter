<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>DataRouter</title>
<%@ include file="/jsp/generic/head.jsp"%>
<script type="text/javascript" data-main="${contextPath}/js/core-common"
	src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
	require([ "bootstrap/bootstrap" ], function($) {
	});
</script>
<%@ include file="/jsp/css/css-import.jspf"%>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>

	<div class="container">
		<h2>Datarouter</h2>
		<a href="${contextPath}/datarouter/routers">DataRouter Home</a>
		&nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; <a
			href="${contextPath}/datarouter/routers?submitAction=inspectRouter&routerName=${param.routerName}">Router:
			${param.routerName}</a> &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; client: <b>${client.name}</b>

		<h3>Memcached Client Summary</h3>
		<b>routerName: </b>${param.routerName}<br /> <b>clientName: </b>${param.clientName}<br />
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