
var owned_obj = {PAGE_ROW : 100000, tbl_footer:'', user_detail_table:'', ajax: false};
var user_entry = {
                    "process_name": "vote for us",
                    "owner": "randy.chen",
                    "wf_ui_view_link": "http://xxx.com",
                    "description": "pass"
           };
var loading_str = "Loading Please Wait...<image style='padding-left:15px;' src='./images/ajax-loader.gif'/>";

$(document).ready(function() {
	$("#id_refresh_owned").click(cbRefreshOwnedTask);
	$("#id_show_none").click(cbHideOwnedSubProcess);
	$("#id_show_all").click(cbShowOwnedSubProcess);
	loading_str = $('#id_user').html();
	owned_obj.user_detail_table = $("#id_owned_user_details").html();
	$("#owned_tab thead td").attr("align", "center");
	$("#owned_tab thead td.cls_left").attr("align", "left");
	
	$("#owned_tab #id_pagination").data('page', 1);
    //var _url = bonita_obj.getOwnedProcessNameListUrl;
	//$.at_ajax_get(cbGetOwnedProcessNameList , _url);
	owned_obj.tbl_footer = $("#owned_tab #id_pagination").html();
	
	$("#owned_tab #id_2_begin").live('click', cbShowOwnedFirstPage);
	
	$("#owned_tab #id_2_end").live('click', cbShowOwnedLastPage);
	$("#owned_tab #rally_link").live('click', cbRallyLink);
	$("#owned_tab #id_2_previous").live('click', cbPreviousOwnedPage);
	$("#owned_tab #id_2_next").live('click', cbNexOwnedtPage);
   
    //$("#owned_tab #id_updated").on('click', cbClickOwnedLastUpdated);
	$("#owned_tab #id_updated").attr('sort','LAST_UPDATE_DESC');
	$("#owned_tab #type_instance").on("change", cbChangeOwnedProcessType);
	
	$(".cls_owned_tab").on('click', cbClickOwnedTask);
	
	$("#owned_tab img.cls_voter_details").live('click', cbToggoleUserDetails);
	$("#owned_tab #tasklist").html('');
	
	
});

function cbHideOwnedSubProcess()
{
	$("div.cls_tbl_wrapper").hide();
}
function cbShowOwnedSubProcess()
{
	$("div.cls_tbl_wrapper").show();
}
function cbRefreshOwnedTask()
{
	$("#owned_tab #tasklist").html('');
	var _str = $('#id_span_refresh_owned').html();
	$('#id_user').html(_str);
	
	 _url = bonita_obj.getOwnedTaskListUrl;
	_qs =bonita_obj.sortQs.replace(/{%0}/, 0);
	_qs = _qs.replace(/{%1}/, owned_obj.PAGE_ROW);
	_qs = _qs.replace(/{%2}/, 'LAST_UPDATE_DESC');
	_url += _qs;
	$.at_ajax_get(cbNewOwnedTableList , _url);
}

function cbClickOwnedTask()
{
	if(owned_obj.ajax == true)
		return;
	owned_obj.ajax = true;
	$('#id_user').html('');
	$('#id_user').html(loading_str);
	 _url = bonita_obj.getOwnedTaskListUrl;
	_qs =bonita_obj.sortQs.replace(/{%0}/, 0);
	_qs = _qs.replace(/{%1}/, owned_obj.PAGE_ROW);
	_qs = _qs.replace(/{%2}/, 'LAST_UPDATE_DESC');
	_url += _qs;
	$.at_ajax_get(cbNewOwnedTableList , _url);
	
}

