define('settings-tools', ['jquery-ui'], function(){

	$.fn.highlight = function(){
		this.effect('highlight', {color: '#ACE671'}, 2500)
		return this
	}

	//clears and disables all 'disableable' inputs
	function disableAndClearAll() {
		$('.disableable').each(function(index, element){
			element = $(element);
			element.prop('disabled', true);
			element.val('');
		});
	}

	function getRowData(row){
		var retVal = {};
		retVal["name"] = (row.find(".setting-name").val() === "") ? row.find(".setting-name").text() : row.find(".setting-name").val();
		retVal["scope"] = row.find(".setting-scope").text();
		retVal["serverType"] = row.find(".setting-type").text();
		retVal["serverName"] = row.find(".setting-serverName").text();
		retVal["value"] = row.find(".setting-value").val();
		return retVal;
	}

	function getGroupRowData(row){
		var settings = {};
		settings['nodeName'] = row.closest('tbody').find('.group-name').val();
		settings['scope'] = row.find('.group-scope').val();
		row.children('.setting').each(function(){
			settings[$(this).find('.setting-name').val()] = $(this).find('.setting-value').val();
		});

		return settings;
	}

	function sameSettings(rowData, settings) {
		return rowData["name"] === settings["name"]
			&& rowData["serverType"] === settings["serverType"]
			&& rowData["serverName"] === settings["serverName"];
	}

	function update(setting){
		var flag = false;
		$('.settings-table').find('tr').each(function(i, row){
			var rowData = getRowData($(this));
			if (sameSettings(rowData, setting)) {
				$(this).find(".setting-value").val(setting['value']).highlight()
				flag = true;
				return false; //breaks out of loop
			}
		});
		return flag;
	}

	function promptForComment(params, promptMessage){
		const ret = prompt(promptMessage);
		if(ret === null) // null indicates user clicked "cancel"
			return false
		params["comment"] = ret || null; // null if comment is an empty string
		return true
	}

	function getFormData(form) {
		return {
			name: form.find('.name').val(),
			nodeName: form.find('.node').val(),
			serverType: form.find('.server-type option:selected').val(),
			serverName: form.find('.server-name').val(),
			value: form.find('.value') && form.find('.value').val(),
			scope: form.find('.scope').val(),
			comment: form.find('[name="comment"]').val()
		}
	}

	function POST(url, data, onSuccess){
		url += url.includes('?') ? '&' : '?'
		url += $.param(data)
		return fetch(url, {method: 'POST', redirect: 'manual', credentials: 'include'})
			.then(response => {
				if(response.type === "opaqueredirect"){
					throw `Request was redirected, make sure you are logged in.`;
				}else if(!response.ok){
					throw `status code ${response.status} - ${response.statusText}`;
				}
				return response
			})
			.then(response => response.json())
			.then(({success, error}) => { // response can contain an error we'd like to display
				if(success){
					return onSuccess()
				}else{
					throw error
				}
			})
			.catch(error => {
				alert("POST request failed.\n" + error)
			})
	}

	return {
		//this is here because one of the backslashes kept getting stripped when in jsp
		escape: function(id){
			return id.replace( /(:|\.|\[|\]|,|=)/g, "\\$1" );
		},
		editSetting: function(context) {
			var row = $(context).closest("tr");
			var rowData = getRowData(row);
			if(promptForComment(rowData, "Are you sure?\nAdd optional comment before pressing \"OK\".")){
				POST("?submitAction=update", rowData, function(){
					row.highlight()
				})
			}
		},
		editSettingGroup: function(context) {
			var row = $(context).closest("tr");
			var rowData = getGroupRowData(row);
			POST( "?submitAction=updateGroup", rowData, function(){
				row.highlight()
			});
		},
		createSetting: function(fn, form) {
			var form = form || $('#form');
			var formData = getFormData(form);
			POST("?submitAction=create", formData, function() {
				if (!update(formData)) {
					fn(formData);
				}
			});
		},
		updateSettingTags: function(){ 
			var data = {
					values: $("#tagValues").val()
			};
			POST("?submitAction=updateSettingTags", data, function() {
				$("#tagValuesRow").highlight();
			});
		},
		deleteSetting: function(context, fn) {
			var row = $(context).closest("tr");
			var rowData = getRowData(row);
			if(promptForComment(rowData, "Are you sure?\nAdd optional comment before pressing \"OK\".")){
				POST("?submitAction=delete", rowData, function() {
					fn(row, rowData["name"]);
				});
			}
		},
		deleteGroup: function(context, fn) {
			var row = $(context).closest('tr');
			var scope = row.children('.group-scope').val();
			var groupName = $(context).closest('table').find('.group-name').val();
			if (confirm('Are you sure?')){
				POST("?submitAction=deleteGroup", {scope, nodeName: groupName}, function() {
					fn(row, undefined, true);
				});
			}
		},
		updateFields: function(form) {
			form = form || $('#form');
			form.find('.value') && form.find('.value').prop('disabled', false);
			var scope = form.find('.scope').val();
			switch(scope){
			case null:
				//reset the form completely
				disableAndClearAll();
				form.find('.server-type').val('');
				break;
			case 'defaultScope':
				disableAndClearAll();
				form.find('.server-type').val('unknown');
				break;
			case 'serverType':
				disableAndClearAll();
				form.find('.server-type').prop('disabled', false);
				form.find('.server-type')[0].selectedIndex = 0;
				break;
			case 'serverName':
				disableAndClearAll();
				form.find('.server-name').prop('disabled', false);
				form.find('.server-type').val('unknown');
				break;
			}
		},
		getValidatorOptionsWithHandler: function(submitHandler, formElem){
			formElem = formElem || $('#form');
			return {
				rules: {
					name: "required",
					scope: "required",
					serverType: "required",
					serverName: {
						required: function() {
							return formElem.find('.scope option[value="serverName"]').prop('selected') == true;
						}
					}
				},
				submitHandler: function(form){
					submitHandler($(form));
				}
			}
		}
	};
});
