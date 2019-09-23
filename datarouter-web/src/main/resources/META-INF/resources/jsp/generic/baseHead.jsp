<meta name="viewport" content="width=device-width, initial-scale=1">
<%@ include file="/jsp/css/css-import.jspf" %>
<script src="${contextPath}/requirejs/require.js"></script>
<script>
	var baseRequireJsConfig = {
		baseUrl: '${contextPath}/',
		shim:{
			'jquery-ui' : ['jquery'],
			'bootstrap': ['jquery'],
			'multiple-select' : ['jquery'],
			'dygraph-extra': ['dygraph']
		},
		paths: {
			'jquery' : 'jquery/jquery',
			'jquery-ui' : 'jquery/jquery-ui',
			'bootstrap' : 'bootstrap/v3/js/bootstrap',
			'multiple-select': 'jee-assets/multiple-select/multiple-select',
			'dygraph' : 'dygraph/dygraph-combined',
			'dygraph-extra' : 'dygraph/dygraph-extra',
			'autocomplete' : 'autocomplete/autocomplete',
			'sorttable': 'sorttable/sorttable',
			'goog' : 'requirejs/plugins/goog',
			'async' : 'requirejs/plugins/async',
			'propertyParser' : 'requirejs/plugins/propertyParser'
		}
	};
	require.config(baseRequireJsConfig);
	require(['bootstrap']);
</script>