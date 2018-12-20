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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class BaseAction implements SelectionListener
{
	protected MenuItem menuItem;
	protected ToolItem toolItem;
	protected boolean enabled;

	void addToMenuAndToolBar(Menu menu, ToolBar toolBar, String tag, int accelerator, String iconFileName, boolean enabled)
	{
		this.enabled = enabled;

		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(tag);
		if(accelerator != 0)
			menuItem.setAccelerator(accelerator);
		menuItem.addSelectionListener(this);
		menuItem.setEnabled(enabled);

		toolItem = new ToolItem(toolBar, SWT.PUSH);
		toolItem.setImage(new Image(toolBar.getParent().getShell().getDisplay(), getClass().getResourceAsStream("/images/" + iconFileName)));
		toolItem.setToolTipText(tag);
		toolItem.addSelectionListener(this);
		toolItem.setEnabled(enabled);
	}

	void addToMenu(Menu menu, String tag, int accelerator, boolean enabled)
	{
		this.enabled = enabled;

		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(tag);
		if(accelerator != 0)
			menuItem.setAccelerator(accelerator);
		menuItem.addSelectionListener(this);
		menuItem.setEnabled(enabled);
	}

	void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		if(menuItem != null)
			menuItem.setEnabled(enabled);
		if(toolItem != null)
			toolItem.setEnabled(enabled);
	}

	boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void widgetSelected(SelectionEvent ignored){}

	@Override
	public void widgetDefaultSelected(SelectionEvent ignored){}
}
