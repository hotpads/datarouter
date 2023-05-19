<div id="timezone-toast" class="toast" role="alert" aria-live="assertive" aria-atomic="true" data-autohide="false"
	 style="position: absolute; top: 8px; right: 8px; z-index: -1">
	<div class="toast-header">
		Mismatched timezone
	</div>
	<div class="toast-body">
		Your saved timezone is <span id="saved-timezone"></span> and your current browser timezone is <span
			id="browser-timezone"></span>. Would you like to update your timezone?
		<div style="margin-top: 8px; text-align: right;">
			<button id="yes-timezone-button" type="button" class="btn btn-primary btn-sm">Yes</button>
			<button id="no-timezone-button" type="button" class="btn btn-primary btn-sm btn-secondary">No</button>
			<button id="never-timezone-button" type="button" class="btn btn-primary btn-sm btn-secondary">Never</button>
		</div>
	</div>
</div>

<script>
	require(['bootstrap'], () => {
		$(document).ready(() => {
			const pathname = window.location.pathname.substring(1);
			const contextRoot = pathname.substring(0, pathname.indexOf("/"));
			const baseUrl = "/" + contextRoot + "/permissionRequest";

			const localStorageKey = "datarouter-timezone";
			if (localStorage.getItem(localStorageKey)) {
				return;
			}

			function showTimezoneToast() {
				const timeZoneToast = $('#timezone-toast');
				timeZoneToast.toast("show");
				timeZoneToast.css("z-index", 2);
			}

			function hideTimezoneToast() {
				const timeZoneToast = $('#timezone-toast');
				timeZoneToast.toast("hide");
				timeZoneToast.css("z-index", -1);
			}

			$.get(baseUrl + "/getUserTimezone", function (timezone) {
				const browserTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
				if (timezone !== browserTimezone) {
					$('#browser-timezone').text(browserTimezone);
					$('#saved-timezone').text(timezone);
					showTimezoneToast();

					$('#yes-timezone-button').on('click', () => {
						$.get(baseUrl + "/setTimezone?timezone=" + browserTimezone, function () {
							hideTimezoneToast();
						});
					});

					$('#no-timezone-button').on('click', () => {
						hideTimezoneToast();
					});

					$('#never-timezone-button').on('click', () => {
						localStorage.setItem(localStorageKey, "true")
						hideTimezoneToast();
					});
				}
			});

		});
	});
</script>
