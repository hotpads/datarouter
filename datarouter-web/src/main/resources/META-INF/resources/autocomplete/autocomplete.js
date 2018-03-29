define(["jquery"], function($) {
	
	//start autocomplete for several objects via a class - they must not have ids, this will replace them
	
	var allAutoCompletes = [];
	
	window.AutoCompleteClass = function(targetClass, urlToLoadParam, minLetters, selectionCallback) {
		$("." + targetClass).each(function(i, target) {
			if ($(target).attr('id') == "") {
				$(target).attr('id', (Math.floor(Math.random()*1000000000+1))); //set id to random num - no .s
			}
			allAutoCompletes.push(
				new AutoComplete($(target).attr('id'), urlToLoadParam, minLetters, selectionCallback)
			);
		});
	};
	
	window.AutoComplete = function(targetInputId, urlToLoadParam, minLetters, selectionCallback){
		this.targetInputId = targetInputId;
		if (selectionCallback == undefined) selectionCallback = function(){};
		this.selectionCallback = selectionCallback;
		this.targetInput = jQuery("#" + targetInputId);
		this.targetInputDom = this.targetInput.get(0);
		this.resultsDiv = null;
		this.urlToLoad = null;
		this.listSize = 0;
		this.listDisplayed = false;
		this.listMemberSelected = -1; //-1 means none selected
		this.origText = ""; // content of the text box before a suggestion was selected
		
		this.targetInput.focus(function(){
			for(var i = 0; i < allAutoCompletes.length; i++){
				if(allAutoCompletes[i].targetInputId != myself.targetInputId){
					allAutoCompletes[i].clearAutoComplete();
				}
			}
		});
		
		
	
		if (typeof(minLetters) == "undefined") {
			this.minLetters = new Number(0);
		} else {
			this.minLetters = minLetters;
		}
		var myself = this;
		if(urlToLoadParam.indexOf(".") == -1){
			this.urlToLoad = "/autoFill/"+urlToLoadParam+".htm";
		}
		else{
			this.urlToLoad = urlToLoadParam;
		}
		
		this.init = function(){			
			this.resultsDiv = $("<div class='autoCompleteResultsDiv' id='autoCompleteResultsFor" + this.targetInputId + "' />");
			this.targetInput.after(this.resultsDiv);
			this.targetInput.blur(function(){
				myself.clearAutoComplete();
			});
			this.targetInput.attr("autocomplete", "off");		
			
			this.targetInput.keyup(function(e){
				if (this.value.length < myself.minLetters) myself.clearAutoComplete();
				var keyCode = e.which;
				
				if (keyCode == 38) { //up
					//if (!listUp) autoComplete(false);
					myself.suggestionsUp();
				} else if (keyCode == 40) { //down
					//if (!listUp) autoComplete(false);
					myself.suggestionsDown();
				} else if (keyCode == 27) { //escape
					myself.clearAutoComplete();
				} else if (keyCode == 13) { //enter
					myself.clearAutoComplete();		
				} else if (keyCode == 9) { //tab
					myself.clearAutoComplete();
				} else if (!myself.isCharacterKey(keyCode)){
					//do nothing
				} else {
					if (typeof(this.theTimeout) != 'undefined') {
						clearTimeout(this.theTimeout);						
					}
					this.theTimeout = setTimeout(myself.autoComplete, 300);
				}
			});
		};
		
		this.isCharacterKey = function(keyCode){
			if(keyCode == 8){
				return true;
			}
			if(keyCode >= 46 && keyCode <= 90){
				return true;
			}
			if(keyCode >= 96 && keyCode <= 111){
				return true;
			}
			if(keyCode >= 186 && keyCode <= 222){
				return true;
			}
			return false;
		};
		
		this.autoComplete = function(s){
			var val = myself.targetInput.val();
			if (val.length >= myself.minLetters) {
			//if (listMemberSelected == -1 || changedText){
				myself.origText = val;
				if(val.length < 1){
					myself.clearAutoComplete();
				} else {
					$.getJSON(myself.urlToLoad + '?partial=' + escape(val), myself.autoCompleteCallBack);
				}
			//} 
			}
		};
		
		this.clearAutoComplete = function(){
			myself.resultsDiv.hide();
			myself.resultsDiv.html('');
			myself.listDisplayed = false;
			myself.listMemberSelected = -1;
		};
		
		this.autoCompleteCallBack = function(json){
			myself.repositionResultsDiv();
			myself.listSize = json.length;
			myself.listMemberSelected = -1;
			if(json.length > 0){
				myself.listDisplayed = true;
				var content = '';
				for(i = 0; i < json.length; i++){
					content+= '<div class="unselected">' + json[i] + '</div>';
				}
				myself.resultsDiv.html(content).show();
				
				var divs = myself.resultsDiv.children();
				divs.mouseover(function(){
					divs.each(function(){this.className = 'unselected';});
					this.className = 'selected';
				});
				
				divs.mousedown(function(){
					myself.targetInput.val(this.childNodes[0].nodeValue);
					inputText = this.childNodes[0].nodeValue;
					myself.clearAutoComplete();
					myself.selectionCallback();
				});
				
			} else {
				myself.clearAutoComplete();
			}
		};
		
		this.repositionResultsDiv = function(){
			// get the field position
			var sf_pos    = myself.targetInput.offset();
			var sf_top    = sf_pos.top;
			var sf_left   = sf_pos.left;
		
			// get the field size
			var sf_height = myself.targetInput.height();
			var sf_width_px = myself.targetInput.css("width");
			var sf_width  = parseInt(sf_width_px, 10);
		
			// apply the css styles - optimized for Firefox
			//myself.resultsDiv.css("position","absolute");
			myself.resultsDiv.css("left", sf_left);
			myself.resultsDiv.css("width", sf_width);
		};
		
		this.suggestionsUp = function(){
			if(myself.listMemberSelected == -1){
				myself.listMemberSelected = myself.listSize - 1;
			} else {
				myself.listMemberSelected --;
			}
			myself.applyStyle();
		};
		
		this.suggestionsDown = function(){
			if(myself.listMemberSelected == (myself.listSize - 1)){
				myself.listMemberSelected = -1;
			} else {
				myself.listMemberSelected ++;
			}
			myself.applyStyle();
		};
		
		this.applyStyle = function(){
			if (myself.listMemberSelected == -1){
				myself.targetInput.val(myself.origText);
			}
			myself.resultsDiv.children().each(function(i){
				if(i == myself.listMemberSelected){
					myself.targetInput.val(this.childNodes[0].nodeValue);
					this.className = 'selected';
				} else {
					this.className = 'unselected';
				}
			});
		};
		
		this.init();
	};
	
	
	return {
		AutoCompleteClass : AutoCompleteClass,
		AutoComplete : 		AutoComplete
	};
});