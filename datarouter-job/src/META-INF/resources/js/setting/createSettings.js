define('settings-tools', ['jquery-ui'], function(){
	
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
		retVal["name"] = (row.find(".setting-name").val() === "") ? row.find(".setting-name").text() : row.find(".setting-name").val() ;
		retVal["scope"] = row.find(".setting-scope").text();
		retVal["serverType"] = row.find(".setting-type").text();
		retVal["serverName"] = row.find(".setting-serverName").text();
		retVal["application"] = row.find(".setting-application").text();
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
			&& rowData["serverName"] === settings["serverName"]
			&& rowData["application"] === settings["application"];
	}
	
	function update(setting){
		var flag = false;
		$('.settings-table').find('tr').each(function(i, row){
			var rowData = getRowData($(this));
			if (sameSettings(rowData, setting)) {
				$(this).find(".setting-value").val(setting['value']);
				$(this).effect("highlight", {color:"#ACE671"}, 2500);
				flag = true;
				return false; //breaks out of loop
			}
		});
		return flag;
	}
	
	function getFormData(form) {
		var retVal = {};
		retVal["name"] = form.find('.name').val();
		retVal["nodeName"] = form.find('.node').val()
		//form.find('.server-type') by itself does not get disabled options when they are selected
		retVal["serverType"] = form.find('.server-type option:selected').val();
		retVal["serverName"] = form.find('.server-name').val();
		retVal["application"] = form.find('.application').val();
		retVal["value"] = form.find('.value') && form.find('.value').val();
		retVal["scope"] = form.find('.scope').val();
		return retVal;
	}
	
	return {
		//this is here because one of the backslashes kept getting stripped when in jsp
		escape: function(id){
			return id.replace( /(:|\.|\[|\]|,|=)/g, "\\$1" );
		},
		editSetting: function(context) {
			var row = $(context).closest("tr");
			var rowData = getRowData(row);
			$.post( "settings?submitAction=update", rowData,
					function(){
						row.effect("highlight", {color:"#ACE671"}, 2500);
				});
		},
		editSettingGroup: function(context) {
			var row = $(context).closest("tr");
			var rowData = getGroupRowData(row);
			$.post( "settings?submitAction=updateGroup", rowData,
					function(){
						row.effect("highlight", {color:"#ACE671"}, 2500);
				});
		},
		createSetting: function(fn, form) {
			var form = form || $('#form');
			var formData = getFormData(form);
			$.post( "settings?submitAction=create",
					formData,
					function() {
						if (!update(formData)) {
							fn(formData);
					};
			});
		},
		deleteSetting: function(context, fn) {
			var row = $(context).closest("tr");
			var rowData = getRowData(row);
			if (confirm("Are you sure?")){
				$.post("settings?submitAction=delete", rowData, function() {
					fn(row, rowData["name"]);
				});
			}
		},
		deleteGroup: function(context, fn) {
			var row = $(context).closest('tr');
			var scope = row.children('.group-scope').val();
			var groupName = $(context).closest('table').find('.group-name').val();		
			if (confirm('Are you sure?')){
				$.post('?submitAction=deleteGroup&scope=' + scope + '&nodeName=' + groupName, '', function() {
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
	    	case 'cluster':
	    		disableAndClearAll();
	    		form.find('.server-type').val('all');
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
	    	case 'application':
	    		disableAndClearAll();
	    		form.find('.application').prop('disabled', false);
	    		form.find('.server-type').val('unknown');
	    		break;
	    	}
		},
		getValidatorOptionsWithHandler: function(submitHandler){
			formElem = $('#form');
			return {
				rules: {
					name: "required",
					scope: "required",
					serverType: "required",
					serverName: {
						required: function() {
							return formElem.find('.scope option[value="serverName"]').prop('selected') == true;
						}
					},
					application: {
						required: function() {
							return formElem.find('.scope option[value="application"]').prop('selected') == true;
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