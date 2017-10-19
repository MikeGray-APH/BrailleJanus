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

import org.aph.liblouisaph.LibLouisAPH;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * Contains the main method.
 * </p>
 */
public final class Main
{
	private final Display display;
	private final Shell shell;
	private final Settings settings;

	public static void main(String args[])
	{
		new Main(args);
	}

	private Main(String args[])
	{
		//   must be before display is created (on Macs at least)
		Display.setAppName("BrailleJanus");

		display = Display.getDefault();

		//   load fonts
		loadFont("APH_Braille_Font-6.otf");
		loadFont("APH_Braille_Font-6b.otf");
		loadFont("APH_Braille_Font-6s.otf");
		loadFont("APH_Braille_Font-6sb.otf");
		loadFont("APH_Braille_Font-8.otf");
		loadFont("APH_Braille_Font-8b.otf");
		loadFont("APH_Braille_Font-8s.otf");
		loadFont("APH_Braille_Font-8sb.otf");
		loadFont("APH_Braille_Font-8w.otf");
		loadFont("APH_Braille_Font-8wb.otf");
		loadFont("APH_Braille_Font-8ws.otf");
		loadFont("APH_Braille_Font-8wsb.otf");

		settings = new Settings(display, null);
		settings.readSettings();

		//   needed to catch Quit (Command-Q) on Macs
		display.addListener(SWT.Close, new CloseHandler());

		shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("BrailleJanus");
		shell.addShellListener(new ShellHandler());

		new TextEditors(shell);

		try
		{
			LibLouisAPH.loadLibrary();
		}
		catch(IOException exception)
		{
			Log.message(Log.LOG_ERROR, exception);
			return;
		}

		shell.setSize(640, 480);
		shell.open();
		while(!shell.isDisposed())
		if(!display.readAndDispatch())
			display.sleep();

		display.dispose();
	}

	private boolean checkClosing()
	{
		boolean doit = true;

		//   write settings file
		//if(doit)
			if(!settings.writeSettings())
			{
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
				messageBox.setMessage("Would you like to exit anyway?");
				doit = messageBox.open() == SWT.YES;
			}

		return doit;
	}

	/**
	 * <p>
	 * Needed to catch Quit (Command-Q) on Macs
	 * </p>
	 */
	private class CloseHandler implements Listener
	{
		@Override
		public void handleEvent(Event event)
		{
			event.doit = checkClosing();
		}
	}

	private class ShellHandler extends ShellAdapter
	{
		@Override
		public void shellClosed(ShellEvent event)
		{
			event.doit = checkClosing();
		}
	}

	private void loadFont(String fontFileName)
	{
		InputStream fontInputStream = getClass().getResourceAsStream("/fonts/" + fontFileName);
		if(fontInputStream == null)
		{
			Log.message(Log.LOG_WARNING, "Unable to open font resource:  " + fontFileName);
			return;
		}

		FileOutputStream fontOutputStream = null;
		try
		{
			File fontFile = new File(System.getProperty("java.io.tmpdir") + File.separator + fontFileName);
			fontOutputStream = new FileOutputStream(fontFile);

			byte buffer[] = new byte[0x1000];
			int length;
			while((length = fontInputStream.read(buffer)) > 0)
				fontOutputStream.write(buffer, 0, length);

			display.loadFont(fontFile.getPath());
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
				fontInputStream.close();
				if(fontOutputStream != null)
					fontOutputStream.close();
			}
			catch(IOException exception)
			{
				Log.message(Log.LOG_ERROR, exception);
			}
		}
	}
}
