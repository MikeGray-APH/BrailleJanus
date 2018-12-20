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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class TextEditors
{
	private final Shell parentShell;
	private final StyledText plainText, brailleText;
	private final TextBrailleContent textBrailleContent;

	TextEditors(Shell parentShell)
	{
		this.parentShell = parentShell;

		textBrailleContent = new TextBrailleContent();

		Composite composite = new Composite(parentShell, 0);
		composite.setLayout(new GridLayout(2, true));
		composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));

		plainText = new StyledText(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		plainText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
		plainText.setContent(textBrailleContent.getTextContent());

		brailleText = new StyledText(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		brailleText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
		brailleText.setContent(textBrailleContent.getBrailleContent());
		brailleText.setFont(new Font(parentShell.getDisplay(), "APH_Braille_Font-6s", 18, SWT.NORMAL));
		BrailleKeyHandler brailleKeyHandler = new BrailleKeyHandler();
		brailleText.addKeyListener(brailleKeyHandler);
		brailleText.addVerifyKeyListener(brailleKeyHandler);
	}

	private class BrailleKeyHandler implements KeyListener, VerifyKeyListener
	{
		private char dotState, dotChar = 0x2800;

		@Override
		public void keyPressed(KeyEvent event)
		{
			switch(event.character)
			{
			case 'f':

				dotState |= 0x01;
				dotChar |= 0x01;
				break;

			case 'd':

				dotState |= 0x02;
				dotChar |= 0x02;
				break;

			case 's':

				dotState |= 0x04;
				dotChar |= 0x04;
				break;

			case 'a':

				dotState |= 0x40;
				dotChar |= 0x40;
				break;

			case 'j':

				dotState |= 0x08;
				dotChar |= 0x08;
				break;

			case 'k':

				dotState |= 0x10;
				dotChar |= 0x10;
				break;

			case 'l':

				dotState |= 0x20;
				dotChar |= 0x20;
				break;

			case ';':

				dotState |= 0x80;
				dotChar |= 0x80;
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent event)
		{
			if(Settings.OS_NAME.startsWith("windows"))
				dotState = 0;
			else switch(event.character)
			{
			case 'f':

				dotState &= ~0x01;
				break;

			case 'd':

				dotState &= ~0x02;
				break;

			case 's':

				dotState &= ~0x04;
				break;

			case 'a':

				dotState &= ~0x40;
				break;

			case 'j':

				dotState &= ~0x08;
				break;

			case 'k':

				dotState &= ~0x10;
				break;

			case 'l':

				dotState &= ~0x20;
				break;

			case ';':

				dotState &= ~0x80;
				break;
			}

			//   insert resulting braille character
			if(dotState == 0 && (dotChar & 0xff) != 0)
			{
				brailleText.insert(Character.toString(dotChar));
				brailleText.setCaretOffset(brailleText.getCaretOffset() + 1);
				dotChar = 0x2800;
			}
		}

		@Override
		public void verifyKey(VerifyEvent event)
		{
			//   check if using braille entry
			if(event.character > ' ' && event.character < 0x7f)
				event.doit = false;
		}
	}
}
