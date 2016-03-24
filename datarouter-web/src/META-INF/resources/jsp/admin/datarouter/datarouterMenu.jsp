<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../../generic/prelude-datarouter.jspf"%>
<html>
<head>
	<title>Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
	<h2>Datarouter</h2>
	<h3>Server Info</h3>
		<table class="table table-striped table-bordered table-hover table-condensed">
		<tr><td>server.name</td><td>${serverName}</td></tr>
		<tr><td>administrator.email</td><td>${administratorEmail}</td></tr>
	</table>
	
	<h3 class="">Routers and Clients</h3>
	<c:forEach items="${routers}" var="router">
		<a href="?submitAction=inspectRouter&routerName=${router.name}">${router.name}</a>
		&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;
		${router['class'].simpleName}
		<table class="table table-striped table-bordered table-hover table-condensed">
			<c:forEach items="${router.allClients}" var="client">
				<tr >
					<td style="width: 50%">
						<a href="${contextPath}/datarouter/clients/${client.type.name}?submitAction=inspectClient&routerName=${router.name}&clientName=${client.name}">${client.name}</a>
					</td>
					<td style="width: 50%">
						${client.type.name}
					</td>
				</tr>
			</c:forEach>
		</table>
		<br/>
	</c:forEach>
	</div>
</body>
</html>