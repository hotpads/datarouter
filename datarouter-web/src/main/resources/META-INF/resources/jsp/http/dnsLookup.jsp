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
		<form action="${contextPath}${path}" class="mb-4">
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

		<c:if test="${not empty javaResult}">
			<div>
				<h3>java resolution</h3>
				<div>networkaddress.cache.ttl (ms): ${caching}</div>
				<div>duration: ${javaDuration}</div>
				<pre class="mt-2 bg-light text-dark p1 border">${javaResult}</pre>
			</div>
		</c:if>
		<c:if test="${not empty digResultStdout}">
			<div>
				<h3>dig resolution</h3>
				<div>duration: ${digDuration}</div>
				<div>exit value: ${digExitVal}</div>
				<h5>standard output:</h5>
				<pre class="mt-2 bg-light text-dark p1 border">${digResultStdout}</pre>
				<h5>standard error</h5>
				<pre class="mt-2 bg-light text-dark p1 border">${digResultStderr}</pre>
				<h5>parsed result</h5>
				<pre class="mt-2 bg-light text-dark p1 border">${parsedDig}</pre>
			</div>
		</c:if>

	</div>
</body>