function parserUserList(_obj)
{
	var row = "<tr {%style}><td><label class='tbl_cell_description' title='{%t0}'>{%t0}</label></td><td><label class='tbl_cell_description' title='{%t1}'>{%t1}</label></td>  <td> <label class='tbl_cell_description'  title='{%t2}'>{%t2}</label></td><td><a style='color:blue;' href='{%3}' target='_blank'>UI</a></td><td><label class='tbl_cell_description' title='{%t4}'>{%t4}</label> </td> </tr>";
	//return row;
	var _len = _obj.length;
	var total = '';
	for(var i = 0; i < _len; ++i)
	{
		var tmp = '';
		var style = "style='background-color:#cccccc;'";
		tmp = row.replace(/{%t0}/g, _obj[i].process_name.split("--")[0].replace(/_/g,  " "));
		tmp = tmp.replace(/{%t4}/g, _obj[i].owner);
		tmp = tmp.replace(/{%t2}/g, _obj[i].description.toUpperCase() == "NOT VOTE" ? "Wait for Reviwer Feedback (NO INPUT FOUND)" : _obj[i].description.toUpperCase());
		tmp = tmp.replace(/{%3}/, _obj[i].wf_ui_view_link);
		tmp = tmp.replace(/{%t1}/g, _obj[i].activity_name.replace(/_/g,  " "));
		if (i % 2 == 1)
		  tmp = tmp.replace(/{%style}/, style);
		else
		  tmp = tmp.replace(/{%style}/, '');
		total += tmp;
	}
	
	return total;
}

function cbToggoleUserDetails()
{
	$(".cls_voter_details").attr("src", "./images/os_nav_arrow_inactive_cui.gif");
	$(this).attr('src', "./images/os_nav_arrow_active_cui.gif");
	$(".cls_tbl_wrapper").html('');
	var base64 = $(this).attr('user_data');
	
	var _str = $.base64.decode(base64);
	
	var _obj = JSON.parse(_str);
	var rows = parserUserList(_obj);
	$(this).parent().append(owned_obj.user_detail_table);
	
	$("tbody#id_userlist").append(rows);
	$("#id_user_table thead td").attr('align','center');
	$("#id_user_table thead label.sub_ui").attr('align','left');
	$("#id_user_table").show();
}


function cbNexOwnedtPage()
{
	var _p = $("#owned_tab #id_pagination").data('page');
	
	var totalPage = $("#owned_tab #id_pagination").data('total');
	if( totalPage == _p)
		return;
	var index = _p * owned_obj.PAGE_ROW;
	$("#owned_tab #id_pagination").data('page',++_p);
	var _url = '';
	var key = $("#owned_tab #type_instance option:selected").attr('id');
	if( key == "all_opt")
	    _url = bonita_obj.getOwnedTaskListUrl;
	else
	{
		_url = bonita_obj.getOwnedTaskListUrlExtUrl;
	    _url = _url.replace(/{%0}/, key);
	}
	_qs = bonita_obj.sortQs.replace(/{%0}/, index);
	_qs = _qs.replace(/{%1}/, owned_obj.PAGE_ROW);
	_qs = _qs.replace(/{%2}/, 'LAST_UPDATE_DESC');
	_url += _qs;
	$("#owned_tab #tasklist").html('');
	$.at_ajax_get(cbNewOwnedTableList , _url);
	
}
function cbPreviousOwnedPage()
{
	var _p = $("#owned_tab #id_pagination").data('page');
	if(1 == _p)
		return;
	$("#owned_tab #id_pagination").data('page', --_p);
	var index = _p * owned_obj.PAGE_ROW - owned_obj.PAGE_ROW;
	var _url = '';
	var key = $("#owned_tab  #type_instance option:selected").attr('id');
	if( key == "all_opt")
	    _url = bonita_obj.getOwnedTaskListUrl;
	else
	{
		_url = bonita_obj.getOwnedTaskListUrlExtUrl;
	    _url = _url.replace(/{%0}/, key);
	}
	
	_qs = bonita_obj.sortQs.replace(/{%0}/, index);
	_qs = _qs.replace(/{%1}/, owned_obj.PAGE_ROW);
	_qs = _qs.replace(/{%2}/, 'LAST_UPDATE_DESC');
	_url += _qs;
	$("#owned_tab #tasklist").html('');
	$.at_ajax_get(cbNewOwnedTableList , _url);
	
}

