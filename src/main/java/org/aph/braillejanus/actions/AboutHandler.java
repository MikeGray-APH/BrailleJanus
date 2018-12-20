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
import org.aph.liblouisaph.LibLouisAPH;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class AboutHandler extends BaseAction
{
	private final Settings settings;
	private final Shell modalShell;

	public AboutHandler(Shell modalShell, Settings settings)
	{
		this.modalShell = modalShell;
		this.settings = settings;
	}

	@Override
	public void widgetSelected(SelectionEvent ignored)
	{
		new AboutDialog();
	}

	private final class AboutDialog
	{
		private AboutDialog()
		{
			Shell dialog = new Shell(modalShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
			dialog.setLayout(new GridLayout(1, true));
			dialog.setText("About BrailleJanus");

			String versionString = settings.getVersion();
			if(versionString == null)
				versionString = "dev";

			Label label;

			//TODO:  add image
			//Image image = new Image(modalShell.getDisplay(), getClass().getResourceAsStream("/images/?????.png"));
			//label = new Label(dialog, SWT.CENTER);
			//label.setLayoutData(new GridData(GridData.FILL_BOTH));
			//label.setImage(image);

			//new Label(dialog, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_BOTH));

			Font font = new Font(modalShell.getDisplay(), "Sans", 10, SWT.NORMAL);

			label = new Label(dialog, SWT.LEFT);
			label.setLayoutData(new GridData(GridData.FILL_BOTH));
			label.setFont(font);
			label.setText("Copyright \u00a9 2018 American Printing House for the Blind Inc.");

			label = new Label(dialog, SWT.LEFT);
			label.setLayoutData(new GridData(GridData.FILL_BOTH));
			label.setFont(font);
			label.setText("version:  " + versionString);

			new Label(dialog, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_BOTH));

			label = new Label(dialog, SWT.LEFT);
			label.setLayoutData(new GridData(GridData.FILL_BOTH));
			label.setFont(font);
			label.setText("LibLouisAPH");

			label = new Label(dialog, SWT.LEFT);
			label.setLayoutData(new GridData(GridData.FILL_BOTH));
			label.setFont(font);
			String liblouisAPHVersion = LibLouisAPH.getVersion();
			String liblouisAPHImplementationVersion = LibLouisAPH.getImplementationVersion();
			if(!liblouisAPHVersion.equals(liblouisAPHImplementationVersion))
				liblouisAPHVersion += " (" + liblouisAPHImplementationVersion + ")";
			label.setText("version:  " + liblouisAPHVersion);

			dialog.pack();
			dialog.open();
			while(!dialog.isDisposed())
				if(!dialog.getDisplay().readAndDispatch())
					dialog.getDisplay().sleep();
		}
	}
}
