function showUserStoryDesignProcess(tasks) {
	var result=getUserStoryDesignProcess(tasks);
	var severity=result.severity;
	var tasks=result.tasks;
	var message=result.message;
	
	if(!message) {
		message="";
	}
	
	var html="<div class="+severity+" title='"+message+"'>"+
					"<div class='process_title'>User Story Design Process</div>";
		
	for(var key2 in tasks ) {
		var task=tasks[key2];
		var formattedId=task.FormattedID;
		
		var textClass= (task.severity=="critical") ? "critical_text" : "normal_text";
		
		html+="<div class='"+textClass+"'>";
		html+="<a href='https://rally1.rallydev.com/#/detail/task/"+task.ObjectID+"'>"+formattedId+"</a> ";
		html+=task._refObjectName+" ";
		html+="[<span class='owner_text'>"+task.Owner._refObjectName+"</span>] ";
		html+="("+task.State+")";
		html+="</div>";
	}
	
	html+="</div>";
	
	return html;
}

function getUserStoryDesignProcess(tasks) {
	var result=new ProcessResult();
	
	var checkPointStartDate=new Date();
	var checkPointEndDate=new Date();
	
	checkPointStartDate=checkPointStartDate.setDate((iterationInfo.startDate).getDate()-5);
	checkPointStartDate=new Date(checkPointStartDate);
	
	checkPointEndDate=checkPointEndDate.setDate((iterationInfo.startDate).getDate()+3);
	checkPointEndDate=new Date(checkPointEndDate);
	
	var timeStatus=getCurrentTimeStatus(checkPointStartDate, checkPointEndDate);
	
	var statusLevel=100;
	for(var key in tasks ) {
		var task=tasks[key];
				
		var taskName=task._refObjectName;
		
		if(taskName.indexOf("Design & Review") !=-1) {

			var level=getStatusLevel(task.State);
			
			if(level<=2 && timeStatus==3) {
				task.severity="critical";
			} else {
				task.severity="normal";
			}
			
			if(level<=statusLevel) {
				statusLevel=level;
			}
			
			result.tasks.push(task);
		}
		
	}
		
	if(statusLevel<=2 && timeStatus==3) {
		result.severity="critical";
		result.message="Design tasks are not completed yet and behind schedule";
	} else {
		result.severity="normal";
	}

	return result;
}