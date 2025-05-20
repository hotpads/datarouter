function addPath(path){
	const docs = window.location.origin + window.location.pathname;
	return (docs.replace(/\/$/, '') + '/' + path.replace(/^\//, ''))
		.replace("/api/", "/docs/")
		.replace("/docsV2/", "/docs/");
}

function fetchCsrf(obj, apiKeyFieldName) {
	const url = addPath("getCsrfIv") + '?' + $.param(obj);
	const options = {method: 'GET'};
	options.headers = new Headers({
		'X-apiKeyFieldName': apiKeyFieldName
	});
	return fetch(url, options)
}

function fetchSignature(theParams, requestBody, apiKeyFieldName){
	const url = addPath("getSignature") + '?' + $.param(theParams);
	const options = !!requestBody ? {method: 'POST', body: requestBody} : {method: 'GET'}
	options.headers = new Headers({
		'X-apiKeyFieldName': apiKeyFieldName
	});
	return fetch(url, options);
}

function getJson(response){
	return response.json();
}

function getParameters(paramRowClass, hideAuth) {
	var paramMap = {};
	var requestBody;
	var needsCsrf = false;
	var needsSignature = false;
	$('.' + paramRowClass).each(function (index, tableRow) {
		var paramName = $(tableRow).find('td.paramName').data('name');
		var paramValueInput = $(tableRow).find('.paramValue');
		var paramValue = paramValueInput.val();
		if (paramValueInput.is("textarea")) {
			requestBody = paramValue;
		}else if(paramName === "csrfIv" && hideAuth){
			needsCsrf = true;
		}else if(paramName === "signature" && hideAuth){
			needsSignature = true;
		}else if (paramValue) {
			paramMap[paramName] = paramValue;
		}
	});
	var result = {};
	const apiKeyFieldName = document.getElementById('response-container').dataset.apikeyfieldname;
	if(needsCsrf && needsSignature){
		result = fetchCsrf(paramMap, apiKeyFieldName)
			.then(getJson)
			.then(params => fetchSignature(params, requestBody, apiKeyFieldName))
			.then(getJson);
	}else if(needsCsrf){
		result = fetchCsrf(paramMap, apiKeyFieldName).then(getJson);
	}else if(needsSignature){
		result = fetchSignature(paramMap, requestBody, apiKeyFieldName).then(getJson);
	}else{
		result = Promise.resolve(paramMap);
	}
	return result;
}

function getBody(paramRowClass){
	var requestBody;
	$('.' + paramRowClass).each(function (index, tableRow) {
		var paramValueInput = $(tableRow).find('.paramValue');
		var paramValue = paramValueInput.val();
		if (paramValueInput.is("textarea")) {
			requestBody = paramValue;
			return;
		}
	});
	return requestBody;

}

require(['jquery', 'bootstrap'], function() {
	function callApi(url, hideAuth){
		const paramRowClass = "param-row";
		let promise = getParameters(paramRowClass, hideAuth, url);
		promise.then(function(params){
			var responseDivId = 'response-div';
			$("#" + responseDivId).hide();
			var requestBody = getBody(paramRowClass);
			var requestUrlId = 'requestUrl';
			var requestBodyId = 'requestBody';
			var jsonResponseId = 'jsonResponse';
			var responseCodeId = 'responseCode';
			var responseHeaderId = 'responseHeader';
			document.getElementById(jsonResponseId).innerHTML = '';
			document.getElementById(requestUrlId).innerHTML = '';
			document.getElementById(requestBodyId).innerHTML = '';
			document.getElementById(responseCodeId).innerHTML = '';
			document.getElementById(responseHeaderId).innerHTML = '';

			var options;
			if(requestBody != null){
				options = {
					type: 'POST',
					data: requestBody,
					contentType: 'application/json'
				};
			}

			var requestUrl = url + "?" + $.param(params);
			$.ajax(requestUrl, options)
				.complete(function(request){
					$("#" + responseDivId).show();
					if(request.getResponseHeader("content-type") === 'application/json'){
						var json = formatJson(request.responseText);
						document.getElementById(jsonResponseId).innerHTML = syntaxHighlight(json);
					}else{
						document.getElementById(jsonResponseId).innerHTML = request.responseText;
					}
					document.getElementById(requestUrlId).innerHTML = getFullRequestUrl(requestUrl);
					document.getElementById(responseCodeId).innerHTML = request.status;
					document.getElementById(responseHeaderId).innerHTML = request.getAllResponseHeaders();
					if(requestBody !== undefined){
						document.getElementById(requestBodyId).innerHTML = requestBody;
					}
				});
		});
	}

	function getFullRequestUrl(partialUrl){
		if(location.origin === undefined){
			return partialUrl;
		}
		return location.origin + partialUrl;
	}

	function formatJson(jsonText){
		const mark = 'JAVASCRIPT_INTEGER_OVERFLOW_PROTECTION'
		const replacer = num => Number.isSafeInteger(num) ? num : ('"' + mark + '(' + num + ')"')
		const longSafeText = jsonText
			.replace(/:\d{15,}\b/g, num => ':' + replacer(num.slice(1)))
			.replace(/^\d{15,}\b/g, replacer)
		return JSON.stringify(JSON.parse(longSafeText), null, 2)
			.replace(new RegExp('"' + mark + '\\((\\d+)\\)"', 'g'), match => match.slice(mark.length + 2, -2))
	}

	function syntaxHighlight(json) {
		json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
		var regex = /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g;
		return json.replace(regex, function (match) {
			var cls = 'number';
			if (/^"/.test(match)) {
				if (/:$/.test(match)) {
					cls = 'key';
				} else {
					cls = 'string';
				}
			} else if (/true|false/.test(match)) {
				cls = 'boolean';
			} else if (/null/.test(match)) {
				cls = 'null';
			}
			return '<span class="' + cls + '">' + match + '</span>';
		});
	}

	$(function(){
		function replaceQueryParam(name, value){
			const val = encodeURIComponent(value)
			const query = window.location.search
			const match = query.match('[?&]' + name + '=[^?&]*')
			let newSearch;
			if(match){
				const matchLen = match[0].length
				const addition = val.length ? name.length + 2 : 0
				const prev = match.input.slice(0, match.index + addition) // "...&key="
				const next = match.input.slice(match.index + matchLen)
				newSearch = (prev + val + next).replace(/^&/, '?')
			}else if(val.length){
				const prefix = query.length ? '&' : '?'
				newSearch = query + prefix + name + '=' + val
			}else{
				return
			}
			history.replaceState(null, null, newSearch)
		}

		$('.paramValue').change(function(){
			const $this = $(this)
			replaceQueryParam($this.attr('name'), $this.val())
		})

		$('pre.json').each(($, pre) => {
			pre.innerHTML = syntaxHighlight(pre.innerHTML);
		});

		$('.copyable').click(event => {
			const copyable = $(event.currentTarget);
			$('#' + copyable.data('copydest')).val(copyable.text());
		});

		$('#submit-button').click(event => {
			const url = $(event.target).data('url');
			const hideAuth = $(event.target).data('hide-auth');
			callApi(url, hideAuth === 'true' || hideAuth === true);
		});

		window.location.search
			.replace(/^\?/, '')
			.split('&')
			.map(param => param.split('=', 2))
			.map(pair => [pair[0], decodeURIComponent(pair[1])])
			.forEach(pair => {
				$('#param-' + pair[0]).val(pair[1])
			})
	})
});
