function showCodeReviewTaskProcess(tasks) {
	var result=getCodeReviewTaskProcess(tasks);
	var severity=result.severity;
	var tasks=result.tasks;
	var message=result.message;
	
	if(!message) {
		message="";
	}
	
	var html="<div class="+severity+" title='"+message+"'>"+
					"<div class='process_title'>Code Review Task Process</div>";
	
	for(var key2 in tasks ) {
		var task=tasks[key2];
		var formattedId=task.FormattedID;
		html+="<div>";
		html+="<a href='https://rally1.rallydev.com/#/detail/task/"+task.ObjectID+"'>"+formattedId+"</a> ";
		html+=task._refObjectName+" ";
		html+="[<span class='owner_text'>"+task.Owner._refObjectName+"</span>] ";
		html+="("+task.State+")";
		html+="</div>";
	}
	
	html+="</div>";
	
	return html;
}

function getCodeReviewTaskProcess(tasks) {
	var result=new ProcessResult();

	var statusLevel=0;
	for(var key in tasks ) {
		var task=tasks[key];
						
		var taskName=task._refObjectName;
		
		if(
			(taskName.toLowerCase()).indexOf("code review") !=-1) {
		
			console.info("test task="+task._refObjectName+" task.State="+	task.State);
			var level=getStatusLevel(task.State);
			console.info("task.State="+task.State+" level="+level);
			if(level>=statusLevel) {
				statusLevel=level;
			}
			
			result.tasks.push(task);
		}
		
	}
	
	var checkPointStartDate=new Date();
	var checkPointEndDate=new Date();
	
	checkPointStartDate=checkPointStartDate.setDate((iterationInfo.startDate).getDate()+3);
	checkPointStartDate=new Date(checkPointStartDate);
	
	checkPointEndDate=checkPointEndDate.setDate((iterationInfo.startDate).getDate()+17);
	checkPointEndDate=new Date(checkPointEndDate);
	
	var timeStatus=getCurrentTimeStatus(checkPointStartDate, checkPointEndDate);
	
	if(timeStatus==1) {
		result.severity="none";
	} else if(timeStatus==2) {
		result.severity="normal";
	} else if(timeStatus==3) {
		if(statusLevel<3) {
			result.severity="critical";
		} else {
			result.severity="normal";
		}
	}

	return result;
}