<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Error processing request</title>
<%@ include file="/jsp/css/css-import.jspf" %>
</head>
<body>
	<div class="container on-top">
		<div class="alert alert-danger">We're sorry, but there was an error processing your request.</div>
		<div class="alert alert-info">
			<b>HTTP error code:</b> ${statusCode}
		</div>
		<c:if test="${not empty message}">
			<div class="alert alert-info">${message}</div>
		</c:if>
		<pre>
<c:if test="${not empty stackTraceString}">
<b>stack trace:</b>
${stackTraceString}
</c:if>
</pre>
	</div>
</body>
</html>