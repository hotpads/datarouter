<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>${tableName}</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<style>
		td{
			white-space: nowrap;
		}
		tbody tr:first-child,
		tbody tr:nth-child(6n){
			background-color: rgba(0,0,0,.075);
		}
	</style>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
	<nav>
		<ol class="breadcrumb rounded-0">
			<li class="breadcrumb-item"><a href="${contextPath}/datarouter">Datarouter Home</a></li>
			<li class="breadcrumb-item active">node: <b>${node.name}</b></li>
		</ol>
	</nav>
	<div class="container-fluid my-4">
		<h2>
			Browse databeans
			<span class="ml-auto">
				<a class="btn btn-light border btn-sm" href="${contextPath}/datarouter/nodes/deleteData?nodeName=${node.name}">Delete a databean</a>
				<a class="btn btn-light border btn-sm" href="${contextPath}/datarouter/nodes/getData?nodeName=${node.name}">Get a databean</a>
			</span>
		</h2>
		<dl>
			<dt>DatabeanType</dt><dd>${node.fieldInfo.sampleDatabean['class']}</dd>
			<dt>NodeType</dt><dd>${node['class'].simpleName}</dd>
		</dl>
		<form method="get" action="?" class="card bg-light card-body">
			<div class="form-group">
				<label for="nodeName" class="font-weight-bold m-0">Node</label>
				<input id="nodeName" name="nodeName" value="${node.name}" type="text" class="form-control">
			</div>
			<c:if test="${browseSortedData == true}">
				<div class="form-group">
					<label for="startKey" class="font-weight-bold m-0">Start key (changes each page)</label>
					<input id="startKey" name="startKey" value="${startKey}" type="text" class="form-control">
				</div>
				<div class="form-row">
					<div class="form-group col-auto">
						<label for="limit" class="font-weight-bold m-0">limit</label>
						<input id="limit" name="limit" value="${limit}" type="number" step="1" class="form-control">
					</div>
					<div class="form-group col-auto">
						<label for="outputBatchSize" class="font-weight-bold m-0">outputBatchSize</label>
						<input id="outputBatchSize" name="outputBatchSize" value="${outputBatchSize}" type="number" step="1" class="form-control">
					</div>
				</div>
				<div>
					<button class="btn btn-success">Scan</button>
				</div>
			</c:if>
		</form>
		<c:if test="${browseSortedData == true}">
			<c:set var="accessDatabeans" value=""/>
			<c:if test="${fn:length(databeans) >= limit}">
				<c:set var="accessDatabeans" value="&startKey=${nextKey}&limit=${limit}"/>
			</c:if>
			<nav>
				<ul class="pagination justify-content-center mt-2 mb-0">
					<c:if test="${not empty startKey}">
						<li class="page-item">
							<a class="page-link" href="?nodeName=${param.nodeName}&startKey=&limit=${limit}">
								First page
							</a>
						</li>
					</c:if>
					<c:if test="${not empty accessDatabeans}">
						<li class="page-item">
							<a class="page-link" href="?nodeName=${param.nodeName}${accessDatabeans}">
								Next page
							</a>
						</li>
					</c:if>
				</ul>
			</nav>
			<c:forEach items="${fields}" var="field" varStatus="loop">
				${field.key.name}<c:if test="${!loop.last}">,</c:if>
			</c:forEach>
			<table class="table table-sm table-bordered table-hover">
				<thead>
					<tr>
						<c:forEach items="${fields}" var="field">
							<th id="fieldAbbreviation.${field.key.name}" title="${field.key.name}">${abbreviatedFieldNameByFieldName[field.key.name]}</th>
						</c:forEach>
						<th class="fa-fw"></th>
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
											<c:forEach items="${field.value}" var="v">
												${valueString}${v}
												<br />
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
											<c:forEach items="${field.value}" var="v">
												${valueString}${v}
												<br />
											</c:forEach>
										</td>
									</c:if>
								</c:forEach>
								<td>
									<a href="${contextPath}/datarouter/nodes/deleteData?nodeName=${node.name}${fieldKeys[status.index]}" title="Delete">
										<i class="far fa-trash-alt fa-fw"></i>
									</a>
								</td>
							</tr>
							<c:set var="valueString" value="" />
						</c:forEach>
					</c:if>
				</tbody>
			</table>
			<nav> 
				<ul class="pagination justify-content-center">
					<c:if test="${not empty startKey}">
						<li class="page-item">
							<a class="page-link" href="?nodeName=${param.nodeName}&startKey=&limit=${limit}">
								First page
							</a>
						</li>
					</c:if>
					<c:if test="${not empty accessDatabeans}">
						<li class="page-item">
							<a class="page-link" href="?nodeName=${param.nodeName}${accessDatabeans}">
								Next page
							</a>
						</li>
					</c:if>
				</ul>
			</nav>
		</c:if>
	</div>
</body>
</html>