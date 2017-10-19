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
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;

import java.util.ArrayList;
import java.util.List;

public class TextBrailleContent
{
	private static final String LINE_DELIMITER = System.getProperty("line.separator");

	@SuppressWarnings("ThisEscapedInObjectConstruction")
	private final TextContent textContent = new TextContent(this);

	@SuppressWarnings("ThisEscapedInObjectConstruction")
	private final BrailleContent brailleContent = new BrailleContent(this);

	private final List<TextChangeListener> textChangeListeners = new ArrayList<>(3);
	private final List<TextChangeListener> brailleChangeListeners = new ArrayList<>(3);

	private final ArrayList<Node> textNodes = new ArrayList<>(100);
	private final ArrayList<Node> brailleNodes = new ArrayList<>(100);

	public TextBrailleContent()
	{
		//   must be at least one always
		textNodes.add(new Node());
		brailleNodes.add(new Node());
	}

	////////////////////////////////////////

	public StyledTextContent getTextContent()
	{
		return textContent;
	}

	public StyledTextContent getBrailleContent()
	{
		return brailleContent;
	}

	////////////////////////////////////////

	private static class Node
	{
		StringBuilder text = new StringBuilder("");
		int offset;
		volatile boolean update, deleted;

		Node(){}

		Node(String text)
		{
			this.text = new StringBuilder(text);
		}

		public String toString()
		{
			StringBuilder stringBuilder = new StringBuilder(" " + text);
			if(deleted)
				stringBuilder.insert(0, '+');
			else
				stringBuilder.insert(0, '-');
			if(update)
				stringBuilder.insert(0, '+');
			else
				stringBuilder.insert(0, '-');
			stringBuilder.insert(0, "[" + offset + ']');
			return stringBuilder.toString();
		}
	}

	private void nodesResetOffsetsAt(ArrayList<Node> nodes, int index)
	{
		if(index < 0)
			index = 0;
		Node crs = nodes.get(index);
		crs.update = crs.deleted = false;
		for(int i = index + 1; i < nodes.size(); i++)
		{
			Node nxt = nodes.get(i);
			nxt.offset = crs.offset + crs.text.length();
			nxt.update = nxt.deleted = false;
			crs = nxt;
		}
	}

	private void nodesDelete(ArrayList<Node> nodes, ArrayList<Node> others, int start, int length)
	{
		if(length <= 0)
			return;

		int indexStart = getLineAtOffset(nodes, start);
		int indexFinish = getLineAtOffset(nodes, start + length);

		//   all in one line
		if(indexStart == indexFinish)
		{
			Node node = nodes.get(indexStart);
			start -= node.offset;
			node.text.delete(start, start + length);
			nodesResetOffsetsAt(nodes, indexStart);

			if(others != null)
			{
				node = others.get(indexStart);
				node.update = true;
			}

			return;
		}

		//   remove in-between textNodes
		for(int i = indexStart + 1; i < indexFinish; i++)
		{
			Node node = nodes.get(indexStart + 1);
			length -= node.text.length();
			nodes.remove(indexStart + 1);

			if(others != null)
			{
				node = others.get(i);
				node.deleted = true;
			}
		}

		Node nodeStart = nodes.get(indexStart);
		Node nodeFinish = nodes.get(indexStart + 1);
		int offsetStart = start - nodeStart.offset;
		int offsetFinish = length - (nodeStart.text.length() - offsetStart);

		nodeStart.text.delete(offsetStart, nodeStart.text.length());
		nodeStart.text.append(nodeFinish.text.substring(offsetFinish, nodeFinish.text.length()));
		nodes.remove(indexStart + 1);
		nodesResetOffsetsAt(nodes, indexStart - 1);

		if(others != null)
		{
			Node node = others.get(indexStart);
			node.update = true;
			node = others.get(indexFinish);
			node.deleted = true;
		}
	}

	private void nodesInsert(ArrayList<Node> nodes, ArrayList<Node> others, StringBuilder lines[], String text, int offset)
	{
		if(text.length() < 1)
			return;

		int index = getLineAtOffset(nodes, offset);
		Node node = nodes.get(index);
		offset -= node.offset;
		String after = node.text.substring(offset);

		if(lines == null)
		{
			node.text.insert(offset, text);

			if(others != null)
			{
				node = others.get(index);
				node.update = true;
			}
		}
		else
		{
			//   first line
			node.text.delete(offset, node.text.length());
			node.text.append(lines[0]);
			if(others != null)
			{
				node = others.get(index);
				node.update = true;
			}

			//   last line
			int indexLast = lines.length - 1;
			if(lines[lines.length - 1].indexOf(LINE_DELIMITER) < 0)
			{
				after = lines[lines.length - 1] + after;
				indexLast = lines.length - 2;
			}

			if(index + 1 < nodes.size() && !after.contains(LINE_DELIMITER))
			{
				node = nodes.get(index + 1);
				node.text.insert(0, after);
			}
			else
				nodes.add(index + 1, new Node(after));

			for(int i = indexLast; i > 0; i--)
				nodes.add(index + 1, new Node(lines[i].toString()));
		}

		nodesResetOffsetsAt(nodes, index - 1);
	}

