<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<div class="container-fluid">
		<h2 class="page-header">Datarouter</h2>
		<ol class="breadcrumb">
			<li><a href="${contextPath}/datarouter">Datarouter Home</a></li>
			<li>
				<a href="${contextPath}/datarouter?submitAction=inspectRouter&routerName=${param.routerName}">
					Router: ${param.routerName}
				</a>
			</li>
			<li>node: <b>${node.name}</b></li>
		</ol>
		<a href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&routerName=${param.routerName}&nodeName=${node.name}">Browse databeans</a>
		<h3>Delete databean</h3>
		<form method="GET" action="?" class="form-horizontal">
			<div class="form-group">
				<label class="col-sm-1 control-label">Router</label>
				<div class="col-sm-11">
					<input name="routerName" value="${param.routerName}" type="text" class="form-control" />
				</div>
			</div>
			<div class="form-group">
				<label class="col-sm-1 control-label">Node</label>
				<div class="col-sm-11">
					<input name="nodeName" value="${node.name}" type="text" class="form-control" />
				</div>
			</div>
			<c:forEach items="${keyFields}" var="field" varStatus="loop">
				<div class="form-group">
					<label class="col-sm-1 control-label">${field.key.name}</label>
					<div class="col-sm-11">
						<input name="${FIELD_PREFIX}${field.key.name}" value="${param[field.key.name]}" type="text" class="form-control" />
					</div>
				</div>
			</c:forEach>
			<input type="hidden" name="submitAction" value="doDeletion" />
			<div class="form-group">
				<div class="col-sm-1"></div>
				<input type="submit" value="Delete databean" class="btn btn-primary" />
			</div>
		</form>
	</div>
</body>
</html>