var CURRENT_TASK = '<div title="{%0}" style="position:absolute;left:{%1}px;top:{%2}px;width:{%3}px;height:{%4}px;" class="div_pos"></div>'
var PRE_LINK = '<tr><td><br/><h1 style="padding-left:30px;"><a  href="javascript:void(0)" id="backHref">Back to previous page</a></h1></td></tr>'
var IMAGE_PATH = '<table class="table_workflow_diagram" border="0" cellspacing="0" cellpadding="0" align="LEFT"><tr><td><img src="{%0}"/></td></tr>{%1}<tr><td><h1 id="rallyid" style="padding:30px;">Graphic View for Rally ID: {%rally_id}</h1></td></tr></table>'
var custom_form_url = ''
var requester_url = ''
$(document).ready(function() {
	var urlObj = getURLParameters(location.href);
	//alert(decodeURIComponent(urlObj.req));
	var instanceName = decodeURIComponent(urlObj.instance_name);
	//alert(instanceName);
	var _url = bonita_obj.getWorkflowUIInfoUrl.replace(/{%0}/, instanceName);
	$.at_ajax_get(cbGetUiInfo, _url);	
  
});
/*
obj = {
		   "imgPath": "http://192.168.80.56:8087/png/{0}".format(_obj.png),
		   "x": 0, "y": 0, "w": 0, "h": 0,
		   "tooltip": _obj.tooltip,
		   "clickable_url": instance_url,
		   'requster_url': requesterURL,
		   'rally_formatted_id':rally_id
		  }
*/
	

function cbGetUiInfo(obj)
{
	custom_form_url = obj.clickable_url; // use for showing the custome form page made by AT team
	requester_url = obj.requster_url;    // use for redirect back to reqester UI page
	
	
	var imagePath = IMAGE_PATH.replace(/{%0}/, obj.imgPath);
	//alert(imagePath);
	obj.requster_url = '';
	if(obj.requster_url !=  '')
	{
		imagePath = imagePath.replace(/{%1}/, PRE_LINK);
	}else{
		imagePath = imagePath.replace(/{%1}/, "&nbsp;");
	}
	imagePath = imagePath.replace(/{%rally_id}/, obj.rally_formatted_id);
    //alert(imagePath);
	
	
	var curTask = CURRENT_TASK.replace(/{%0}/, obj.tooltip);
	curTask = curTask.replace(/{%1}/, obj.x - 20);
	curTask = curTask.replace(/{%2}/, obj.y - 5);
	curTask = curTask.replace(/{%3}/, obj.w -2);
	curTask = curTask.replace(/{%4}/, obj.h );
	$('body').append(curTask);
	$('body').append(imagePath);
	if(obj.rally_formatted_id == '')
	     $("#rallyid").html('');
	$("#loading").hide();
	$(".div_pos").bind("click", onClick_bonita_task_obj);
	$("#backHref").live("click", onClick_back2requester_url);
	
}
function onClick_back2requester_url(){
  
	location.href = requester_url;
}

function onClick_bonita_task_obj()
{
	if(custom_form_url != '')
    {
		////alert("You are going to redirect to Bonita Workflow UI page");
		location.href = custom_form_url;
	}else{
		//alert("this is end of process, the process has been archived in Bonita Workflow");
	}
}