function cbShowOwnedLastPage()
{
	var _p = $("#owned_tab #id_pagination").data('page');
	
	var totalPage = $("#owned_tab #id_pagination").data('total');
	if( totalPage == _p)
		return;
	$("#owned_tab #id_pagination").data('page',totalPage);
	var _url = '';
	var key = $("#owned_tab #type_instance option:selected").attr('id');
	if( key == "all_opt")
	    _url = bonita_obj.getOwnedTaskListUrl;
	else
	{
		_url = bonita_obj.getOwnedTaskListUrlExtUrl;
	    _url = _url.replace(/{%0}/, key);
	}
	var rows = $("#owned_tab #type_instance option:selected").attr('rows');
	_qs = bonita_obj.sortQs.replace(/{%0}/, rows - owned_obj.PAGE_ROW);
	_qs = _qs.replace(/{%1}/, owned_obj.PAGE_ROW);
	_qs = _qs.replace(/{%2}/, 'LAST_UPDATE_DESC');
	_url += _qs;
	$("#owned_tab #tasklist").html('');
	$.at_ajax_get(cbNewOwnedTableList , _url);
}
	


function cbShowOwnedFirstPage()
{
	var _p = $("#owned_tab #id_pagination").data('page');
	if( 1 == _p)
		return;
	$("#owned_tab #id_pagination").data('page', 1);
	var _url = '';
	var key = $("#owned_tab #type_instance option:selected").attr('id');
	if( key == "all_opt")
	    _url = bonita_obj.getOwnedTaskListUrl;
	else
	{
		_url = bonita_obj.getOwnedTaskListUrlExtUrl;
	    _url = _url.replace(/{%0}/, key);
	}
	_qs = bonita_obj.sortQs.replace(/{%0}/, 0);
	_qs = _qs.replace(/{%1}/, owned_obj.PAGE_ROW);
	_qs = _qs.replace(/{%2}/, 'LAST_UPDATE_DESC');
	_url += _qs;
	$("#owned_tab #tasklist").html('');
	$.at_ajax_get(cbNewOwnedTableList , _url);
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
		  _str = _str.replace(/{%1}/, _cmb_arr[i].description.replace(/_/g, " "));
		  _str = _str.replace(/{%2}/, _cmb_arr[i].count);
		  _str = _str.replace(/{%row}/, _cmb_arr[i].count);
		  _opts += _str;
	}
	var _all = "<option id='all_opt' selected='true' rows='{%row}'>Show All Process ({%0}) </option>" + _opts;
	_all = _all.replace(/{%0}/, nTotal); 
	_all = _all.replace(/{%row}/, nTotal); 
	$("#owned_tab #type_instance").html('');
	$("#owned_tab #type_instance").append(_all);
	//alert($.toJSON(data));
}

function cbChangeOwnedProcessType()
{
	$("#owned_tab #id_pagination").data('page', 1);
	var key = $("#owned_tab #type_instance option:selected").attr('id');

	var _url = '';
	if( key == "all_opt")
	    _url = bonita_obj.getOwnedTaskListUrl;
	else
	{
		_url = bonita_obj.getOwnedTaskListUrlExtUrl;
	    _url = _url.replace(/{%0}/, key);
	}
}




function cbClickOwnedLastUpdated()
{  
    var _sortby = $("#owned_tab #id_updated").attr('sort');
	if (_sortby == 'LAST_UPDATE_ASC'){
		$("#owned_tab #id_updated").attr('sort','LAST_UPDATE_DESC');
		$("#owned_tab #id_img_created").attr('src', './images/sort_desc.png');
	}
	else{
	    $("#owned_tab #id_updated").attr('sort','LAST_UPDATE_ASC');
		$("#owned_tab #id_img_created").attr('src', './images/sort_asc_alt.png');
	}
	cbChangeOwnedProcessType();
}