	////////////////////////////////////////

	private static class TextRange
	{
		int textOffset, textLength;
		int indexStart, indexCount;

		TextRange(int textOffset, int textLength, int indexStart, int indexCount)
		{
			this.textOffset = textOffset;
			this.textLength = textLength;
			this.indexStart = indexStart;
			this.indexCount = indexCount;
		}
	}

	////////////////////////////////////////

	private static class TextContent implements StyledTextContent
	{
		private final TextBrailleContent textBrailleContent;

		TextContent(TextBrailleContent textBrailleContent)
		{
			this.textBrailleContent = textBrailleContent;
		}

		@Override
		public void addTextChangeListener(TextChangeListener listener)
		{
			textBrailleContent.addTextChangeListener(listener);
		}

		@Override
		public void removeTextChangeListener(TextChangeListener listener)
		{
			textBrailleContent.removeTextChangeListener(listener);
		}

		@Override
		public int getCharCount()
		{
			return textBrailleContent.getCharCount(textBrailleContent.textNodes);
		}

		@Override
		public int getLineCount()
		{
			return textBrailleContent.getLineCount(textBrailleContent.textNodes);
		}

		@Override
		public String getLineDelimiter()
		{
			return textBrailleContent.getLineDelimiter();
		}

		@Override
		public String getLine(int lineIndex)
		{
			return textBrailleContent.getLine(textBrailleContent.textNodes, lineIndex);
		}

		@Override
		public int getLineAtOffset(int offset)
		{
			return textBrailleContent.getLineAtOffset(textBrailleContent.textNodes, offset);
		}

		@Override
		public int getOffsetAtLine(int lineIndex)
		{
			return textBrailleContent.getOffsetAtLine(textBrailleContent.textNodes, lineIndex);
		}

		@Override
		public String getTextRange(int start, int length)
		{
			return textBrailleContent.getTextRange(textBrailleContent.textNodes, start, length);
		}

		@Override
		public void replaceTextRange(int start, int replaceLength, String text)
		{
			TextRange textRanges[] = textBrailleContent.replaceTextRange(textBrailleContent.textChangeListeners, textBrailleContent.textContent, textBrailleContent.textNodes, textBrailleContent.brailleNodes, start, replaceLength, text);
			textBrailleContent.updateForward(textRanges);
		}

		@Override
		public void setText(String text)
		{
			textBrailleContent.setText(textBrailleContent.textChangeListeners, textBrailleContent.textContent, textBrailleContent.textNodes, textBrailleContent.brailleNodes, text);
		}
	}

	////////////////////////////////////////

	private static class BrailleContent implements StyledTextContent
	{
		private final TextBrailleContent textBrailleContent;

		BrailleContent(TextBrailleContent textBrailleContent)
		{
			this.textBrailleContent = textBrailleContent;
		}

		@Override
		public void addTextChangeListener(TextChangeListener listener)
		{
			textBrailleContent.addBrailleChangeListener(listener);
		}

		@Override
		public void removeTextChangeListener(TextChangeListener listener)
		{
			textBrailleContent.removeBrailleChangeListener(listener);
		}

		@Override
		public int getCharCount()
		{
			return textBrailleContent.getCharCount(textBrailleContent.brailleNodes);
		}

		@Override
		public int getLineCount()
		{
			return textBrailleContent.getLineCount(textBrailleContent.brailleNodes);
		}

		@Override
		public String getLineDelimiter()
		{
			return textBrailleContent.getLineDelimiter();
		}

		@Override
		public String getLine(int lineIndex)
		{
			return textBrailleContent.getLine(textBrailleContent.brailleNodes, lineIndex);
		}

		@Override
		public int getLineAtOffset(int offset)
		{
			return textBrailleContent.getLineAtOffset(textBrailleContent.brailleNodes, offset);
		}

		@Override
		public int getOffsetAtLine(int lineIndex)
		{
			return textBrailleContent.getOffsetAtLine(textBrailleContent.brailleNodes, lineIndex);
		}

		@Override
		public String getTextRange(int start, int length)
		{
			return textBrailleContent.getTextRange(textBrailleContent.brailleNodes, start, length);
		}

		@Override
		public void replaceTextRange(int start, int replaceLength, String text)
		{
			TextRange textRanges[] = textBrailleContent.replaceTextRange(textBrailleContent.brailleChangeListeners, textBrailleContent.brailleContent, textBrailleContent.brailleNodes, textBrailleContent.textNodes, start, replaceLength, text);
			textBrailleContent.updateBackward(textRanges);
		}

