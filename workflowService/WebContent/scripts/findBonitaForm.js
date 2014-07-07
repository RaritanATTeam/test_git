 var urlObj = '';
$(document).ready(function() {
	urlObj = getURLParameters(location.href);
	//alert($.toJSON(urlObj));
	if (urlObj == {})
    {
		alert("can not find the link becuase of empty query string");
	    return;
	}
    _obj = $.secureEvalJSON(decodeURIComponent(urlObj.req));
	bonita_instanceName = _obj['bonita_instanceName'];

	//req={bonita_instanceName:dummy-4.4-11,cur_user:alexp.chen@raritna.com}
	_url = bonita_obj.getExternalUrlLinkUrl.replace(/{%0}/, bonita_instanceName);
	$.at_ajax_get(cbGetExternalUrlLink, _url);
	
  
});

function cbGetExternalUrlLink(obj)
{
	var _url = obj.url;

	if( _url != null && _url != '') {
		location.href = _url + "?req=" + urlObj.req ;
	} else {
		alert("This form is not assigned to you. ");
		history.back(-1);
	}
    	//alert("can not find the link ");
	
}


