<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.security.*" %>
<%
%>
<html>
	<title>API Test - Workflow Service</title>
	<link rel="stylesheet" type="text/css" href="css/main.css" />
	<script src="scripts/jquery-1.7.1.min.js"></script>
	<script>
		
		var Base64 = {
		 
			// private property
			_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
		 
			// public method for encoding
			encode : function (input) {
				var output = "";
				var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
				var i = 0;
		 
				input = Base64._utf8_encode(input);
		 
				while (i < input.length) {
		 
					chr1 = input.charCodeAt(i++);
					chr2 = input.charCodeAt(i++);
					chr3 = input.charCodeAt(i++);
		 
					enc1 = chr1 >> 2;
					enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
					enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
					enc4 = chr3 & 63;
		 
					if (isNaN(chr2)) {
						enc3 = enc4 = 64;
					} else if (isNaN(chr3)) {
						enc4 = 64;
					}
		 
					output = output +
					this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) +
					this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);
		 
				}
		 
				return output;
			},
		 
			// public method for decoding
			decode : function (input) {
				var output = "";
				var chr1, chr2, chr3;
				var enc1, enc2, enc3, enc4;
				var i = 0;
		 
				input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
		 
				while (i < input.length) {
		 
					enc1 = this._keyStr.indexOf(input.charAt(i++));
					enc2 = this._keyStr.indexOf(input.charAt(i++));
					enc3 = this._keyStr.indexOf(input.charAt(i++));
					enc4 = this._keyStr.indexOf(input.charAt(i++));
		 
					chr1 = (enc1 << 2) | (enc2 >> 4);
					chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
					chr3 = ((enc3 & 3) << 6) | enc4;
		 
					output = output + String.fromCharCode(chr1);
		 
					if (enc3 != 64) {
						output = output + String.fromCharCode(chr2);
					}
					if (enc4 != 64) {
						output = output + String.fromCharCode(chr3);
					}
		 
				}
		 
				output = Base64._utf8_decode(output);
		 
				return output;
		 
			},
		 
			// private method for UTF-8 encoding
			_utf8_encode : function (string) {
				string = string.replace(/\r\n/g,"\n");
				var utftext = "";
		 
				for (var n = 0; n < string.length; n++) {
		 
					var c = string.charCodeAt(n);
		 
					if (c < 128) {
						utftext += String.fromCharCode(c);
					}
					else if((c > 127) && (c < 2048)) {
						utftext += String.fromCharCode((c >> 6) | 192);
						utftext += String.fromCharCode((c & 63) | 128);
					}
					else {
						utftext += String.fromCharCode((c >> 12) | 224);
						utftext += String.fromCharCode(((c >> 6) & 63) | 128);
						utftext += String.fromCharCode((c & 63) | 128);
					}
		 
				}
		 
				return utftext;
			},
		 
			// private method for UTF-8 decoding
			_utf8_decode : function (utftext) {
				var string = "";
				var i = 0;
				var c = c1 = c2 = 0;
		 
				while ( i < utftext.length ) {
		 
					c = utftext.charCodeAt(i);
		 
					if (c < 128) {
						string += String.fromCharCode(c);
						i++;
					}
					else if((c > 191) && (c < 224)) {
						c2 = utftext.charCodeAt(i+1);
						string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
						i += 2;
					}
					else {
						c2 = utftext.charCodeAt(i+1);
						c3 = utftext.charCodeAt(i+2);
						string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
						i += 3;
					}
		 
				}
		 
				return string;
			}
		 
		}
		
		//var JSON.stringify = JSON.stringify || function (obj) {
		var stringify = function (obj) {
			var t = typeof (obj);
			if (t != "object" || obj === null) {
				// simple data type
				if (t == "string") obj = '"'+obj+'"';
				return String(obj);
			}
			else {
				// recurse array or object
				var n, v, json = [], arr = (obj && obj.constructor == Array);
				for (n in obj) {
					v = obj[n]; t = typeof(v);
					if (t == "string") v = '"'+v+'"';
					else if (t == "object" && v !== null) v = stringify(v);
					json.push((arr ? "" : '"' + n + '":') + String(v));
				}
				return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
			}
		};
		
		function validateUser() {
		
			var username="randy.chen@raritan.com";
			var password="bpm";
			
			$("#input1").html("Authorization:Basic "+username+":"+password);
				
			$.ajax({
			  url:"/workflowService/at/webservice/1.0/validateUser",
			  type:"GET",
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  beforeSend: function(xhr) {
					xhr.setRequestHeader("Authorization", "Basic " + Base64.encode(username + ":" + password));
				  },
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result1").html(result);
				}
			})
		
		}
		
		function logout() {
		
			var url="/workflowService/at/webservice/1.0/logout";
		
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
							
			$("#input11").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result11").html(result);
				}
			})
		
		}

		function getCurrentUser() {
		
			var url="/workflowService/at/webservice/1.0/getCurrentUser";
		
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
							
			$("#input2").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result2").html(result);
				}
			})
		
		}
		
		function getCurrentUserList() {
		
			var url="/workflowService/at/webservice/1.0/getCurrentUserList";
		
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
							
			$("#input25").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result25").html(result);
				}
			})
		
		}
		
		function getOwnedProcessNameList() {
		
			var url="/workflowService/at/webservice/1.0/getOwnedProcessNameList";
			
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
			
			$("#input3").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result3").html(result);
				}
			})
		
		}
		
		function getAssignedTaskListAll() {
		
			var url="/workflowService/at/webservice/1.0/getAssignedTaskList";
			
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
			
			$("#input4").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result4").html(result);
				}
			})
		
		}
		
		function getAssignedTaskList(instanceName) {
		
			var url="/workflowService/at/webservice/1.0/getAssignedTaskList/"+instanceName;
			
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
			
			$("#input5").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result5").html(result);
				}
			})
		
		}
		
		function getGlobalVar(instanceName) {
		
			var url="/workflowService/at/webservice/1.0/"+instanceName+"/getGlobalVar";
			
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
			
			$("#input6").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result6").html(result);
				}
			})
		
		}
		
		function getLocalVar(instanceName) {
		
			var url="/workflowService/at/webservice/1.0/"+instanceName+"/getLocalVar";
			
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
			
			$("#input7").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result7").html(result);
				}
			})
		
		}
		
		function setGlobalVar(instanceName) {
		
			var url="/workflowService/at/webservice/1.0/"+instanceName+"/setGlobalVar";
			
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
		
			/*
			var jsonParam='{'+
				'"uuid":"the test uuid",'+
				'"instance_var":"instance_var content",'+
				'"list":"randy.chen@raritan.com"'+
			'}';
			*/
			
			var jsonParam='{'+
				'"list":"randy.chen@raritan.com"'+
			'}';
		
			$("#input8").html(input+"<br/>"+jsonParam);
		
			$.ajax({
			  url:url,
			  type:"POST",
			  data:jsonParam,
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result8").html(result);
				}
			})
		
		}

		function setLocalVar(instanceName) {
		
			var url="/workflowService/at/webservice/1.0/"+instanceName+"/setLocalVar";
			
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
		
			var jsonParam='{'+
				'"externalUrl":"the test externalUrl"'+
			'}';
		
			$("#input9").html(input+"<br/>"+jsonParam);
		
			$.ajax({
			  url:url,
			  type:"POST",
			  data:jsonParam,
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result9").html(result);
				}
			})
		
		}
		
		function getExternalUrlLink(instanceName) {
		
			var url="/workflowService/at/webservice/1.0/"+instanceName+"/getExternalUrlLink";
			
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
		
		
			$("#input91").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result91").html(result);
				}
			})
		
		}
		
		function getExternalUrlLink(instanceName) {
		
			var url="/workflowService/at/webservice/1.0/"+instanceName+"/getExternalUrlLink";
			
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
		
			$("#input91").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result91").html(result);
				}
			})
		
		}
		
		function getWorkflowUIInfo(instanceName) {
		
			var url="/workflowService/at/webservice/1.0/"+instanceName+"/getWorkflowUIInfo";
			
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
		
			$("#input92").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result92").html(result);
				}
			})
		
		}
		
		function runTask(instanceName) {
		
			var url="/workflowService/at/webservice/1.0/"+instanceName+"/runTask";
			
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
			
			$("#input10").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result10").html(result);
				}
			})
		
		}
		
		function checkNewTAFromRally() {
		
			var url="/workflowService/at/webservice/1.0/checkNewTAFromRally";
		
			var input="<a href='"+
							window.location.protocol+"//"+
							window.location.hostname+":"+
							window.location.port+
							url+"'>Test URL</a>";
							
			$("#input11").html(input);
		
			$.ajax({
			  url:url,
			  type:"GET",
			  dataType:"json",
			  success: function(data){
					console.log(data);
					var result=stringify(data);
					$("#result11").html(result);
				}
			})
		
		}

	</script>

	<body>
		<h3>Workflow API Test</h3>
		<table width="100%" border>
			<tr>
				<th width="20%">API Name</th>
				<th width="40%">Input</th>
				<th width="40%">Output</th>
			</tr>
			
			<tr>
				<td><a href="javascript:validateUser()"> validateUser </a></td>
				<td><div id="input1">&nbsp;</div></td>
				<td><div id="result1">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:logout()"> logout </a></td>
				<td><div id="input11">&nbsp;</div></td>
				<td><div id="result11">&nbsp;</div></td>
			</tr>
	
			<tr>
				<td><a href="javascript:getCurrentUser()"> getCurrentUser </a></td>
				<td><div id="input2">&nbsp;</div></td>
				<td><div id="result2">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:getCurrentUserList()"> getCurrentUserList </a></td>
				<td><div id="input25">&nbsp;</div></td>
				<td><div id="result25">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:getOwnedProcessNameList()"> getOwnedProcessNameList </a></td>
				<td><div id="input3">&nbsp;</div></td>
				<td><div id="result3">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:getAssignedTaskListAll()"> getAssignedTaskListAll (All) </a></td>
				<td><div id="input4">&nbsp;</div></td>
				<td><div id="result4">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:getAssignedTaskList('Review_Rally_US_Process--1.7')"> getAssignedTaskList </a></td>
				<td><div id="input5">&nbsp;</div></td>
				<td><div id="result5">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:getGlobalVar('Review_Rally_US_Process--1.7--1')"> getGlobalVar </a></td>
				<td><div id="input6">&nbsp;</div></td>
				<td><div id="result6">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:getLocalVar('Review_Rally_US_Process--1.7--1')"> getLocalVar </a></td>
				<td><div id="input7">&nbsp;</div></td>
				<td><div id="result7">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:setGlobalVar('AT_Funnel_Process--1.3--10')"> setGlobalVar </a></td>
				<td><div id="input8">&nbsp;</div></td>
				<td><div id="result8">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:setLocalVar('Review_Rally_US_Process--1.7--1')"> setLocalVar </a></td>
				<td><div id="input9">&nbsp;</div></td>
				<td><div id="result9">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:getExternalUrlLink('Review_Rally_US_Process--1.7--1')"> getExternalUrlLink </a></td>
				<td><div id="input91">&nbsp;</div></td>
				<td><div id="result91">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:getWorkflowUIInfo('Review_Rally_US_Process--1.7--1')"> getWorkflowUIInfo </a></td>
				<td><div id="input92">&nbsp;</div></td>
				<td><div id="result92">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:runTask('Vote_Rally_US__Process--1.7')"> runTask </a></td>
				<td><div id="input10">&nbsp;</div></td>
				<td><div id="result10">&nbsp;</div></td>
			</tr>
			
			<tr>
				<td><a href="javascript:checkNewTAFromRally()"> checkNewTAFromRally </a></td>
				<td><div id="input11">&nbsp;</div></td>
				<td><div id="result11">&nbsp;</div></td>
			</tr>
			
		</table>
		
	</body>
</html>