		@Override
		public void setText(String text)
		{
			textBrailleContent.setText(textBrailleContent.brailleChangeListeners, textBrailleContent.brailleContent, textBrailleContent.brailleNodes, textBrailleContent.textNodes, text);
		}
	}

	////////////////////////////////////////

	private static StringBuilder[] linesSplit(String text)
	{
		ArrayList<StringBuilder> lines = new ArrayList<>(3);
		int crs = 0;
		int end = text.indexOf(LINE_DELIMITER, crs);
		while(end > -1)
		{
			end += LINE_DELIMITER.length();
			lines.add(new StringBuilder(text.substring(crs, end)));
			crs = end;
			end = text.indexOf(LINE_DELIMITER, crs);
		}

		//   not ending with a line delimiter
		if(crs > 0 && crs < text.length())
			lines.add(new StringBuilder(text.substring(crs)));

		if(lines.isEmpty())
			return null;
		else
			return lines.toArray(new StringBuilder[lines.size()]);
	}

	////////////////////////////////////////

	private void addTextChangeListener(TextChangeListener listener)
	{
		textChangeListeners.add(listener);
	}

	private void addBrailleChangeListener(TextChangeListener listener)
	{
		brailleChangeListeners.add(listener);
	}

	private void removeTextChangeListener(TextChangeListener listener)
	{
		textChangeListeners.remove(listener);
	}

	private void removeBrailleChangeListener(TextChangeListener listener)
	{
		brailleChangeListeners.remove(listener);
	}

	////////////////////////////////////////

	private int getCharCount(ArrayList<Node> nodes)
	{
		Node node = nodes.get(nodes.size() - 1);
		return node.offset + node.text.length();
	}

	private int getLineCount(ArrayList<Node> nodes)
	{
		int count = nodes.size();
		if(count <= 0)
			return 1;
		else
			return count;
	}

	private String getLineDelimiter()
	{
		return LINE_DELIMITER;
	}

	private String getLine(ArrayList<Node> nodes, int lineIndex)
	{
		if(lineIndex >= nodes.size())
			return "";
		String line = nodes.get(lineIndex).text.toString();
		int index = line.indexOf(LINE_DELIMITER);
		if(index < 0)
			return line;
		else
			return line.substring(0, index);
	}

	private int getLineAtOffset(ArrayList<Node> nodes, int offset)
	{
		int cnt = 0;
		for(Node node : nodes)
			if(node.offset <= offset && offset < node.offset + node.text.length())
				break;
			else if(node.text.indexOf(LINE_DELIMITER) >= 0)
				cnt++;
		return cnt;
	}

	private int getOffsetAtLine(ArrayList<Node> nodes, int lineIndex)
	{
		if(lineIndex >= nodes.size())
			return getCharCount(nodes);
		return nodes.get(lineIndex).offset;
	}

	private String getTextRange(ArrayList<Node> nodes, int start, int length)
	{
		if(length <= 0)
			return "";

		int startIndex = getLineAtOffset(nodes, start);
		int finishIndex = getLineAtOffset(nodes, start + length);
		Node node = nodes.get(startIndex);

		//   all in one line
		if(startIndex == finishIndex)
			return node.text.toString();

		StringBuilder text = new StringBuilder(100);

		//   first node
		start -= node.offset;
		length -= node.text.length() - start;
		text.append(node.text.substring(start));

		//   in-between textNodes
		for(int i = startIndex + 1; i < finishIndex; i++)
		{
			node = nodes.get(i);
			length -= node.text.length();
			text.append(node.text);
		}

		//   last node
		node = nodes.get(finishIndex);
		text.append(node.text.substring(0, length));

		return text.toString();
	}

	private TextRange[] replaceTextRange(List<TextChangeListener> listeners, StyledTextContent content, ArrayList<Node> nodes, ArrayList<Node> others, int start, int length, String text)
	{
		int startIndex = getLineAtOffset(nodes, start);

		StringBuilder lines[] = linesSplit(text);

		int newLineCount = 0;
		if(lines != null)
		{
			newLineCount = lines.length;
			if(lines[lines.length - 1].indexOf(LINE_DELIMITER) < 0)
				newLineCount--;
		}

		TextRange nodesChange = new TextRange(start, text.length(), startIndex, newLineCount);

		int replaceLineCount = getLineAtOffset(nodes, start + length) - getLineAtOffset(nodes, start);

		TextRange othersChange = null;
		if(others != null)
		{
			Node nodeStart = others.get(startIndex);
			Node nodeFinish = others.get(startIndex + replaceLineCount);
			othersChange = new TextRange(nodeStart.offset, (nodeFinish.offset - nodeStart.offset) + nodeFinish.text.length(), startIndex, startIndex + replaceLineCount);
		}

		TextChangingEvent textChangingEvent = new TextChangingEvent(content);
		textChangingEvent.newText = text;
		textChangingEvent.newCharCount = text.length();
		textChangingEvent.newLineCount = newLineCount;
		textChangingEvent.start = start;
		textChangingEvent.replaceLineCount = replaceLineCount;
		textChangingEvent.replaceCharCount = length;
		for(TextChangeListener listener : listeners)
			listener.textChanging(textChangingEvent);

		nodesDelete(nodes, others, start, length);
		nodesInsert(nodes, others, lines, text, start);

		TextChangedEvent textChangedEvent = new TextChangedEvent(content);
		for(TextChangeListener listener : listeners)
			listener.textChanged(textChangedEvent);

		return new TextRange[]{ nodesChange,othersChange };
	}

