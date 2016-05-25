<%@ include file="../../generic/prelude-datarouter.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Routers</title>
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
		<c:if test="${not empty uninitializedClientNames}">
			[<a href="${contextPath}/datarouter/routers/initAllClients">init remaining clients</a>]<br/>
			<br/>
		</c:if>
		<c:forEach items="${routers}" var="router">
			<a href="?submitAction=inspectRouter&routerName=${router.name}">${router.name}</a>
			&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;
			${router['class'].simpleName}
			<table class="table table-striped table-bordered table-hover table-condensed">
				<c:forEach items="${router.clientNames}" var="clientName">
					<c:set var="lazyClientProvider" value="${lazyClientProviderByName[clientName]}" />
					<tr>
						<c:choose>
							<c:when test="${lazyClientProvider.initialized}">
								<c:set var="client" value="${lazyClientProvider.client}" />
								<td style="width: 50%">
									<a href="${contextPath}/datarouter/clients/${client.type.name}?submitAction=inspectClient&routerName=${router.name}&clientName=${client.name}">${client.name}</a>
								</td>
								<td style="width: 50%">
									${client.type.name}
								</td>
							</c:when>
							<c:otherwise>
								<td style="width: 50%">
									${clientName} [<a href="${contextPath}/datarouter/routers/initClient?clientName=${clientName}">init</a>]
								</td>
								<td style="width: 50%">
									unknown
								</td>
							</c:otherwise>
						</c:choose>
					</tr>
				</c:forEach>
			</table>
			<br/>
		</c:forEach>
	</div>
</body>
</html>