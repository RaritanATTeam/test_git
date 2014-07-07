function showUserStoryEstimateProcess(tasks) {
	var result=getUserStoryEstimateProcess(tasks);
	var severity=result.severity;
	var tasks=result.tasks;
	var message=result.message;
	
	if(!message) {
		message="";
	}
	
	var html="<div class="+severity+" title='"+message+"'>"+
					"<div class='process_title'>User Story Estimate Process</div>";
	
	for(var key2 in tasks ) {
		var task=tasks[key2];
		var formattedId=task.FormattedID;
		
		var textClass= (task.severity=="critical") ? "critical_text" : "normal_text";

		html+="<div class='"+textClass+"'>";
		html+="<a href='https://rally1.rallydev.com/#/detail/task/"+task.ObjectID+"'>"+formattedId+"</a> ";
		html+=task._refObjectName+" ";
		html+="[<span class='owner_text'>"+task.Owner._refObjectName+"</span>] ";
		html+="("+task.Estimate+")";
		html+="</div>";
	}
	
	html+="</div>";
	
	return html;
}

function getUserStoryEstimateProcess(tasks) {
	var result=new ProcessResult();
		
	console.info("iterationInfo.startDate="+iterationInfo.startDate);
	
	var checkPointStartDate=addDate(iterationInfo.startDate, -5);
	var checkPointEndDate=addDate(iterationInfo.startDate, -3);
	
	var timeStatus=getCurrentTimeStatus(checkPointStartDate, checkPointEndDate);

	var isEstimated=true;
	for(var key in tasks ) {
		var task=tasks[key];
				
		var taskName=task._refObjectName;
		
		var estimate=task.Estimate;
		if(estimate==0 && timeStatus==3) {
			isEstimated=false;
			task.severity="critical";
		} else {
			task.severity="normal";
		}
				
		result.tasks.push(task);
	}
	
	if(timeStatus==3) {

		if(isEstimated==false) {
			result.severity="critical";
			result.message="Some tasks are not  estimated";
		} else {
			result.severity="normal";
		}

	} else {
		result.severity="none";
	}
	
	return result;
}