<%@ include file="/WEB-INF/prelude.jspf" %>
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

<h3>Server Info</h3>
	<table class="table table-striped table-bordered table-hover table-condensed">
	<tr><td>server.name</td><td>${serverName}</td></tr>
	<tr><td>administrator.email</td><td>${administratorEmail}</td></tr>
</table>
<br/>
<br/>

<h3 class="">Routers and Clients</h3>
<c:forEach items="${routers}" var="router">
	<a href="?submitAction=inspectRouter&routerName=${router.name}">${router.name}</a>
	&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;
	${router['class'].simpleName}
	<table class="table table-striped table-bordered table-hover table-condensed">
		<c:forEach items="${router.allClients}" var="client">
			<tr >
				<td style="width: 33%">
					<a href="?submitAction=inspectClient&routerName=${router.name}&clientName=${client.name}">${client.name}</a>
				</td>
				<td style="width: 33%">
					${client.type}
				</td>
					<td style="width: 33%">
						<c:if test="${client.type=='hbase'}">
							<a href="?submitAction=viewHBaseServers&routerName=${router.name}&clientName=${client.name}">servers</a>
						</c:if>
					</td>
			</tr>
		</c:forEach>
	</table>
	<br/>
</c:forEach>
<br/>
<br/>



</div>
</body>
</html>