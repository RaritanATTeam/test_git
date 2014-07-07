#!/usr/bin/env python

from lxml import etree, objectify
from StringIO import StringIO
from subprocess import Popen
import subprocess
import json
import logging
import sys

logger = logging.getLogger('py_wf_ui_parser')
hdlr = logging.FileHandler('/tmp/py_wf_ui_parser_.log')
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
logger.addHandler(hdlr)
logger.setLevel(logging.INFO)

def getTaskBoundsEx(_obj):#{'instance_name': 'RallyUS--4.4--13', 'username': 'abc', 'password': '211'}
	my_obj = {'ret': 0, 'msg':'', 'h': 10, 'w': 10, 'x': 10, 'y': 10}
	curTaskName = ''
	#uuidValue = ''
	#activityDefinitionUUID = ''
	bpmnName = _obj['instance_name'].split("--")[0]
	bpmnPath = "/home/atuser/projects/workflow/server/workflowService/WebContent/bpmn/{0}.bpmn".format(bpmnName)
	f = ''
	try:
		logger.info("try to find the file at: {0}".format(bpmnPath))
		f = open(bpmnPath,"r")
	except:
		logger.info("ERROR: can not find the file at: {0}".format(bpmnPath))
		my_obj["msg"] = "ERROR: can not find the file at: {0}".format(bpmnPath)
		my_obj["ret"] = -1;
		return json.dumps(my_obj)
	_strXML = f.read()
	f.close()
	root = objectify.fromstring(_strXML)
	#print root.tag
	bpmn = getattr(root, "{http://www.omg.org/spec/BPMN/20100524/DI}BPMNDiagram")
	#print bpmn.BPMNPlane.get("bpmnElement")


	fEND_OF_PROCESS = False
	for _item in bpmn.BPMNPlane.BPMNShape:
		_str = _item.get("bpmnElement")
		if _str == "END_OF_PROCESS":
			fEND_OF_PROCESS = True
			bound = _item["{http://www.omg.org/spec/DD/20100524/DC}Bounds"]
			my_obj["h"] = bound.get("height").replace(".0",'')
			my_obj["w"]= bound.get("width").replace(".0",'')
			my_obj["x"] = bound.get("x").replace(".0",'')
			my_obj["y"] = bound.get("y").replace(".0",'')
			break
	if not fEND_OF_PROCESS:
		logger.info("ERROR: can not find END_OF_PROCESS task name at the file path: {0}".format(bpmnPath))
		my_obj["msg"] = "ERROR: can not find END_OF_PROCESS task name at the file path: {0}".format(bpmnPath)
		my_obj["ret"] = -2;
		return json.dumps(my_obj)

	_BONTIA_INFO = '''http://localhost:9080/bonita-server-rest/API/queryRuntimeAPI/getLightActivityInstancesFromRootByState/READY'''
	_CURL_CMD= '''curl -s --data "options=user:{0}&rootInstanceUUIDs={1}" --user restuser:restbpm '''
	_cmd = _CURL_CMD.format(_obj['username'], _obj['instance_name'] ) + _BONTIA_INFO
	_output = Popen(_cmd, shell=True,stdout=subprocess.PIPE)
	_output = _output.communicate()[0]
	logger.info("\nthe output from bonita rest API is {0}\n\n".format(_output))
	root = objectify.fromstring(_output)
	try:
		curTaskName =  root.entry.list.LightActivityInstance.name.text
		#uuidValue = root.entry.list.LightActivityInstance.uuid.value.text
		#activityDefinitionUUID = root.entry.list.LightActivityInstance.activityDefinitionUUID.value.text
	except:
		return json.dumps(my_obj)

	root = objectify.fromstring(_strXML)
	#print root.tag
	bpmn = getattr(root, "{http://www.omg.org/spec/BPMN/20100524/DI}BPMNDiagram")
	#print bpmn.BPMNPlane.get("bpmnElement")

	for _item in bpmn.BPMNPlane.BPMNShape:
		_str = _item.get("bpmnElement")
		if curTaskName == _str:

			bound = _item["{http://www.omg.org/spec/DD/20100524/DC}Bounds"]
			my_obj["h"] = bound.get("height").replace(".0",'')
			my_obj["w"]= bound.get("width").replace(".0",'')
			my_obj["x"] = bound.get("x").replace(".0",'')
			my_obj["y"] = bound.get("y").replace(".0",'')
			return json.dumps(my_obj)

if __name__ == "__main__":
	_default = {'ret': 1, 'msg':''}
	_ok = True
	_obj = {}
	_jsonStr = ''

	for _str in sys.argv[1:]:
		_jsonStr += _str
	try:
		logger.info("the input string is {0}".format(_jsonStr))
		_obj = json.loads(_jsonStr)
	except:
		_default["msg"] = "ERROR: not a valid json string in cmd arg: {0}".format(_jsonStr)
		logger.info("ERROR: not a valid json string in cmd arg: {0}".format(_jsonStr))
		_ok = False
		print json.dumps(_default)
	if _ok:
		_str =  getTaskBoundsEx(_obj)
		logger.info("the getTaskBoundsEx return value: {0}".format(_str))
		print _str
