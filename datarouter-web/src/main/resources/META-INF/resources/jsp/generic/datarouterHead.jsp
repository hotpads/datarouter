<%@ include file="/jsp/generic/baseHead.jsp" %>
<link rel="icon" href="${contextPath}/jee-assets/datarouter-logo.png">
<script>
	var requireJsConfig = {
		paths: {
			'async' : 'requirejs/plugins/async',
			'goog' : 'requirejs/plugins/goog',
			'propertyParser' : 'requirejs/plugins/propertyParser',
			'jquery.validate': 'jquery/jquery.validate'
		},
		shim: {
			'jquery.validate': ['jquery']
		}
	}
	requirejs.config(requireJsConfig);
</script>