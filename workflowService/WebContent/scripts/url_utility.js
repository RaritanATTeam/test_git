var bonita_server_ip = "/workflowService/at/webservice/1.0/";
var bonita_login_page = "/workflowService/login.html";
//sessionStorage.at_wf_username = "noboday";
//sessionStorage.at_wf_password = "noboday";
var bonita_obj = {
username: 'xxx'
,
password: 'xxx'
,
validateUserUrl: "validateUser"
,
getInstanceListUrl: "getInstanceList"
,
getProcessNameListUrl: "getProcessNameList"
,
getAssignedProcessNameListUrl: "getAssignedProcessNameList"
,
getGlobalVarUrl: "{%0}/getGlobalVar"
,
getLocalVarUrl: "{%0}/getLocalVar"
,
setGlobalVarUrl: "{%0}/setGlobalVar"
,
setLocalVarUrl: "{%0}/setLocalVar"
,
runTaskUrl: "{%0}/runTask"
,
getCurrentUserListUrl : "getCurrentUserList"
,
getWorkflowUIInfoUrl: "{%0}/getWorkflowUIInfo"
,
getWorkflowUIInfoUrl: "{%0}/getWorkflowUIInfo"
,
getExternalUrlLinkUrl: "{%0}/getExternalUrlLink"
,
getAssignedTaskListUrl:"getAssignedTaskList"
,
getAssignedTaskListUrlExtUrl:"getAssignedTaskList/{%0}"
,
getOwnedTaskListUrl:  "getOwnedTaskList" //"getOwnedTaskList"  TODO
,
getOwnedTaskListUrlExtUrl:"getOwnedTaskList/{%0}"
,
checkRallyDescriptionUrl: "/{%0}/checkRallyDescription"
,
logoutUrl: "/logout"
,
thinkThankUrl: "http://thinktank.raritan.com/phpBB2_idea/viewtopic.php?t="
,
sortQs: "?fromIndex={%0}&pageSize={%1}&sort={%2}" //STARTED_DATE_ASC/STARTED_DATE_DESC
,
createInstanceUrl: 'createInstance'
,
notifyWikiUpdateUrl: 'notifyWikiUpdate'
,
dummmy: 'abc'
};



function cbRallyLink()
{
	var newWindow = window.open(this.getAttribute('href'), '_blank');
    newWindow.focus();
    return false;
	
}

function sleep( seconds ) {
	var timer = new Date();
	var time = timer.getTime();
	do
		timer = new Date();
	while( (timer.getTime() - time) < (seconds * 1000) );
}

$(function(){
	$.ajaxSetup({
			async: false,
			cache: false,
            dataType: 'json',
			timeout: 36000000,
			contentType: 'application/json; charset=utf-8',
			
		    complete: function (XMLHttpRequest, textStatus)
			{
				;
			}
			,
			error: function(XMLHttpRequest, textStatus, errorThrown)
            {
				var errObj = {};
				errObj.AjaxReqFail = true;
				errObj.textStatus = textStatus;
				errObj.errorThrown = errorThrown;
				alert("Failed to send ajax request at $.ajaxSetup. \n" + $.toJSON(errObj));
            }
		
		});
       
});



/**
 * function getURLParameters()
 * http://stilbuero.de/demo/jquery/query.html?foo=bar
 * Requires jQuery
 * 
 * @param string s_href
 * 
 * @return object
 */
function getURLParameters (s_href) {
	
	var r = {};
	
	if (s_href && s_href.indexOf('?') > 0) {
	   
		var q = s_href.substring(s_href.indexOf('?') + 1); // remove everything up to the ?
	    q = q.replace(/\&$/, ''); // remove the trailing &
	    $.each(q.split('&'), function() {
	        var splitted = this.split('=');
	        var key = splitted[0];
	        var val = splitted[1];
	        // convert numbers
	        if (/^[0-9.]+$/.test(val)) val = parseFloat(val);
	        // convert booleans
	        if (val == 'true') val = true;
	        if (val == 'false') val = false;
	        // ignore empty values
	        if (typeof val == 'number' || typeof val == 'boolean' || val.length > 0) r[key] = val;
	    });

	}
	
	return r;
};

