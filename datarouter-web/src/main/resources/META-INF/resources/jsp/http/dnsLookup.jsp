<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
<title>dns lookup</title>
<%@ include file="/jsp/generic/datarouterHead.jsp"%>
</head>

<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container mt-4">

		<h2>DNS Lookup</h2>
		<form action="${contextPath}${path}">
			<div class="form-row">
				<div class="col">
					<input type="text" class="form-control" id="hostname"
						name="hostname" placeholder="hostname" value='${hostname}'>
				</div>
				<div class="col">
					<button type="submit" class="btn btn-primary">Lookup</button>
				</div>
			</div>

		</form>

		<div>
			<c:if test="${not empty ipAddresses}">
				<h5 class="mt-4">DNS details</h5>
				<pre class="mt-2 bg-light text-dark p1 border">${ipAddresses}</pre>
			</c:if>
			<c:if test="${not empty error}">
				<h5 class="mt-4">DNS details</h5>
				<pre class="mt-2 bg-light text-dark p1 border">${error}</pre>
			</c:if>
		</div>

	</div>


</body>