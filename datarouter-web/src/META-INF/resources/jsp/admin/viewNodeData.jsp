<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>Datarouter</title>
<%@ include file="/jsp/generic/head.jsp" %>
<%@ include file="/jsp/css/css-import.jspf" %>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<style>
</style>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<h2 class="container">Datarouter</h2>
	<div class="wide-container">
		<a href="${contextPath}/datarouter/routers">Datarouter Home</a> &nbsp;&nbsp;&#62;&#62;&nbsp;&nbsp; <a href="${contextPath}/datarouter/routers?submitAction=inspectRouter&routerName=${param.routerName}">Router: ${param.routerName}</a> &nbsp;&nbsp;&#62;&#62; &nbsp;&nbsp;
		node: <b>${node.name}</b><br /> <br />
		<form method="get" action="?">
			<div class="label-above row-fluid">
				<ul class="span6">
					<li><b>RouterName:</b></li>
					<li><input name="routerName" value="${param.routerName}" type="text" /></li>
				</ul>
				<ul class="span5">
					<li><b>NodeName:</b></li>
					<li><input name="nodeName" value="${node.name}" type="text" /></li>
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
			<div class="label-above row-fluid">
				<ul class="span6">
					<li><b>StartAfterKey (changes each page):</b></li>
					<li><input name="startAfterKey" value="${startAfterKey}" type="text" /></li>
				</ul>
				<ul class="span5">
					<li><b>Limit:</b>&nbsp;<%/*(<b>offset:</b>${offset})*/%></li>
					<li><input name="limit" value="${limit}" type="text" /></li>
				</ul>
			</div>
			<div class="label-above row-fluid">
				<ul class="span12">
					<li></li>
					<li><input type="submit" name="submitAction" value="browseData" class="btn btn-success" /></li>
				</ul>
			</div>
			<div class="label-above row-fluid">
				<ul class="span6">
					<li><b>where:</b></li>
					<li><input name="where" value="${param.where}" type="text" /></li>
				</ul>
				<ul class="span6">
					<li></li>
<!-- 					<li class="span3"><input type="submit" name="submitAction" value="countWhere" class="btn btn-success" /> -->
					<li class="span3"><input type="submit" name="submitAction" value="getWhere" class="btn btn-success" /></li>
				</ul>
			</div>
		</form>
		
		
		<c:if test="${offset > 0}">
			<a href="?submitAction=${param.submitAction}&routerName=${param.routerName}&nodeName=${param.nodeName}
				&startAfterKey=
				&limit=${limit}&offset=0&where=${param.where}"><b>start</b></a>&nbsp;-&nbsp;
		</c:if>
		<c:if test="${!(offset > 0)}">
			<b>start</b>&nbsp;-&nbsp;
		</c:if>
		<c:if test="${offset > 0 and not empty backKey}">
			<a href="?submitAction=${param.submitAction}&routerName=${param.routerName}&nodeName=${param.nodeName}
				&startAfterKey=${backKey}
				&limit=${limit}&offset=${offset-limit}&where=${param.where}"><b>previous</b></a>&nbsp;-&nbsp;
		</c:if>
		<c:if test="${not (offset > 0 and not empty backKey)}">
			<b>previous&nbsp;</b>-&nbsp;
		</c:if>
		<c:set var="accesDatabeans" value=""></c:set>
		<c:if test="${fn:length(databeans) >= limit}">
		<c:set var="accesDatabeans" value="&backKey=${startAfterKey}&startAfterKey=${nextKey}
				&limit=${limit}&offset=${offset+limit}&where=${param.where}"></c:set>
		</c:if>
				<a href="?submitAction=${param.submitAction}&routerName=${param.routerName}&nodeName=${param.nodeName}${accesDatabeans}">next</a>
		
		<br />
		${param.nodeName} is${nonFieldAware}
		
		<br />
		<c:forEach items="${fields}" var="field">
				${field.key.name},&nbsp;
		</c:forEach>
		<table class="viewNodeDataTable data sortable table table-condensed table-bordered table-hover">
			<thead>
				<tr>
					<th>#</th>
					<c:forEach items="${fields}" var="field">
						<th id="fieldAbbreviation.${field.key.name}">${abbreviatedFieldNameByFieldName[field.key.name]}</th>
					</c:forEach>
				</tr>
			</thead>
			<tbody>
				<c:if test="${empty rowsOfFields}">
					<c:forEach items="${databeans}" var="databean" varStatus="status">
						<tr <c:if test="${status.index%5==0}"> class="highlighted"</c:if>>
							<td>${offset + status.index}</td>
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
							<td>${offset + status.index}</td>
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