/*******************************************************************************
@ brief  used to post JSON string to server
ex usage: $.cl_ajax({name:"alex"}, selfjsCallback);
********************************************************************************/
$.extend(
{
    at_ajax_get : function (jsAjaxCallback, _url)
    {
		if (_url.indexOf("validateUser") > -1 && "xxx" != bonita_obj.username )
		{
			logInfo("validateUser URL found : " + bonita_obj.username);
			sessionStorage.at_wf_username = bonita_obj.username;
			sessionStorage.at_wf_password = bonita_obj.password;
		}
        _url =  bonita_server_ip + _url;
		logInfo("New Ajax GET Req: " + _url );
        $.ajax(
        {
			url: _url,
			type:"GET",
			beforeSend: function(xhr)
			{
				logInfo("the session username is : " + sessionStorage.at_wf_username + "\n password is : " + sessionStorage.at_wf_password);
				//alert("the session username is : " + sessionStorage.at_wf_username + "\n password is : " + sessionStorage.at_wf_password);
				xhr.setRequestHeader("Authorization", "Basic " + $.base64.encode(sessionStorage.at_wf_username + ":" + sessionStorage.at_wf_password)); //May need to use "Authentication" instead
		    }
			,
            
            success: function(data)
            {  
				if(data.ret == null)
                {
                    logInfo("Http Req OK, but there's no ret specified in the returned data." + $.toJSON(data))
                    alert("ERROR : ret: " + $.toJSON(data));
                }
                else
                {
                    if(data.ret == 0) // backend said success get data
                    {
                        jsAjaxCallback(data);
                    }
					else if(data.ret == 1)
					{
						alert("Login failed.");
					}
                    else
                    {
						data.get_url_address = _url;
					    alert("GET AJAX ERROR: response error from server: " + $.toJSON(data));
                    }
                }
                
            }
           
        });
    }
	
	
	
	
});

$.extend(
{
    at_ajax_post : function (o_params2Server, jsAjaxCallback, _url)
    {
		_url =  bonita_server_ip + _url;
        var o_params = {};
        // conver the JavaScript obj to JSON str here
        o_params = $.toJSON(o_params2Server);
		logInfo("New Ajax POST Req: " +  o_params );
        $.ajax(
        {
			url: _url,
			type:"POST",
            data:o_params,
			beforeSend: function(xhr)
			{
				logInfo("the session username is : " + sessionStorage.at_wf_username + "\n password is : " + sessionStorage.at_wf_password);
				//alert("the session username is : " + sessionStorage.at_wf_username + "\n password is : " + sessionStorage.at_wf_password);
				xhr.setRequestHeader("Authorization", "Basic " + $.base64.encode(sessionStorage.at_wf_username + ":" + sessionStorage.at_wf_password)); //May need to use "Authentication" instead
		    }
			,
            	
            success: function(data)
            {
                    
				if(data.ret == null)
                {
                    logInfo("Http Req OK, but there's no ret specified in the returned data." + $.toJSON(data))
                    alert("GET AJAX ERROR: response error from server: " + $.toJSON(data));
                }
                else
                {
                    if(data.ret == 0) // backend said success get data
                    {
                        
                        jsAjaxCallback(data);
                    }else if(data.ret == 1)
					{
						//location.href = bonita_login_page ;
					}
                    else
                    {
						data.post_url_address = _url;
						data.post_data = o_params2Server;
					    alert("POST AJAX ERROR: response error from server: " + $.toJSON(data));
                    }
                }
                
            }
           
        });
    }
	
	
	
	
});
$.extend(
{
    at_ajax_getExt : function (_obj_, _url)
    {
		if (_url.indexOf("validateUser") > -1 && bonita_obj.password != "xxx")
		{
			sessionStorage.at_wf_username = bonita_obj.username;
			sessionStorage.at_wf_password = bonita_obj.password;
		}
        _url =  bonita_server_ip + _url;
		logInfo("New Ajax GET Req: " + _url );
        $.ajax(
        {
			url: _url,
			type:"GET",
			beforeSend: function(xhr)
			{
				xhr.setRequestHeader("Authorization", "Basic " + $.base64.encode(sessionStorage.at_wf_username + ":" + sessionStorage.at_wf_password)); //May need to use "Authentication" instead
		    }
			,
            
            success: function(data)
            {  
				if(data.ret == null)
                {
                    logInfo("Http Req OK, but there's no ret specified in the returned data." + $.toJSON(data))
                    alert("ERROR : ret: " + $.toJSON(data));
                }
                else
                {
                    if(data.ret == 0) // backend said success get data
                    {
                        _obj_.cb(data, _obj_.t, _obj_.p);
                    }
					else if(data.ret == 1) // login fail
					{
						data = { ret: -1, msg: 'notvalid user'};
						obj_.cb(data, _obj_.t, _obj_.p);
					}
                    else
                    {
						data.get_url_address = _url;
					    alert("GET AJAX ERROR: response error from server: " + $.toJSON(data));
                    }
                }
                
            }
           
        });
    }
	
	
	
	
});

