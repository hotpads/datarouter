function addNavbarRequestTiming(contextPath){
	require(['jquery'], function($){
		const navPerformance = performance.getEntriesByType("navigation")[0]
		const roundedMs = duration => Math.round(duration) + "ms"
		$('#requestTiming').text(roundedMs(navPerformance.responseEnd))

		$(document).ready(function() {
			context = contextPath.replace("/", "");
			$("#common-menu-" + context).find("a").addClass("underline");
			if (location.href.indexOf(context + "/datarouter") > -1) {
				$('#common-menu-datarouter').find("a").addClass("underline");
			}

			const checkLoadEndInterval = setInterval(() => {
				if(navPerformance.loadEventEnd === 0){
					return
				}
				clearInterval(checkLoadEndInterval)
				$('#clientTiming').text(roundedMs(navPerformance.loadEventEnd - navPerformance.responseEnd))
			}, 100)
		});
	})
}
