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

import org.eclipse.swt.widgets.Display;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Settings
{
	static final String OS_NAME = System.getProperty("os.name").toLowerCase();

	private final Display display;
	private final File file;

	private String version;

	Settings(Display display, String fileName)
	{
		this.display = display;

		version = getClass().getPackage().getImplementationVersion();
		if(version == null)
		{
			version = System.getProperty("braillejanus.version");
			if(version == null)
			{
				Log.message(Log.LOG_WARNING, "unable to determine version");
				version = "development";
			}
		}

		if(fileName == null)
		{
			fileName = System.getProperty("user.home") + File.separator;
			if(OS_NAME.startsWith("windows"))
				fileName += "AppData" + File.separator + "Local" + File.separator + "BrailleJanus.conf";
			else
				fileName += ".braillejanus.conf";
		}
		file = new File(fileName);
	}

	private boolean readLine(String line)
	{
		if(line.isEmpty())
			return true;

		int offset = line.indexOf(' ');
		if(offset < 0)
			return false;

		String value = line.substring(offset + 1);
		if(value.length() < 1)
			return false;

		String tokens[];
		switch(line.substring(0, offset))
		{
		case "version":

			if(version != null)
				if(!version.equals(value))
					Log.message(Log.LOG_WARNING, "Version " + value + " from settings file does not match " + version);
			break;

		default:  return false;
		}

		return true;
	}

	boolean readSettings()
	{
		if(!file.exists())
		{
			Log.message(Log.LOG_WARNING, "Settings file not found:  " + file.getPath());
			return false;
		}

		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String line;
			int lineNumber = 1;
			while((line = reader.readLine()) != null)
			{
				try
				{
					if(!readLine(line))
						Log.message(Log.LOG_ERROR, "Unknown setting, line #" + lineNumber + ":  " + line + " -- " + file.getPath());
				}
				catch(NumberFormatException ignored)
				{
					Log.message(Log.LOG_ERROR, "Bad setting value, line #" + lineNumber + ":  " + line + " -- " + file.getPath());
				}
				finally
				{
					lineNumber++;
				}
			}
		}
		catch(FileNotFoundException exception)
		{
			Log.message(Log.LOG_ERROR, exception);
		}
		catch(IOException exception)
		{
			Log.message(Log.LOG_ERROR, exception);
		}
		finally
		{
			try
			{
				if(reader != null)
					reader.close();
			}
			catch(IOException exception)
			{
				Log.message(Log.LOG_ERROR, exception);
			}
		}

		return true;
	}

	private void writeLines(PrintWriter writer)
	{
		if(version != null)
			writer.println("version " + version);

		writer.println();
	}

	boolean writeSettings()
	{
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(file);
			writeLines(writer);
		}
		catch(FileNotFoundException exception)
		{
			Log.message(Log.LOG_ERROR, exception);
			return false;
		}
		finally
		{
			if(writer != null)
				writer.close();
		}

		return true;
	}
}
