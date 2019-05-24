<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<div class="container-fluid">
		<h2 class="page-header">Datarouter</h2>
		<ol class="breadcrumb">
			<li><a href="${contextPath}/datarouter">Datarouter Home</a></li>
			<li>node: <b>${node.name}</b></li>
		</ol>
		<a href="${contextPath}/datarouter/nodes/browseData?submitAction=browseData&nodeName=${node.name}">Browse databeans</a>
		<h3>Get databean</h3>
		<form method="GET" action="?" class="form-horizontal">
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
			<input type="hidden" name="submitAction" value="get" />
			<div class="form-group">
				<div class="col-sm-1"></div>
				<input type="submit" value="Get databean" class="btn btn-primary" />
			</div>
		</form>
		<c:set var="accesDatabeans" value=""></c:set>
		<c:if test="${fn:length(databeans) >= limit}">
			<c:set var="accesDatabeans" value="&startAfterKey=${nextKey}&limit=${limit}"></c:set>
		</c:if>
		<nav>
			<ul class="pager">
				<c:if test="${not empty startAfterKey}">
					<li>
						<a href="?submitAction=${param.submitAction}&nodeName=${param.nodeName}&startAfterKey=&limit=${limit}">
							Start
						</a>
					</li>
				</c:if>
				<c:if test="${not empty accesDatabeans}">
					<li>
						<a href="?submitAction=${param.submitAction}&nodeName=${param.nodeName}${accesDatabeans}">
							Next
						</a>
					</li>
				</c:if>
			</ul>
		</nav>
		<br />
		<c:forEach items="${fields}" var="field" varStatus="loop">
			${field.key.name}<c:if test="${!loop.last}">,</c:if>
		</c:forEach>
		<br />
		<table class="viewNodeDataTable data sortable table table-condensed table-bordered table-hover">
			<thead>
				<tr>
					<c:forEach items="${fields}" var="field">
						<th id="fieldAbbreviation.${field.key.name}">${abbreviatedFieldNameByFieldName[field.key.name]}</th>
					</c:forEach>
				</tr>
				<tr></tr>
			</thead>
			<tbody>
				<c:if test="${empty rowsOfFields}">
					<c:forEach items="${databeans}" var="databean" varStatus="status">
						<tr <c:if test="${status.index%5==0}"> class="highlighted"</c:if>>
							<c:forEach items="${databean.fields}" var="field">
								<c:if test="${! field.key.collection}">
									<td>${field.valueString}</td>
								</c:if>
								<c:if test="${field.key.collection}">
									<td><c:forEach items="${field.values}" var="v">
							${valueString}${v}<br />
										</c:forEach></td>
								</c:if>
							</c:forEach>
						</tr>
						<c:set var="valueString" value="" />
					</c:forEach>
				</c:if>
				<c:if test="${not empty rowsOfFields}">
					<c:forEach items="${rowsOfFields}" var="rowOfFields" varStatus="status">
						<tr <c:if test="${status.index%5==0}"> class="highlighted"</c:if>>
							<c:forEach items="${rowOfFields}" var="field">
								<c:if test="${! field.key.collection}">
									<td>${field.valueString}</td>
								</c:if>
								<c:if test="${field.key.collection}">
									<td><c:forEach items="${field.values}" var="v">
							${valueString}${v}<br />
										</c:forEach></td>
								</c:if>
							</c:forEach>
							<td>
								<a href="${contextPath}/datarouter/nodes/deleteData?nodeName=${node.name}${fieldKeys[status.index]}">
									<span class="glyphicon glyphicon-trash"/>
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