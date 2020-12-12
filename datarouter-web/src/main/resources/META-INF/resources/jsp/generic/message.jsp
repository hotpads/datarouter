<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
<title>Message</title>
<%--TODO possible to include a head here, instead of this stuff? --%>
<%@ include file="/jsp/css/css-import-b3.jspf" %>
</head>
<body>
	<pre class="container" style="margin-top: 10%">
		<h4>${fn:escapeXml(message)}</h4>
	</pre>
</body>
</html>
