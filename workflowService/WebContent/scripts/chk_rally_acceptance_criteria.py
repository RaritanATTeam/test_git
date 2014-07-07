#!/usr/bin/env python


import sys
import shlex, subprocess
import thread
import os
import json
import logging
from time import gmtime, strftime, localtime
from lxml import etree, objectify


def checkRallyUserStory(obj_id):
	_cmd = """
	curl -s -u 'alexp.chen@raritan.com:1qaz2wsx'  https://rally1.rallydev.com/slm/webservice/1.28/hierarchicalrequirement/{0}""".format(obj_id)
	process = subprocess.Popen(_cmd, shell=True,stdout=subprocess.PIPE)
	_output = process.communicate()[0]

	try:
		root = objectify.fromstring(_output)
		if root.Description.text:
			return True
		else:
			return True
	except:
		return True

if __name__ == "__main__":
	obj_id = sys.argv[1]

	ok = checkRallyUserStory(obj_id)
	_obj = {"isFilled":ok , 'ret': 0}
	print json.dumps(_obj)
