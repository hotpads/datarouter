<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>Databean</title>
<%@ include file="/jsp/generic/head.jsp" %>
<%@ include file="/jsp/css/css-import.jspf" %>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<style>
</style>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<h2 class="container">Databean viewer</h2>
	<div class="wide-container">
		<a href="${contextPath}/datarouter/routers">Datarouter Home</a>
		<form method="get" action="?">
			<div class="label-above row-fluid">
				<ul class="span5">
					<li><b>NodeName:</b></li>
					<li>${node.name}</li>
				</ul>
			</div>
			<div class="label-above row-fluid">
 				<ul class="span6">
					<li><b>DatabeanType:</b></li>
					<li>${node.fieldInfo.sampleDatabean['class']}</li>
				</ul>
				<ul class="span5">
					<li><b>NodeType:</b></li>
					<li>${node['class'].simpleName}</li>
				</ul>
			</div>
			
		</form>
		
		<br />
		It is ${nonFieldAware}
		
		<br />
		
		<table class="viewNodeDataTable data sortable table table-condensed table-bordered table-hover">
			<thead>
				<tr>
					<c:forEach items="${fields}" var="field">
						<th id="fieldAbbreviation.${field.key.name}">${field.key.name}</th>
					</c:forEach>
				</tr>
			</thead>
			<tbody>
				<c:if test="${not empty rowsOfFields}">
					<c:forEach items="${rowsOfFields}" var="rowOfFields" varStatus="status">
						<tr>
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
						</tr>
						<c:set var="valueString" value="" />
					</c:forEach>
				</c:if>
			</tbody>
		</table>
	</div>
	<script type="text/javascript">
		require([ "bootstrap/bootstrap" ], function() {});
	</script>
</body>
</html>