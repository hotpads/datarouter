function addNavbarRequestTiming(contextPath){
	require(['jquery'], function($){
		const navPerformance = performance.getEntriesByType("navigation")[0] || performance.timing
		if(!navPerformance){
			console.log("Unable to find navigation performance from: " + performance)
			return
		}
		$('#requestTiming').text(Math.ceil(navPerformance.responseEnd - navPerformance.requestStart) + 'ms')
	
		$(function(){
			const app = contextPath.replace('/', '')
			const isDatarouterPage = location.pathname.indexOf("${app}/datarouter") !== -1
			const target = isDatarouterPage ? "datarouter" : app
			$('#common-navbar a[data-target="' + target + '"]').addClass('active')
	
			const checkLoadEndInterval = setInterval(() => {
				if(navPerformance.loadEventEnd === 0){
					return
				}
				clearInterval(checkLoadEndInterval)
				$('#clientTiming').text(Math.ceil(navPerformance.loadEventEnd - navPerformance.responseEnd) + 'ms')
			}, 100)
		})
	})
}