function cbNewOwnedTableList(data)
{
	var cur_user = $.cookie("loginName").toUpperCase();
	
	$("#id_user").html("USER: " + cur_user + '<a title="click to logout" style="padding-left:8px;color:#FF0000;" href="#" id="id_logout">LOGOUT</a>');
	$("#owned_tab #id_pages").html('');
	$("#owned_tab #tasklist").html("");
	var len = data.instance_list.length;
	if(len == 0)
	{
		$("#id_load_owned_tab").html("NO OWNED TASK FOUND FOR: " + cur_user);
		return;	
	}
	var at_arr = data.instance_list;
	var rows = '';
	var _cmb = "<option id='{%0}' {%1}>{%2}</option>";
	var _cmb_arr = [];
	var my_arr = [];
	var _opts = '';
	var cur_selected = $("#owned_tab #type_instance option:selected").attr('id');
	var bFoundSelected = false;
	//var _j = "FOUND<bold> " + len + " </bold>OWNED WORKFLOW PROCESS RECORD";
	$("#id_load_owned_tab").hide();
	for (var i = 0; i < len ; ++i)
    {
		var style = "style='background-color:#FFFFFF;'";
		var tr = "<tr {%style}><td title='{%t0}' class='tbl_cell_description'><label>{%t0}</label></td><td><a style='color:blue;' id='rally_link' href='{%mylink2}'><label title='{%t1}' class='tbl_cell_description' style='cursor:pointer;'>{%t1}</label></a>{%sub_process}</td><td title='{%t4}'><label>{%4}</label></td></tr>";
		var strProcessName = data.instance_list[i].process_name.split("--")[0].replace(/_/g,  " ");
		_obj = { name: data.instance_list[i].process_name.split("--")[0], data: data.instance_list[i]};
		my_arr.push(_obj);
		_cmb_arr.push(_obj.name);
		
		tr = tr.replace(/{%t0}/g, strProcessName);
		tr = tr.replace(/{%t1}/g, data.instance_list[i].description );
		tr = tr.replace(/{%mylink2}/, data.instance_list[i].description_link );
		tr = tr.replace(/{%4}/, $.at_time(data.instance_list[i].created_timestamp).split("T")[0]);
		tr = tr.replace(/{%t4}/g, $.at_time(data.instance_list[i].created_timestamp).replace(/T/g,  " "));
		var _str = owned_obj.user_detail_table;
		_str = _str.replace(/<!--{%sub_process_body}-->/ , parserUserList(data.instance_list[i].sub_list));
		
		tr = tr.replace(/{%sub_process}/, _str ); // TODO data.instance_list[i].sub_list
		if (i % 2 == 0)
		  tr = tr.replace(/{%style}/, style);
		else
		  tr = tr.replace(/{%style}/, '');
		rows += tr;
	   	
	}
	$("#owned_tab #tasklist").append(rows);
	$("#owned_tab tbody#tasklist td").attr("align", "left");
	$("#id_user_table thead td").attr('align','center');
	$("#id_user_table #id_ui_link").attr('align','left');
	
	var nRows = parseInt($("#owned_tab #type_instance option:selected").attr('rows'));
	if(nRows > owned_obj.PAGE_ROW)
    {
		 $("#owned_tab  #id_pagination").show();
		 $("#owned_tab #id_pagination").html(owned_obj.tbl_footer);
		 var cur_page = $("#owned_tab  #id_pagination").data('page');
		 var _s = "Page " + cur_page + " / ";
		 var nPage = nRows/owned_obj.PAGE_ROW;
		 nPage = Math.ceil(nPage)
		 _s += nPage; 
		 $("#owned_tab #id_pagination").data('total', nPage);
		 
		 $("#owned_tab #id_pages").html(_s);
	}else{
		$("#owned_tab #id_pagination").hide();
	}
	
}

