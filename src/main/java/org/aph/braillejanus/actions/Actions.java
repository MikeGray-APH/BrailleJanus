/* Copyright (C) 2018 American Printing House for the Blind Inc.
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

package org.aph.braillejanus.actions;

import org.aph.braillejanus.Settings;
import org.aph.braillejanus.TextEditors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

public class Actions
{
	private final Shell parentShell;
	private final Settings settings;
	private final TextEditors textEditors;

	public Actions(Shell parentShell, Settings settings, TextEditors textEditors)
	{
		this.parentShell = parentShell;
		this.settings = settings;
		this.textEditors = textEditors;


		Menu menuBar = new Menu(parentShell, SWT.BAR);
		parentShell.setMenuBar(menuBar);

//		ToolBar toolBar = new ToolBar(parentShell, SWT.HORIZONTAL | SWT.FLAT);
//		toolBar.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
//		toolBar.moveAbove(null);

		Menu menu;
		MenuItem item;

		String mod1KeyName = "Ctrl+";
		String mod2KeyName = "Shift+";
		if(Settings.OS_NAME.startsWith("mac"))
		{
			mod1KeyName = "\u2318";
			mod2KeyName = "\u21e7";
		}


		//   about menu
		menu = new Menu(menuBar);
		item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("Help");
		item.setMenu(menu);

		new AboutHandler(parentShell, settings).addToMenu(menu, "About", 0, true);

	}
}
