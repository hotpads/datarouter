<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>${node.name} databean</title>
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
		<p>
		<c:forEach items="${databeanWrappers}" var="databeanWrapper">
			<form method="get" action="?">
				<div class="label-above row-fluid">
					<ul class="span3">
						<li><b>NodeName:</b></li>
						<li>${databeanWrapper.node.name}</li>
					</ul>
					<ul class="span3">
						<li><b>NodeType:</b></li>
						<li>${databeanWrapper.node['class'].simpleName}</li>
					</ul>
	 				<ul class="span3">
						<li><b>DatabeanType:</b></li>
						<li>${databeanWrapper.node.fieldInfo.sampleDatabean['class']}</li>
					</ul>
					<ul class="span3">
						<li><b>Field Aware:</b></li>
						<li>${databeanWrapper.fieldAware ? "True" : "False"}</li>
					</ul>
				</div>
			</form>
						
			<table class="viewNodeDataTable data sortable table table-condensed table-bordered table-hover">
				<thead>
					<tr>
						<c:forEach items="${databeanWrapper.fields}" var="field">
							<th id="fieldAbbreviation.${field.key.name}">${field.key.name}</th>
						</c:forEach>
					</tr>
				</thead>
				<tbody>
					<c:if test="${not empty databeanWrapper.rowOfFields}">
							<tr>
								<c:forEach items="${databeanWrapper.rowOfFields}" var="field">
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
					</c:if>
				</tbody>
			</table>
			<br />
			<p>
		</c:forEach>
	</div>
	<script type="text/javascript">
		require([ "bootstrap/bootstrap" ], function() {});
	</script>
</body>
</html>