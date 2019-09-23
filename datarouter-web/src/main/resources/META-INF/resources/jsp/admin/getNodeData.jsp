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
		<h2>Datarouter - Get Databean</h2>
		<ol class="breadcrumb">
			<li class="breadcrumb-item"><a href="${contextPath}/datarouter">Datarouter Home</a></li>
			<li class="breadcrumb-item"><a href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&nodeName=${node.name}">${node.name}</a></li>
			<li class="breadcrumb-item active">get databean</li>
		</ol>
		<form method="GET" action="?">
			<input type="hidden" name="submitAction" value="get">
			<div class="form-group">
				<label>Node</label>
				<input name="nodeName" value="${node.name}" type="text" class="form-control">
			</div>
			<c:forEach items="${keyFields}" var="field" varStatus="loop">
				<div class="form-group">
					<label>${field.key.name}</label>
					<c:set var="paramKey" value="field_${field.key.name}"/>
					<input name="${FIELD_PREFIX}${field.key.name}" value="${param[paramKey]}" type="text" class="form-control">
				</div>
			</c:forEach>
			<div>
				<input type="submit" value="Get databean" class="btn btn-primary">
			</div>
		</form>
		<br>
		<c:forEach items="${fields}" var="field" varStatus="loop">
			${field.key.name}<c:if test="${!loop.last}">,</c:if>
		</c:forEach>
		<br>
		<table class="data sortable table table-sm table-bordered table-hover">
			<thead>
				<tr>
					<c:forEach items="${fields}" var="field">
						<th title="${field.key.name}">${abbreviatedFieldNameByFieldName[field.key.name]}</th>
					</c:forEach>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<c:if test="${empty rowsOfFields}">
					<c:forEach items="${databeans}" var="databean" varStatus="status">
						<tr>
							<c:forEach items="${databean.fields}" var="field">
								<c:if test="${! field.key.collection}">
									<td>${field.valueString}</td>
								</c:if>
								<c:if test="${field.key.collection}">
									<td>
										<c:forEach items="${field.values}" var="v">
											${valueString}${v}<br />
										</c:forEach>
									</td>
								</c:if>
							</c:forEach>
						</tr>
						<c:set var="valueString" value="" />
					</c:forEach>
				</c:if>
				<c:if test="${not empty rowsOfFields}">
					<c:forEach items="${rowsOfFields}" var="rowOfFields" varStatus="status">
						<tr>
							<c:forEach items="${rowOfFields}" var="field">
								<c:if test="${! field.key.collection}">
									<td>${field.valueString}</td>
								</c:if>
								<c:if test="${field.key.collection}">
									<td>
										<c:forEach items="${field.values}" var="v">
											${valueString}${v}<br />
										</c:forEach>
									</td>
								</c:if>
							</c:forEach>
							<td>
								<a href="${contextPath}/datarouter/nodes/deleteData?nodeName=${node.name}${fieldKeys[status.index]}">
									<i class="far fa-trash-alt"></i>
								</a>
							</td>
						</tr>
						<c:set var="valueString" value="" />
					</c:forEach>
				</c:if>
			</tbody>
		</table>
	</div>
</body>
</html>