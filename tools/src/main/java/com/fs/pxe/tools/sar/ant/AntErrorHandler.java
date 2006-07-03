/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.sar.ant;

import org.apache.tools.ant.Task;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class AntErrorHandler implements ErrorHandler {

	private Task _task;
	private String _file;
	private int _errors;
	private int _warnings;

	AntErrorHandler(Task task, String file) {
		_task = task;
		_file = file;
	}
	
	int getErrors() { return _errors; }
	int getWarnings() { return _warnings; }
	
	public void warning(SAXParseException exception) throws SAXException {
		_task.log("WARN: " + _file + exception.getLineNumber() + ":" + exception.getMessage());
		++_warnings;
	}

	public void error(SAXParseException exception) throws SAXException {
		_task.log("ERROR: " + _file + exception.getLineNumber() + ":" + exception.getMessage());
		++_errors;
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		_task.log("ERROR: " + _file + exception.getLineNumber() + ":" + exception.getMessage());
		++_errors;

	}

}
