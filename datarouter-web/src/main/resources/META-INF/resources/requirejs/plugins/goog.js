define([], function () {
	return {
		load: function(name, req, onLoad, config){
			console.log(name, req, onLoad, config)
			req(['https://www.gstatic.com/charts/loader.js'], function(){
				google.charts.load('current', {packages:name.split(/\s*,\s*/), callback: onLoad})
			});
		}
	}
})
