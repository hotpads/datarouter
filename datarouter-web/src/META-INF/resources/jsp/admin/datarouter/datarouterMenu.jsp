<%@ include file="/WEB-INF/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Routers</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<style>
	.status{
		display: inline-block;
		width: 10px;
		height: 10px;
		border: solid 1px;
		border-color: transparent;
		border-radius: 50%;
	}
	.green{
		background-color: #55DD44;
		border-color: #337722;
	}
	.orange{
		background-color: #FFAA00;
		border-color: #CC8800;
	}
	.red{
		background-color: #DD2222;
		border-color: #882222;
	}
	.router td{
		width: 50%;
	}
	</style>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<div class="container">
		<h2>Datarouter</h2>
		<h3>Server Info</h3>
		<table class="table table-striped table-bordered table-hover table-condensed">
			<tr><td>server.name</td><td>${serverName}</td></tr>
			<tr><td>server.type</td><td>${serverType}</td></tr>
			<tr><td>administrator.email</td><td>${administratorEmail}</td></tr>
			<tr><td>server.privateIp</td><td>${serverPrivateIp}</td></tr>
			<tr><td>server.publicIp</td><td>${serverPublicIp}</td></tr>
		</table>
		
		<h3 class="">Routers and Clients</h3>
		<c:if test="${not empty uninitializedClientNames}">
			[<a href="${contextPath}/datarouter/routers/initAllClients">init remaining clients</a>]
			<br/>
			<br/>
		</c:if>
		<c:forEach items="${routers}" var="router">
			<a href="?submitAction=inspectRouter&routerName=${router.name}">${router.name}</a> router
			<table class="table table-striped table-bordered table-hover table-condensed router">
				<c:forEach items="${router.clientNames}" var="clientName">
					<c:set var="lazyClientProvider" value="${lazyClientProviderByName[clientName]}" />
					<tr>
						<c:choose>
							<c:when test="${lazyClientProvider.initialized}">
								<c:set var="checkResult"
									value="${monitoringService.getLastResultForDatarouterClient(clientName)}"/>
								<c:set var="client" value="${lazyClientProvider.client}" />
								<td>
									<c:choose>
										<c:when test="${empty checkResult}">
											<span class="status"></span>
										</c:when>
										<c:otherwise>
											<a class="status ${checkResult.cssClass}" title="${checkResult}"
												href="${monitoringService.getGraphLinkForDatarouterClient(clientName)}">
											</a>
										</c:otherwise>
									</c:choose>
									<a href="${contextPath}/datarouter/clients/${client.type.name}?submitAction=inspectClient&routerName=${router.name}&clientName=${client.name}">
										${client.name}
									</a>
								</td>
								<td>
									${client.type.name}
								</td>
							</c:when>
							<c:otherwise>
								<td>
									<span class="status"></span>
									${clientName}
								</td>
								<td>
									[<a href="${contextPath}/datarouter/routers/initClient?clientName=${clientName}">init</a>]
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