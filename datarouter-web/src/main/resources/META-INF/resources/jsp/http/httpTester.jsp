<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
<title>http tester</title>
<%@ include file="/jsp/generic/datarouterHead.jsp"%>
</head>

<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container mt-4">

		<h2>HTTP Tester Tool</h2>
		<form action="${contextPath}/datarouter/httpTester">
			<div class="form-row">
				<div class="col-sm-2">
					<select class="form-control" id="method" name="method">
						<option>GET</option>
						<option>POST</option>
					</select>
				</div>
				<div class="col">
					<input type="text" class="form-control" id="url" name="url"
						placeholder="URL">
				</div>
				<div class="col">
					<button type="submit" class="btn btn-primary">Execute</button>
				</div>
			</div>
		</form>
		<c:if test="${not empty url}">
			<div class="table-responsive">
				<table class="table table-striped mt-4">
					<thead class>
						<tr>
							<th scope="col">url</th>
							<th scope="col">server name</th>
							<th scope="col">response ms</th>
							<c:if test="${not empty statusCode}">
								<th scope="col">status code</th>
							</c:if>
							<c:if test="${not empty cause}">
								<th scope="col">exception cause</th>
							</c:if>
							<c:if test="${not empty message}">
								<th scope="col">exception message</th>
							</c:if>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>${url}</td>
							<td>${serverName}</td>
							<td>${responseMs}</td>
							<c:if test="${not empty statusCode}">
								<td>${statusCode}</td>
							</c:if>
							<c:if test="${not empty cause}">
								<td>${cause}</td>
							</c:if>
							<c:if test="${not empty message}">
								<td>${message}</td>
							</c:if>
						</tr>
					</tbody>
				</table>
			</div>
		</c:if>
		<c:if test="${not empty headers}">
			<h5 class="mt-4">Response Headers</h5>
			<div class="table-responsive">
				<table class="table table-sm table-striped table-hover mt-2">
					<thead>
						<tr>
							<th>Header</th>
							<th>Value</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="entry" items="${headers}">
							<tr>
								<td>${entry.key}</td>
								<td>${entry.value}</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</c:if>
		<c:if test="${not empty responseBody}">
			<div id="responseBodyDiv">
				<h5 class="mt-4">Response Body</h5>
				<pre id="responseBody" class="mt-2 bg-light text-dark p1 border">${fn:escapeXml(responseBody)}</pre>
			</div>
		</c:if>
	</div>
</body>