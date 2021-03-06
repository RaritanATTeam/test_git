
var fromIndex = 0;
var curIndex = 0;
var PAGE_ROW = 10;
var at_arr = [];
var bAppendProcessComboBox = true;
var cur_user = '';
var dummy = '';
var tbl_footer = '';
$(document).ready(function() {
	$("#assigned_tab thead td").attr("align", "center");
	$("#assigned_tab thead td.cls_left").attr("align", "left");
	$("#assigned_tab #id_img_created").attr('src', './images/sort_desc.png');
	$("#assigned_tab #id_pagination").data('page', 1);
    var _url = bonita_obj.getOwnedProcessNameListUrl;
	$.at_ajax_get(cbGetOwnedProcessNameList , _url);
	tbl_footer = $("#assigned_tab #id_pagination").html();
	//$("#id_pagination").html('');
	$("#assigned_tab #id_2_begin").live('click', cbShowFirstPage);
	$("#assigned_tab #id_2_end").live('click', cbShowLastPage);
	$("#assigned_tab #rally_link").live('click', cbRallyLink);
	$("#assigned_tab #id_2_previous").live('click', cbPreviousPage);
	$("#assigned_tab #id_2_next").live('click', cbNextPage);
    var _objHref = getURLParameters(location.href);
	cur_user = _objHref.user;
    $("#assigned_tab #id_updated").on('click', cbClickLastUpdated);
	$("#assigned_tab #id_updated").attr('sort','LAST_UPDATE_DESC');
	$("#assigned_tab #type_instance").on("change", cbChangeProcessType);
   // _url = bonita_obj.getInstanceListUrl;
    _url = bonita_obj.getAssignedTaskListUrl;
	_qs =bonita_obj.sortQs.replace(/{%0}/, fromIndex);
	_qs = _qs.replace(/{%1}/, PAGE_ROW);
	_qs = _qs.replace(/{%2}/, 'LAST_UPDATE_DESC');
	_url += _qs;
	
	$("#assigned_tab #tasklist").html('');
	$.at_ajax_get(cbNewTableList , _url);
});
function cbNextPage()
{
	var _p = $("#assigned_tab #id_pagination").data('page');
	
	var totalPage = $("#assigned_tab #id_pagination").data('total');
	if( totalPage == _p)
		return;
	var index = _p * PAGE_ROW;
	$("#assigned_tab #id_pagination").data('page',++_p);
	var _url = '';
	var key = $("#assigned_tab #type_instance option:selected").attr('id');
	if( key == "all_opt")
	    _url = bonita_obj.getAssignedTaskListUrl;
	else
	{
		_url = bonita_obj.getAssignedTaskListUrlExtUrl;
	    _url = _url.replace(/{%0}/, key);
	}
	_qs = bonita_obj.sortQs.replace(/{%0}/, index);
	_qs = _qs.replace(/{%1}/, PAGE_ROW);
	_qs = _qs.replace(/{%2}/, 'LAST_UPDATE_DESC');
	_url += _qs;
	$("#assigned_tab #tasklist").html('');
	$.at_ajax_get(cbNewTableList , _url);
	
}
function cbPreviousPage()
{
	var _p = $("#assigned_tab #id_pagination").data('page');
	if(1 == _p)
		return;
	$("#assigned_tab #id_pagination").data('page', --_p);
	var index = _p * PAGE_ROW - PAGE_ROW;
	var _url = '';
	var key = $("#assigned_tab  #type_instance option:selected").attr('id');
	if( key == "all_opt")
	    _url = bonita_obj.getAssignedTaskListUrl;
	else
	{
		_url = bonita_obj.getAssignedTaskListUrlExtUrl;
	    _url = _url.replace(/{%0}/, key);
	}
	
	_qs = bonita_obj.sortQs.replace(/{%0}/, index);
	_qs = _qs.replace(/{%1}/, PAGE_ROW);
	_qs = _qs.replace(/{%2}/, 'LAST_UPDATE_DESC');
	_url += _qs;
	$("#assigned_tab #tasklist").html('');
	$.at_ajax_get(cbNewTableList , _url);
	
}