	private void setText(List<TextChangeListener> listeners, StyledTextContent content, ArrayList<Node> nodes, ArrayList<Node> others, String text)
	{
		replaceTextRange(listeners, content, nodes, others, 0, getCharCount(nodes), text);
	}

	////////////////////////////////////////

	void updateForward(TextRange textRanges[])
	{
		if(textRanges[1] == null)
			return;
		Node node = brailleNodes.get(textRanges[0].indexStart);
		if(!(node.update || node.deleted))
			return;

		int indexStart = textRanges[0].indexStart;
		int indexFinish = textRanges[0].indexStart + textRanges[0].indexCount;
		Node nodeStart = textNodes.get(indexStart);
		Node nodeFinish = textNodes.get(indexFinish);
		int length = (nodeFinish.offset - nodeStart.offset) + nodeFinish.text.length();

		StringBuilder braille = new StringBuilder(length * 3);
		for(int i = indexStart; i <= indexFinish; i++)
		{
			node = textNodes.get(i);

//			StringBuilder line = new StringBuilder(node.text);
//			String nl = "";
//			int index = line.indexOf(LINE_DELIMITER);
//			while(index >= 0)
//			{
//				nl = LINE_DELIMITER;
//				line.deleteCharAt(index);
//				index = line.indexOf(LINE_DELIMITER, index);
//			}
//			if(line.length() == 0)
//			{
//				if(nl.length() > 0)
//					braille.append(nl);
//				continue;
//			}
//			String result = LibLouisAPH.translateForward(line.toString(), line.length() * 5, "english-ueb-grade2.rst", null, null, null, null);
//			if(result == null)
//				braille.append("ERROR" + nl);
//			else
//				braille.append(result + nl);

			if(node.text.length() == 0)
				continue;
			if(node.text.toString().equals(LINE_DELIMITER))
			{
				braille.append(LINE_DELIMITER);
				continue;
			}
			length = node.text.length() * 5;
			if(length < 0x100)
				length = 0x100;
			String result = LibLouisAPH.translateForward(node.text.toString(), length, "english-ueb-grade2.rst", null, null, null, null);
			if(result == null)
			{
				if(node.text.lastIndexOf(LINE_DELIMITER) >= 0)
					braille.append(LINE_DELIMITER);
			}
			else
				braille.append(result);
		}

		replaceTextRange(brailleChangeListeners, brailleContent, brailleNodes, null, textRanges[1].textOffset, textRanges[1].textLength, braille.toString());
	}

	void updateBackward(TextRange textRanges[])
	{
		if(textRanges[1] == null)
			return;
		Node node = textNodes.get(textRanges[0].indexStart);
		if(!(node.update || node.deleted))
			return;

		int indexStart = textRanges[0].indexStart;
		int indexFinish = textRanges[0].indexStart + textRanges[0].indexCount;
		Node nodeStart = brailleNodes.get(indexStart);
		Node nodeFinish = brailleNodes.get(indexFinish);
		int length = (nodeFinish.offset - nodeStart.offset) + nodeFinish.text.length();

		StringBuilder text = new StringBuilder(length * 3);
		for(int i = indexStart; i <= indexFinish; i++)
		{
			node = brailleNodes.get(i);
			if(node.text.length() == 0)
				continue;
			if(node.text.toString().equals(LINE_DELIMITER))
			{
				text.append(LINE_DELIMITER);
				continue;
			}
			length = node.text.length() * 5;
			if(length < 0x100)
				length = 0x100;
			String result = LibLouisAPH.translateBackward(node.text.toString(), length, "english-ueb-grade2.rst", null, null, null, null);
			if(result == null)
			{
				if(node.text.lastIndexOf(LINE_DELIMITER) >= 0)
					text.append(LINE_DELIMITER);
			}
			else
				text.append(result);
		}

		replaceTextRange(textChangeListeners, textContent, textNodes, null, textRanges[1].textOffset, textRanges[1].textLength, text.toString());
	}
}
