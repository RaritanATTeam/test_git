var name = '';
var password = '';
var fLogin = false;
$(document).ready(function() {
	var chan = Channel.build({window: window.parent, origin: "*", scope: "testScope"});

chan.bind("login",cbLoginSubmit);

chan.bind("getGlobalVar", cbGetGlobalVarSubmit);
chan.bind('setGlobalVar', cbSetGlobalVarSubmit);

chan.bind("getLocalVar", cbGetLocalVarSubmit);
chan.bind('setLocalVar', cbSetLocalVarSubmit);

chan.bind("createInstance", cbCreateInstanceSubmit);
chan.bind("runTask", cbRunTaskSubmit);	
chan.bind("checkThinkTankId", cbCheckThinkTankIdSubmit);	
chan.bind("getUserList", cbGetUserListSubmit);	
//chan.bind("showWikiTask", cbGetUserListSubmit);
chan.bind("notifyWikiUpdate", cbNotifyWikiUpdateSumbit);	
  
});

function cbNotifyWikiUpdateSumbit(trans, params)
{
	var _url = bonita_obj.notifyWikiUpdateUrl;
	var _obj = { t: trans, p: params, param: {url:params.url}, url:_url, cb: cbNotifyWikiUpdate };
	$.at_ajax_postExt(_obj);
}

function cbNotifyWikiUpdate(data, trans, params)
{
	params.cb($.toJSON(data));
	trans.complete("complete");
}
function cbGetUserListSubmit(trans, params)
{
	var _url = bonita_obj.getCurrentUserListUrl
	$.at_ajax_getExt({cb:_cbGetUserList, t: trans, p:params}, _url);
}
function _cbGetUserList(data, trans, params)
{
	params.cb($.toJSON(data));
	trans.complete("complete");
}
function cbCheckThinkTankIdSubmit(trans, params)
{
	var _url = 'checkThinkTankId';
	var _obj = { t: trans, p: params, param: {tankId:params.tankId}, url:_url, cb: cbCheckThinkTankId };
	$.at_ajax_postExt(_obj);
}

function cbCheckThinkTankId (data, trans, params)
{
	params.cb($.toJSON(data));
	trans.complete("complete");
}

function cbRunTaskSubmit(trans, params)
{
	
	var _url = bonita_obj.runTaskUrl.replace(/{%0}/, params.full_instance_name);
	$.at_ajax_getExt({cb:cbRunTask, t: trans, p:params}, _url);
	
}

function cbRunTask(data,trans, params)
{
	params.cb($.toJSON(data));
	trans.complete("complete");
}

function cbCreateInstanceSubmit(trans, params)
{
	//params.cb($.toJSON({ret:0}));
	
	var _url = bonita_obj.createInstanceUrl;
	var _obj = { t: trans, p: params, param: params.my_new_instance, url:_url, cb: cbCreateInstance };
	
	//var _obj = { t: trans, p: params, param: {instance_name:'funnel', username: 'alexp.chen', variables:'<map><entry><string>instance_var</string><string>new value by alex</string></entry></map>'}, url:_url, cb: cbCreateInstance };
	$.at_ajax_postExt(_obj);
	
}

function cbCreateInstance(data,trans, params)
{
	params.cb($.toJSON(data));
	trans.complete("complete");
}

function cbSetLocalVarSubmit(trans, params)
{
		var _url = bonita_obj.setLocalVarUrl.replace(/{%0}/, params.full_instance_name);
		var _obj = { t: trans, p: params, param: params.vars, url:_url, cb: cbSetLocalVar };
		$.at_ajax_postExt(_obj);
}
function cbSetLocalVar(data,trans, params)
{
	params.cb($.toJSON(data));
	trans.complete("complete");
}

function cbGetLocalVarSubmit(trans, params)
{
	var _url = bonita_obj.getLocalVarUrl.replace(/{%0}/, params.full_instance_name);
	$.at_ajax_getExt({cb:cbGetLocalVar, t: trans, p:params}, _url);
	
}
function cbGetLocalVar(data,trans, params)
{
	params.cb($.toJSON(data));
	trans.complete("complete");
}

function cbSetGlobalVarSubmit(trans, params)
{
		var _url = bonita_obj.setGlobalVarUrl.replace(/{%0}/, params.full_instance_name);
		var _obj = { t: trans, p: params, param: params.vars, url:_url, cb: cbSetGlobalVar };
		$.at_ajax_postExt(_obj);
}

function cbSetGlobalVar(data,trans, params)
{
	params.cb($.toJSON(data));
	trans.complete("complete");
}

function cbGetGlobalVarSubmit(trans, params)
{
	var _url = bonita_obj.getGlobalVarUrl.replace(/{%0}/, params.full_instance_name);
	$.at_ajax_getExt({cb:cbGetGlobalVar, t: trans, p:params}, _url);
	
}

function cbGetGlobalVar(data,trans, params)
{
	params.cb($.toJSON(data));
	trans.complete("complete");
}


function cbLoginSubmit(trans, params)
{
	bonita_obj.username = params.username.indexOf("@") == -1 ? params.username + "@raritan.com" : params.username;//'alexp.chen@raritan.com' ;
	bonita_obj.password = params.password;//'bpm';
	$.at_ajax_getExt({cb:cbLogin, t: trans, p:params}, bonita_obj.validateUserUrl);
}

function cbUserList(data,trans, params)
{
	params.cb($.toJSON(data));
	trans.complete("complete");
}



function cbLogin(data,trans, params)
{
	params.cb($.toJSON(data));
	trans.complete("complete");
	
}


/*chan.bind("reverse", function(trans, params) {
    t =  params.str.split("").reverse().join("");
    params.cb(t);
    //trans.complete("complete");
});*/