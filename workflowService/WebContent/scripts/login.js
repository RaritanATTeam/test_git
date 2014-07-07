var name = '';
var password = '';
$(document).ready(function() {
	$(document).keypress(function(event){
 
	var keycode = (event.keyCode ? event.keyCode : event.which);
	if(keycode == '13'){
		$("#submitButton").attr('disabled','disabled');
		cbLoginSubmit();
	}
 
	});
	//var urlObj = getURLParameters(location.href);
	//var jsonValue = $.secureEvalJSON(decodeURIComponent(urlObj.info));
	$("#submitButton").on('click', cbLoginSubmit);
    name = $.cookie("loginName");
	password = $.cookie("password");
	if(name != '')
    {
		$("#username").val(name);
		$("#password").val(password);
	}
	//#errorMessage
  
});
function cbPost(data)
{
   alert($.toJSON(data));	
}

function cbLoginSubmit()
{
	bonita_obj.username = $("#username").val().toLowerCase() ;
	bonita_obj.password = $("#password").val();
	$.at_ajax_get(cbLogin, bonita_obj.validateUserUrl);
}

function cbLogin(data)
{
	if(data.ret == 0)
    {
		$.cookie("loginName",bonita_obj.username , { expires: 365 * 2 });
		$.cookie("password",bonita_obj.password , { expires: 365 * 2 });
		logInfo("log in success with: " + bonita_obj.username + ":" + bonita_obj.password);
		location.href = "./instanceList.html";
	}else{
		logInfo("log in fail with username:password as  " + bonita_obj.username + ":" + bonita_obj.password);
		logInfo("login fail from server: " + data.msg);
		alert("Login failed.");
	}
	
}
