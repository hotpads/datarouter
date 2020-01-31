<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter ${param.clientName}</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container my-3">
		<h2>Datarouter ${param.clientName}</h2>
		<nav>
			<ol class="breadcrumb">
				<li class="breadcrumb-item"><a href="${contextPath}/datarouter">Datarouter Home</a></li>
				<li class="breadcrumb-item active"><b>Client: </b>${param.clientName}</li>
			</ol>
		</nav>

		<h3>Client Information</h3>
		<p>
			<b>Number of Nodes: </b>${fn:length(nodes)}<br>
			<b>Nodes</b>
			<ul>
				<c:forEach items="${nodes}" var="node">
					<li>${node}</li>
				</c:forEach>
			</ul>
		</p>

		<h3>Statistics</h3>
		<c:forEach items="${memcachedStats}" var="stats">
			<h4>Node <small>${stats.key}</small></h4>
			<table class="table table-striped table-hover table-sm">
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