function cbShowLastPage()
{
	var _p = $("#assigned_tab #id_pagination").data('page');
	
	var totalPage = $("#assigned_tab #id_pagination").data('total');
	if( totalPage == _p)
		return;
	$("#assigned_tab #id_pagination").data('page',totalPage);
	var _url = '';
	var key = $("#assigned_tab #type_instance option:selected").attr('id');
	if( key == "all_opt")
	    _url = bonita_obj.getAssignedTaskListUrl;
	else
	{
		_url = bonita_obj.getAssignedTaskListUrlExtUrl;
	    _url = _url.replace(/{%0}/, key);
	}
	var rows = $("#assigned_tab #type_instance option:selected").attr('rows');
	_qs = bonita_obj.sortQs.replace(/{%0}/, rows - PAGE_ROW);
	_qs = _qs.replace(/{%1}/, PAGE_ROW);
	_qs = _qs.replace(/{%2}/, 'LAST_UPDATE_DESC');
	_url += _qs;
	$("#assigned_tab #tasklist").html('');
	$.at_ajax_get(cbNewTableList , _url);
}
	


function cbShowFirstPage()
{
	var _p = $("#assigned_tab #id_pagination").data('page');
	if( 1 == _p)
		return;
	$("#assigned_tab #id_pagination").data('page', 1);
	var _url = '';
	var key = $("#assigned_tab #type_instance option:selected").attr('id');
	if( key == "all_opt")
	    _url = bonita_obj.getAssignedTaskListUrl;
	else
	{
		_url = bonita_obj.getAssignedTaskListUrlExtUrl;
	    _url = _url.replace(/{%0}/, key);
	}
	_qs = bonita_obj.sortQs.replace(/{%0}/, fromIndex);
	_qs = _qs.replace(/{%1}/, PAGE_ROW);
	_qs = _qs.replace(/{%2}/, 'LAST_UPDATE_DESC');
	_url += _qs;
	$("#assigned_tab #tasklist").html('');
	$.at_ajax_get(cbNewTableList , _url);
}

function cbGetOwnedProcessNameList(data)
{
	var _cmb = "<option id='{%0}' rows='{%row}'>{%1} ({%2})</option>";
	var _cmb_arr = data.process_list;
	var nTotal = 0;
	var _opts = '';
	for(var i = 0; i < _cmb_arr.length; ++i)
	{
		  nTotal += _cmb_arr[i].count;
		  var _str = _cmb.replace(/{%0}/,_cmb_arr[i].name);
		  _str = _str.replace(/{%1}/, _cmb_arr[i].description.replace(/_/, " "));
		  _str = _str.replace(/{%2}/, _cmb_arr[i].count);
		  _str = _str.replace(/{%row}/, _cmb_arr[i].count);
		  _opts += _str;
	}
	var _all = "<option id='all_opt' selected='true' rows='{%row}'>Show All Process ({%0}) </option>" + _opts;
	_all = _all.replace(/{%0}/, nTotal); 
	_all = _all.replace(/{%row}/, nTotal); 
	$("#assigned_tab #type_instance").html('');
	$("#assigned_tab #type_instance").append(_all);
	//alert($.toJSON(data));
}

function cbChangeProcessType()
{
	$("#assigned_tab #id_pagination").data('page', 1);
	var key = $("#assigned_tab #type_instance option:selected").attr('id');

	var _url = '';
	if( key == "all_opt")
	    _url = bonita_obj.getAssignedTaskListUrl;
	else
	{
		_url = bonita_obj.getAssignedTaskListUrlExtUrl;
	    _url = _url.replace(/{%0}/, key);
	}
	_qs = bonita_obj.sortQs.replace(/{%0}/, 0);
	_qs = _qs.replace(/{%1}/, PAGE_ROW);
	var _sortby = $("#assigned_tab #id_updated").attr('sort');
	_qs = _qs.replace(/{%2}/, _sortby);
	_url += _qs;
	
	$("#assigned_tab #tasklist").html('');
	$.at_ajax_get(cbNewTableList , _url);
}



function cmpCreatedUnixTime(ta,tb)
{
	var sortby = $("#assigned_tab #id_updated").attr('sort');
	if(sortby == 'asc'){
		return tb.created_timestamp - ta.created_timestamp;
	}else 
	    return ta.created_timestamp - tb.created_timestamp;
}

