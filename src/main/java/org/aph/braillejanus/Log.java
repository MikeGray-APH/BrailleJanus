/* Copyright (C) 2017 American Printing House for the Blind Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aph.braillejanus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Log
{
	static final int LOG_ALL = 0;
	static final int LOG_TRACE = 1;
	static final int LOG_DEBUG = 2;
	static final int LOG_INFO = 3;
	static final int LOG_WARNING = 4;
	static final int LOG_ERROR = 5;
	static final int LOG_FATAL = 6;

	private static final StringWriter logString = new StringWriter();
	private static final PrintWriter logWriter = new PrintWriter(logString);

	private static Shell shell;

	private Log(){}

	static void setShell(Shell shell)
	{
		Log.shell = shell;
	}

	static String getString()
	{
		return logString.toString();
	}

	static void message(int level, String message, boolean showDialog)
	{
		String string;

		switch(level)
		{
		case LOG_ALL:      string = "ALL:  ";        break;
		case LOG_TRACE:    string = "TRACE:  ";      break;
		case LOG_DEBUG:    string = "DEBUG:  ";      break;
		case LOG_INFO:     string = "INFO:  ";       break;
		case LOG_WARNING:  string = "WARNING:  ";    break;
		case LOG_ERROR:    string = "ERROR:  ";      break;
		case LOG_FATAL:    string = "FATAL:  ";      break;
		default:           string = level + "?:  ";  break;
		}

		if(message != null)
			string += message;
		System.err.println(string);
		System.err.flush();
		logWriter.println(string);
		logWriter.flush();

		if(showDialog && shell != null)
		{
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setMessage(string);
			messageBox.open();
		}
	}

	static void message(int level, Exception exception, boolean showDialog)
	{
		message(level, exception.getMessage(), showDialog);
	}

	static void message(int level, Error error, boolean showDialog)
	{
		message(level, error.getMessage(), showDialog);
	}
}
