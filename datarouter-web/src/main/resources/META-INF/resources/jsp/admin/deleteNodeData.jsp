<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/new-common-navbar.jsp" %>
	<div class="container my-4">
		<h2>Datarouter - Delete a Databean</h2>
		<ol class="breadcrumb">
			<li class="breadcrumb-item"><a href="${contextPath}/datarouter">Datarouter Home</a></li>
			<li class="breadcrumb-item"><a href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&nodeName=${node.name}">${node.name}</a></li>
			<li class="breadcrumb-item active">delete databean</li>
		</ol>
		<form method="GET" action="?">
			<input type="hidden" name="submitAction" value="doDeletion">
			<div class="form-group">
				<label for="nodeName">Node</label>
				<input name="nodeName" value="${node.name}" type="text" class="form-control" id="nodeName">
			</div>
			<div class="card bg-light border card-body">
				<c:forEach items="${keyFields}" var="field" varStatus="loop">
					<div class="form-group">
						<label>${field.key.name}</label>
						<input name="${FIELD_PREFIX}${field.key.name}" value="${param[field.key.name]}" type="text" class="form-control">
					</div>
				</c:forEach>
				<div>
					<input type="submit" value="Delete databean" class="btn btn-primary">
				</div>
			</div>
		</form>
	</div>
</body>
</html>