$.extend(
{
    at_ajax_postExt : function (_obj_)
    {
		var _url =  bonita_server_ip + _obj_.url;
        var o_params = {};
        // conver the JavaScript obj to JSON str here
        o_params = $.toJSON(_obj_.param);
		logInfo("New Ajax POST Req: " +  o_params );
        $.ajax(
        {
			url: _url,
			type:"POST",
            data:o_params,
			beforeSend: function(xhr)
			{
				xhr.setRequestHeader("Authorization", "Basic " + $.base64.encode(sessionStorage.at_wf_username + ":" + sessionStorage.at_wf_password)); //May need to use "Authentication" instead
		    }
			,	
            success: function(data)
            {
                    
				if(data.ret == null)
                {
                    logInfo("Http Req OK, but there's no ret specified in the returned data." + $.toJSON(data))
                    alert("POST AJAX  ERROR: response error from server: " + $.toJSON(data));
                }
                else
                {
                    if(data.ret == 0) // backend said success get data
                    {
                         _obj_.cb(data, _obj_.t, _obj_.p);
                       
                    }else if(data.ret == 1)
					{
						location.href = bonita_login_page ;
					}
                    else
                    {
						data.post_url_address = _url;
						data.post_data = o_params2Server;
					    alert("POST AJAX ERROR: response error from server: " + $.toJSON(data));
                    }
                }
                
            }
           
        });
    }
	
	
	
	
});


$.extend(
{
    at_json_rpc : function (method, o_params2Server, jsAjaxCallback, _url) //o_params2Server ex: [1,"2',bac]
    {
        $.ajax(
        {
			url: _url,
			type:"POST",
            data: JSON.stringify ({jsonrpc:'2.0', method:method, params:o_params2Server, id:"jsonrpc"} ),  //JSON.stringify ({jsonrpc:'2.0',method:'echo', params:["abc"],id:"jsonrpc"} ),
            dataType:"json",
            success:  function (data)       { jsAjaxCallback(data.result)},
            error: function (err)  { alert ("Error on at_json_rpc");}

        });
    }
	
	
});

$.extend(
{
	
	 at_time : function (UNIX_timestamp_milliseconds)
	 {
		    var tmp = "0{%0}";
 	 		var a = new Date(UNIX_timestamp_milliseconds);
 	 		var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
     		var year = a.getFullYear();
     		var month = months[a.getMonth()];
			var d = a.getDate();
			var h = a.getHours();
			var m = a.getMinutes();
			var s = a.getSeconds();
     		var date = d > 9 ? d : tmp.replace(/{%0}/, d);
     		var hour = h > 9 ? h : tmp.replace(/{%0}/, h);
     		var min = m > 9 ? m : tmp.replace(/{%0}/, m);
     		var sec = s > 9 ? s : tmp.replace(/{%0}/, s);
     		var time = date+','+month+' '+year +'T'+hour+':'+min+':'+sec ;
     		return time;
    }
	
}
)



function logInfo(strMsg)
{
   if(window.console && window.console.warn)
        window.console.info("[at_log]", strMsg);

}

// Return new array with duplicate values removed
Array.prototype.unique =
  function() {
    var a = [];
    var l = this.length;
    for(var i=0; i<l; i++) {
      for(var j=i+1; j<l; j++) {
        // If this[i] is found later in the array
        if (this[i] === this[j])
          j = ++i;
      }
      a.push(this[i]);
    }
    return a;
  };
  
 $(document).ready(function() { 
 
 if(location.href.indexOf("84.198") > -1 && location.href.indexOf("wf_ui_view.html")  == -1)
 {
	 if(location.href.indexOf("findBonitaForm.html") > -1)
	 	return;
	 $('body').append("<h4 style='color:red;position:absolute; top: 5px; left: 5px;'>ALERT: THIS IS AT WORKFLOW DEVELOPING SITE</h4>");
	 
 }
 
 })