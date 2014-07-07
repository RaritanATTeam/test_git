$(document).ready(function() {
	
	//var urlObj = getURLParameters(location.href);
	//var jsonValue = $.secureEvalJSON(decodeURIComponent(urlObj.info));
	
	$("#tasklist").html('');
	$("table").show();
	var _url = bonita_obj.getProcessNameListUrl;
	$.at_ajax_get(cbProcessNameList , _url);
	
	$("#type_instance").on("change", cbChangeType);
    _url = bonita_obj.getInstanceListUrl;
	$("#tasklist").html('');
	$.at_ajax_get(cbNewList , _url);
  
});

function cbProcessNameList(data)
{
	//$("#type_instance").append("<option>Alex</option>");
	var len =  data.process_list.length;var rows = '';
	for (var i = 0; i < len ; ++i)
    {
		var tr = "<option>{%0}</option>";
		tr = tr.replace(/{%0}/, data.process_list[i]);
		rows += tr;
	}
	
	$("#type_instance").append(rows);
}

function cbChangeType()
{
	var _url  = '';
	var t = $("#type_instance option:selected").text();
	if (t != "All")
		 _url = bonita_obj.getInstanceListUrl + "/" + t;
	else
	     _url = bonita_obj.getInstanceListUrl;
	
	$("#tasklist").html('');
	$.at_ajax_get(cbNewList , _url);
	
}

function cbNewList(data)
{
	//alert($.toJSON(data));
	if(data.ret !=0)
		return;
	//instance_list: [ { instance_name: abc, instance_link: http://ab.com, process_name: kd}]
	var len = data.instance_list.length;
	var rows = '';
	for (var i = 0; i < len ; ++i)
    {
		var style = "style='background-color:#9CC;'";
		var tr = "<tr {%style}><td><label>{%0}</label></td><td><a href='{%1}'>Link</a></td><td><a href='{%2}'>Link</a></td></tr>";
		tr = tr.replace(/{%0}/, data.instance_list[i].instance_name);
		tr = tr.replace(/{%1}/, data.instance_list[i].instance_link);
		tr = tr.replace(/{%2}/, data.instance_list[i].wf_ui_view_link);
		if (i % 2 == 1)
		  tr = tr.replace(/{%style}/, style);
		else
		  tr = tr.replace(/{%style}/, '');
		rows += tr;
	   	
	}
	
	$("#tasklist").append(rows);
	
}

