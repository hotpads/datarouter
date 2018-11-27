<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>${node.name} databean</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<div class="container-fluid">
		<h2 class="page-header">Databean viewer</h2>
		<c:forEach items="${databeanWrappers}" var="databeanWrapper">
			<dl>
				<dt>NodeName</dt>
				<dd>${databeanWrapper.node.name}</dd>
				<dt>NodeType</dt>
				<dd>${databeanWrapper.node['class'].simpleName}</dd>
				<dt>DatabeanType</dt>
				<dd>${databeanWrapper.node.fieldInfo.sampleDatabean['class']}</dd>
			</dl>
			<table class="table table-condensed table-bordered">
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
									<td>
										<c:forEach items="${field.value}" var="v">
											${v}<br />
										</c:forEach>
									</td>
								</c:if>
							</c:forEach>
						</tr>
					</c:if>
				</tbody>
			</table>
		</c:forEach>
	</div>
</body>
</html>