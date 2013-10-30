var urlToLoad;
var inputTextBox;
var inputText;
var listMemberSelected = -1;
var listSize = 0;
var listUp = false;
var lastTimeout = 0;

function initAutoComplete(inputTextId, urlToLoadParam){
	
	urlToLoad = urlToLoadParam;
	inputTextBox = $("#" + inputTextId);
	
	//Need to add a div area Choice
	if (typeof $("#areaChoice") != 'undefined' && $("#areaChoice") != null) {
	$("#areaChoice").after('<div id="autoCompleteResults""></div>');
	}else{
	inputTextBox.after('<div id="autoCompleteResults""></div>');
	}
	
	inputTextBox.keyup(function(e){
		var keyCode = e.which;
		
		if (keyCode == 38) { //up
			if (!listUp) autoComplete(false);
			suggestionsUp();
		} else if (keyCode == 40) { //down
			if (!listUp) autoComplete(false);
			suggestionsDown();
		} else if (keyCode == 27) { //escape
			clearAutoComplete();
		} else if (keyCode == 13) { //enter
			clearAutoComplete();		
		} else {
			clearTimeout(lastTimeout);
			lastTimeout = setTimeout("autoComplete(true)", 500);
		}
	});
	
	inputTextBox.blur(function(){ 
		lastTimeout = setTimeout("clearAutoComplete()", 500);
	});
}

function autoComplete(changedText){
	val = inputTextBox.val();
	if (listMemberSelected == -1 || changedText){
		inputText = val;
		if(val.length < 1){
			clearAutoComplete();
		} else {
			$.getJSON(urlToLoad + "?partial=" + escape(val) + "&type=" + $(".areaType:checked").val(), autoCompleteCallBack);
		}
	} 
}
function autoCompleteCallBack(json){
	repositionResultsDiv();
	listSize = json.length;
	listMemberSelected = -1;
	if(json.length > 0){
		listUp = true;
		var content = "";
		for(i = 0; i < json.length; i++){
//		content+= "<div class=\"unselected\">" + json[i]["area"] + "</div></div>";
//
//		}
			if(json[i]["type"] == "City"){
				content+= "<div class=\"unselected\">" + json[i]["city"] +", " + json[i]["state"] + " (city)<div style=\"display:none\">" + json[i]["uniqueName"] + "</div></div>";
			}if(json[i]["type"] == "County"){
				content+= "<div class=\"unselected\">" + json[i]["county"] + ", " + json[i]["state"] + " (county)<div style=\"display:none\">" + json[i]["uniqueName"] + "</div></div>";
			}if(json[i]["type"] == "MetroArea"){
				content+= "<div class=\"unselected\">" + json[i]["metro"] + " (metro)<div style=\"display:none\">" + json[i]["uniqueName"] + "</div></div>";
			}if(json[i]["type"] == "Neighborhood"){
				content+= "<div class=\"unselected\">" + json[i]["neighborhood"] + " in " + json[i]["city"] + ", " + json[i]["state"] + " (neighborhood)<div style=\"display:none\">" + json[i]["uniqueName"] + "</div></div>";
			}if(json[i]["type"] == "State"){
				content+= "<div class=\"unselected\">" + json[i]["state"] + " (state)<div style=\"display:none\">" + json[i]["uniqueName"] + "</div></div>";
			}if(json[i]["type"] == "Zip"){
				content+= "<div class=\"unselected\">" + json[i]["zip"] + " in " + json[i]["city"] + ", " + json[i]["state"] + " (zip)<div style=\"display:none\">" + json[i]["uniqueName"] + "</div></div>";
			}
		}
		$("#autoCompleteResults").html(content);
		$("#autoCompleteResults").css("display", "block");
		
		var divs = $("#autoCompleteResults > div");
		divs.mouseover(function(){
			divs.each(function(){this.className = "unselected";});
			this.className = "selected";
		});
		
		divs.click(function(){
			inputTextBox.val(this.childNodes[0].nodeValue);
			$("#uniqueIdForSelectedArea").val($(this).children().html());
			inputText = this.childNodes[0].nodeValue;
			clearAutoComplete();
		});
		
	} else {
		clearAutoComplete();
	}
}

function repositionResultsDiv(){
//	// get the field position
//	var sf_pos    = inputTextBox.offset();
//	var sf_top    = sf_pos.top;
//	var sf_left   = sf_pos.left;
//
//	// get the field size
//	var sf_height = inputTextBox.height();
//	var sf_width  = inputTextBox.width();
//
//	// apply the css styles - optimized for Firefox
//	$("#autoCompleteResults").css("position","absolute");
//	$("#autoCompleteResults").css("left", sf_left - 2);
//	repositionResultsDivTop(sf_top, sf_height);
//	$("#autoCompleteResults").css("width", sf_width - 2);
}

function repositionResultsDivTop(sf_top, sf_height){
//	$("#autoCompleteResults").css("top", sf_top + sf_height + 5);
}

function clearAutoComplete(){
	$("#autoCompleteResults").css("display", "none");
	$("#autoCompleteResults").html('');
	listUp = false;
	listMemberSelected = -1;
}

function suggestionsUp(){
	if(listMemberSelected == -1){
		listMemberSelected = listSize - 1;
	} else {
		listMemberSelected --;
	}
	applyStyle();
}

function suggestionsDown(){
	if(listMemberSelected == (listSize - 1)){
		listMemberSelected = -1;
	} else {
		listMemberSelected ++;
	}
	applyStyle();
}

function applyStyle(){
	if (listMemberSelected == -1){
		inputTextBox.val(inputText);
	}
	$("#autoCompleteResults > div").each(function(i){
		if(i == listMemberSelected){
			inputTextBox.val(this.childNodes[0].nodeValue);
			$("#uniqueIdForSelectedArea").val($(this).children().html());
			this.className = "selected";
		} else {
			this.className = "unselected";
		}
	});
}
$(function(){
	initAutoComplete("areaPartial", "/analytics/events/search/area");
});