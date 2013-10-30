define([
	"jquery"
], function($) {

	$.extend({
		createDygraph : function(selector, url, param) {
			var g = null;
			$.get(url, function() {}).done(function(json) {
				if (json.indexOf("Chart creation error") > -1) {
					$(selector).empty();
					$(selector).append("<h5>For some reasons, an error occured on the server. Please check your formula. </h5>" + json);
				}
				// if the csv is not null and not empty, we can display the
				// chart
				else if (json != null && json != " ") {
					// Do this step before getting chart because the chart needs
					// to know the size of the dive above to appear correctly
					$(selector).empty();
					g = new Dygraph(document.getElementById(selector), json, param);
				}
				// If we fail to get a csv, we ask for checking the parameters
			}).fail(function() {
				$(selector).append("<h5>For some reasons, an error occured on the server. Please check your formulas.</h5>" + json);
			});
			return g;

		}
	});

	$.extend({
		drawWeekEndPeriod : function(canvas, area, g, color) {

			canvas.fillStyle = color;

			function highlight_period(x_start, x_end) {
				var canvas_left_x = g.toDomXCoord(x_start);
				var canvas_right_x = g.toDomXCoord(x_end);
				var canvas_width = canvas_right_x - canvas_left_x;
				canvas.fillRect(canvas_left_x, area.y, canvas_width, area.h);
			}

			var min_data_x = g.getValue(0, 0);
			var max_data_x = g.getValue(g.numRows() - 1, 0);

			// get day of week
			var d = new Date(min_data_x);
			var dow = d.getUTCDay();
			var ds = d.toUTCString();

			var w = min_data_x;
			// starting on Sunday is a special case
			if (dow == 0) {
				highlight_period(w, w + 12 * 3600 * 1000);
			}
			// find first saturday
			while (dow != 6) {
				w += 24 * 3600 * 1000;
				d = new Date(w);
				dow = d.getUTCDay();
			}
			// shift back 1/2 day to center highlight around the point for the
			// day
			w -= 12 * 3600 * 1000;
			while (w < max_data_x) {
				var start_x_highlight = w;
				var end_x_highlight = w + 2 * 24 * 3600 * 1000;
				// make sure we don't try to plot outside the graph
				if (start_x_highlight < min_data_x) {
					start_x_highlight = min_data_x;
				}
				if (end_x_highlight > max_data_x) {
					end_x_highlight = max_data_x;
				}
				highlight_period(start_x_highlight, end_x_highlight);
				// calculate start of highlight for next Saturday
				w += 7 * 24 * 3600 * 1000;
			}
		}

	});

	$.extend({
		collision : function($div1, $div2) {
			var x1 = $div1.offset().left;
			var y1 = $div1.offset().top;
			var h1 = $div1.outerHeight(true);
			var w1 = $div1.outerWidth(true);
			var b1 = y1 + h1;
			var r1 = x1 + w1;
			var x2 = $div2.offset().left;
			var y2 = $div2.offset().top;
			var h2 = $div2.outerHeight(true);
			var w2 = $div2.outerWidth(true);
			var b2 = y2 + h2;
			var r2 = x2 + w2;

			if (b1 < y2 || y1 > b2 || r1 < x2 || x1 > r2)
				return false;
			return true;
		}
	});
	$.extend({
		updateURLParameter : function(url, param, paramVal) {
			var TheAnchor = null;
			var newAdditionalURL = "";
			var tempArray = url.split("?");
			var baseURL = tempArray[0];
			var additionalURL = tempArray[1];
			var temp = "";

			if (additionalURL) {
				var tmpAnchor = additionalURL.split("#");
				var TheParams = tmpAnchor[0];
				TheAnchor = tmpAnchor[1];
				if (TheAnchor)
					additionalURL = TheParams;

				tempArray = additionalURL.split("&");

				for (i = 0; i < tempArray.length; i++) {
					if (tempArray[i].split('=')[0] != param) {
						newAdditionalURL += temp + tempArray[i];
						temp = "&";
					}
				}
			} else {
				var tmpAnchor = baseURL.split("#");
				var TheParams = tmpAnchor[0];
				TheAnchor = tmpAnchor[1];

				if (TheParams)
					baseURL = TheParams;
			}

			if (TheAnchor)
				paramVal += "#" + TheAnchor;

			var rows_txt = temp + "" + param + "=" + paramVal;
			return baseURL + "?" + newAdditionalURL + rows_txt;
		}
	});

	$.fn.autoReloadPage = function(timeInMs, toLoad) {

		if ($(this).attr("class").indexOf("autoReloadEnabled") > -1) {
			$(this).removeClass("autoReloadEnabled");
			$(this).addClass("autoReloadDisabled");
			$(this).text("Enable Autoreload (" + timeInMs / 1000 + "s)");
			window.clearInterval($(this).attr("idInterval"));
		} else {
			$(this).text("Disable Autoreload");
			$(this).removeClass("autoReloadDisabled");
			$(this).addClass("autoReloadEnabled");
			var idInterval = window.setInterval(function() {
				toLoad();
			}, timeInMs);
			$(this).attr("idInterval", idInterval);

		}
	};

	/**
	 * Check if the input is null or empty
	 */
	$.extend({
		isNull : function(input) {
			if (input === null || typeof input === 'undefined' || input.length < 1 || input == " ") {
				return true;
			}
			return false;
		}
	});

	/**
	 * Check if the input is null or empty and return optional value if so.
	 */
	$.extend({
		isNullOptionalValue : function(input, optional) {
			if (typeof input === 'undefined' || input.length < 1) {
				return optional;
			}
			return input;
		}
	});

	/**
	 * IE makes click sounds whenever the src of an iframe is changed. This gets
	 * around it by changing the src of an iframe not yet added to the DOM, and
	 * then replaces the current iframe with it.
	 */
	$.fn.quietIframeSrcChange = function(url) {
		if ($.browser.mozilla && this.attr("src") == "") { /*
															 * firefox doesn't
															 * like
															 * cloning/replacing
															 * an empty iframe.
															 */
			this.attr("src", url);
		} else {
			var newFrame = this.clone();
			newFrame.attr("src", url);
			this.replaceWith(newFrame);
		}
	};

	$.extend({
		deserialize : function(paramString, paramArray, include) {
			paramString = paramString.replace(/#|\?/g, '&');
			var hasParamArray = typeof (paramArray) != 'undefined';
			var includeGivenParams = typeof (include) == 'undefined' || include;
			var map = {};
			jQuery.each(paramString.split('&'), function(i, pair) {
				if (pair.length == 0)
					return;
				var j = pair.indexOf('=');
				var key = (j != -1) ? pair.substr(0, j) : pair;
				if (!hasParamArray || (includeGivenParams && jQuery.inArray(key, paramArray) != -1) || (!includeGivenParams && jQuery.inArray(key, paramArray) == -1)) {
					map[key] = (j != -1) ? unescape(pair.substr(j + 1)).replace(/\+/g, ' ') : 'true';
				}
			});
			return map;
		}
	});

	/*
	 * ! Cookie plugin Copyright (c) 2006 Klaus Hartl (stilbuero.de) Dual
	 * licensed under the MIT and GPL licenses:
	 * http://www.opensource.org/licenses/mit-license.php
	 * http://www.gnu.org/licenses/gpl.html
	 */

	/**
	 * Create a cookie with the given name and value and other optional
	 * parameters.
	 * 
	 * @example $.cookie('the_cookie', 'the_value'); Set the value of a cookie.
	 * @example $.cookie('the_cookie', 'the_value', { expires: 7, path: '/',
	 *          domain: 'jquery.com', secure: true }); Create a cookie with all
	 *          available options.
	 * @example $.cookie('the_cookie', 'the_value'); Create a session cookie.
	 * @example $.cookie('the_cookie', null); Delete a cookie by passing null as
	 *          value. Keep in mind that you have to use the same path and
	 *          domain used when the cookie was set.
	 * @param String
	 *            name The name of the cookie.
	 * @param String
	 *            value The value of the cookie.
	 * @param Object
	 *            options An object literal containing key/value pairs to
	 *            provide optional cookie attributes.
	 * @option Number|Date expires Either an integer specifying the expiration
	 *         date from now on in days or a Date object. If a negative value is
	 *         specified (e.g. a date in the past), the cookie will be deleted.
	 *         If set to null or omitted, the cookie will be a session cookie
	 *         and will not be retained when the the browser exits.
	 * @option String path The value of the path atribute of the cookie
	 *         (default: path of page that created the cookie).
	 * @option String domain The value of the domain attribute of the cookie
	 *         (default: domain of page that created the cookie).
	 * @option Boolean secure If true, the secure attribute of the cookie will
	 *         be set and the cookie transmission will require a secure protocol
	 *         (like HTTPS).
	 * @type undefined
	 * @name $.cookie
	 * @cat Plugins/Cookie
	 * @author Klaus Hartl/klaus.hartl@stilbuero.de
	 */
	jQuery.cookie = function(name, value, options) {
		if (typeof value != 'undefined') { // name and value given, set cookie
			options = options || {};
			if (value === null) {
				value = '';
				options.expires = -1;
			}
			var expires = '';
			if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
				var date;
				if (typeof options.expires == 'number') {
					date = new Date();
					date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
				} else {
					date = options.expires;
				}
				expires = '; expires=' + date.toUTCString(); // use expires
				// attribute, max-age is
				// not supported by IE
			}
			// CAUTION: Needed to parenthesize options.path and options.domain
			// in the following expressions, otherwise they evaluate to
			// undefined
			// in the packed version for some reason...
			var path = options.path ? '; path=' + (options.path) : '';
			var domain = options.domain ? '; domain=' + (options.domain) : '';
			var secure = options.secure ? '; secure' : '';
			document.cookie = [
			        name, '=', encodeURIComponent(value), expires, path, domain, secure
			].join('');
		} else { // only name given, get cookie
			var cookieValue = null;
			if (document.cookie && document.cookie != '') {
				var cookies = document.cookie.split(';');
				for ( var i = 0; i < cookies.length; i++) {
					var cookie = jQuery.trim(cookies[i]);
					// Does this cookie string begin with the name we want?
					if (cookie.substring(0, name.length + 1) == (name + '=')) {
						cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
						break;
					}
				}
			}
			return cookieValue;
		}
	};

	return $;

});