function cbClickLastUpdated()
{  
    var _sortby = $("#assigned_tab #id_updated").attr('sort');
	if (_sortby == 'LAST_UPDATE_ASC'){
		$("#assigned_tab #id_updated").attr('sort','LAST_UPDATE_DESC');
		$("#assigned_tab #id_img_created").attr('src', './images/sort_desc.png');
	}
	else{
	    $("#assigned_tab #id_updated").attr('sort','LAST_UPDATE_ASC');
		$("#assigned_tab #id_img_created").attr('src', './images/sort_asc_alt.png');
	}
	cbChangeProcessType();
}

function cbRallyLink(event)
{
	event.preventDefault();
	var newWindow = window.open(this.getAttribute('href'), '_blank');
	newWindow.focus();
}


function cbNewTableList(data)
{
	cur_user = $.cookie("loginName").toUpperCase();
	$("#id_user").html("USER: " + cur_user);
	$("#assigned_tab #id_pages").html('');
	$("#assigned_tab #tasklist").html("");
	var len = data.instance_list.length;
	if(len == 0)
	{
		$("#assigned_tab #id_user").html("NO TASK FOUND FOR: " + cur_user);
		return;	
	}
	at_arr = data.instance_list;
	var rows = '';
	var _cmb = "<option id='{%0}' {%1}>{%2}</option>";
	var _cmb_arr = [];
	var my_arr = [];
	var _opts = '';
	var cur_selected = $("#assigned_tab #type_instance option:selected").attr('id');
	var bFoundSelected = false;
	for (var i = 0; i < len ; ++i)
    {
		var style = "style='background-color:#FFFFFF;'";
		var tr = "<tr {%style}><td class='tbl_cell_description'><label title='{%t0}'>{%0}</label></td><td title='{%t1}' class='tbl_cell_description'><a style='color:blue;' id='rally_link' href='{%mylink2}'><label style='cursor:pointer;'>{%1}</label></a></td><td><a style='color:blue;' target='_blank' href='{%2}'>Form</a></td><td><a style='color:blue;' target='_blank' href='{%3}'>UI</a></td><td><label title='{%t4}'>{%4}</label></td><td class='tbl_cell_description' title='{%t5}'><label>{%5}</label></td></tr>";
		var strProcessName = data.instance_list[i].process_name.split("--")[0].replace(/_/g,  " ");
		_obj = { name: data.instance_list[i].process_name.split("--")[0], data: data.instance_list[i]};
		my_arr.push(_obj);
		_cmb_arr.push(_obj.name);
		
		tr = tr.replace(/{%0}/, strProcessName);
		tr = tr.replace(/{%t0}/, strProcessName);
		tr = tr.replace(/{%1}/, data.instance_list[i].description );
		tr = tr.replace(/{%t1}/, data.instance_list[i].description );
		tr = tr.replace(/{%mylink2}/, data.instance_list[i].description_link );
		tr = tr.replace(/{%2}/, data.instance_list[i].form_link);
		tr = tr.replace(/{%3}/, data.instance_list[i].wf_ui_view_link);
		tr = tr.replace(/{%4}/, $.at_time(data.instance_list[i].created_timestamp).split("T")[0]);
		tr = tr.replace(/{%t4}/, $.at_time(data.instance_list[i].created_timestamp).replace(/T/g,  " "));
		tr = tr.replace(/{%5}/, data.instance_list[i].activity_name);
		tr = tr.replace(/{%t5}/, data.instance_list[i].activity_name);
		if (i % 2 == 0)
		  tr = tr.replace(/{%style}/, style);
		else
		  tr = tr.replace(/{%style}/, '');
		rows += tr;
	   	
	}
	$("#assigned_tab #tasklist").append(rows);
	$("#assigned_tab tbody#tasklist td").attr("align", "left");
	
	var nRows = parseInt($("#assigned_tab #type_instance option:selected").attr('rows'));
	if(nRows > PAGE_ROW)
    {
		 $("#assigned_tab  #id_pagination").show();
		 $("#assigned_tab #id_pagination").html(tbl_footer);
		 var cur_page = $("#assigned_tab  #id_pagination").data('page');
		 var _s = "Page " + cur_page + " / ";
		 var nPage = nRows/PAGE_ROW;
		 nPage = Math.ceil(nPage)
		 _s += nPage; 
		 $("#assigned_tab #id_pagination").data('total', nPage);
		 
		 $("#assigned_tab #id_pages").html(_s);
	}else{
		$("#assigned_tab #id_pagination").hide();
	}
	
}

