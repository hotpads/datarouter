<c:import  url="/jsp/generic/prelude.jspf" />
<html>
<head>
<title>${title}</title>
<c:import url="/jsp/generic/head.jsp" />
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
	require([
           "jquery/jquery-ui", "bootstrap/bootstrap"
    ], function($) {});
</script>
<c:import url="/jsp/css/css-import.jsp" />

</head>
<body>
	<c:import url="/jsp/menu/dr-navbar.jsp" />
	<div class="container">
		<h4>Welcome to Datarouter module !</h4>
	</div>
</body>