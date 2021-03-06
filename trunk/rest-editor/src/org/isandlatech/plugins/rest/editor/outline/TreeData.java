/*******************************************************************************
 * Copyright (c) 2011 isandlaTech, Thomas Calmant
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thomas Calmant (isandlaTech) - initial API and implementation
 *******************************************************************************/

package org.isandlatech.plugins.rest.editor.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.TreePath;

/**
 * Represents a section node in the document hierarchy
 * 
 * @author Thomas Calmant
 */
public class TreeData implements Comparable<TreeData> {

	/** Next value to be used as node ID */
	private static int sNextId = 0;

	/**
	 * Creates an array of TreeData objects from a list of labels
	 * 
	 * @param aParent
	 *            Parent of the elements (can be null for roots)
	 * @param aLabels
	 *            Labels of the given elements
	 * @return An array of TreeData objects. Never null.
	 */
	public static TreeData[] createElements(final TreeData aParent,
			final String... aLabels) {
		TreeData[] resultArray = new TreeData[aLabels.length];

		if (aLabels != null) {
			int i = 0;
			for (String label : aLabels) {
				resultArray[i++] = new TreeData(aParent, label);
			}
		}

		return resultArray;
	}

	/**
	 * Reset the next ID value to 0
	 */
	public static void resetIdIndex() {
		sNextId = 0;
	}

	/** Element children */
	private List<TreeData> pChildren;

	/** Element document */
	private IDocument pDocument;

	/** Element level */
	private int pLevel;

	/** Element line */
	private int pLine;

	/** Element line offset */
	private int pLineOffset;

	/** Node ID */
	private int pNodeID;

	/** Element parent */
	private TreeData pParent;

	/** Element label */
	private String pText;

	/** Does the element have an upper line decoration ? */
	private boolean pUpperlined;

	/**
	 * Configures the tree element
	 * 
	 * @param aText
	 *            Element label
	 */
	public TreeData(final String aText) {
		this(null, aText);
	}

	/**
	 * Configures the tree element
	 * 
	 * @param aText
	 *            Element label
	 * @param aLine
	 *            Element extra data : line in the source document
	 * @param aLineOffset
	 *            Element extra data : line offset in the source document
	 * @param aUpperline
	 *            Element extra data : is there a line upon the element ?
	 */
	public TreeData(final String aText, final int aLine, final int aLineOffset,
			final boolean aUpperline) {
		this(null, aText, aLine, aLineOffset, aUpperline);
	}

	/**
	 * Configures the tree element
	 * 
	 * @param aParent
	 *            Element parent
	 * @param aText
	 *            Element label
	 */
	public TreeData(final TreeData aParent, final String aText) {
		this(aParent, aText, -1, 0, false);
	}

	/**
	 * Configures the tree element
	 * 
	 * @param aParent
	 *            Element parent
	 * @param aText
	 *            Element label
	 * @param aLine
	 *            Element extra data : line in the source document
	 * @param aLineOffset
	 *            Element extra data : line offset in the source document
	 * @param aUpperline
	 *            Element extra data : is there a line upon the element ?
	 */
	public TreeData(final TreeData aParent, final String aText,
			final int aLine, final int aLineOffset, final boolean aUpperline) {
		pParent = aParent;
		pChildren = new ArrayList<TreeData>();
		pText = String.valueOf(aText);

		if (aParent != null) {
			aParent.addChild(this);
		} else {
			pLevel = 0;
		}

		pLine = aLine;
		pLineOffset = aLineOffset;
		pUpperlined = aUpperline;

		pNodeID = -1;
	}

	/**
	 * Adds a child to the element
	 * 
	 * @param aChild
	 *            A new child
	 */
	public TreeData addChild(final TreeData aChild) {

		if (aChild == null) {
			return null;
		}

		pChildren.add(aChild);
		aChild.pParent = this;
		aChild.pLevel = pLevel + 1;

		aChild.pNodeID = sNextId++;

		return aChild;
	}

	/**
	 * Suppress all children, recursively
	 */
	public void clearChildren() {

		for (TreeData child : pChildren) {
			child.clearChildren();
			child.pParent = null;
		}

		pChildren.clear();
	}

	/**
	 * Compares the given node to the current one. Comparison is only based on
	 * nodes ID.
	 * 
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(final TreeData aOther) {

		if (aOther == null || aOther.pNodeID < pNodeID) {
			return 1;
		}

		if (aOther.pNodeID > pNodeID) {
			return -1;
		}

		return 0;
	}

	/**
	 * Tests if the given object is equal to the current one, based on type,
	 * text and parent equality.
	 * 
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object aObj) {

		if (!(aObj instanceof TreeData)) {
			return false;
		}

		TreeData other = (TreeData) aObj;

		if (!pText.equals(other.pText)) {
			return false;
		}

		if (pParent == null) {
			return other.pParent == null;
		}

		// other.pParent is null if clearChildren() was called before
		if (other.pParent != null && !pParent.equals(other.pParent)) {
			return false;
		}

		return true;
	}

	/**
	 * Tries to find the given node element (based on the
	 * {@link #equals(Object)} method)
	 * 
	 * @param aNode
	 *            Node to look for
	 * @return The found node, null if not present
	 */
	public TreeData find(final TreeData aNode) {

		// Save some loops
		if (aNode == null) {
			return null;
		}

		if (this.equals(aNode)) {
			return this;
		}

		for (TreeData child : pChildren) {

			TreeData result = child.find(aNode);
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	/**
	 * Retrieves the element children in an array
	 * 
	 * @return An array containing all children
	 */
	public TreeData[] getChildrenArray() {
		TreeData[] childrenArray = new TreeData[pChildren.size()];
		pChildren.toArray(childrenArray);
		return childrenArray;
	}

	/**
	 * Retrieves the document associated to the section node
	 * 
	 * @return the document
	 */
	public IDocument getDocument() {
		return pDocument;
	}

	/**
	 * Retrieves the node ID
	 * 
	 * @return the node ID
	 */
	public int getId() {
		return pNodeID;
	}

	/**
	 * Retrieves the element level
	 * 
	 * @return The element level
	 */
	public int getLevel() {
		return pLevel;
	}

	/**
	 * Retrieves the line associated to the element, the 1-based line number of
	 * the title line (or the 0-based line number of the section underline)
	 * 
	 * @return The line associated to the element
	 */
	public int getLine() {
		return pLine;
	}

	/**
	 * Retrieves the offset of the beginning of the title line associated to the
	 * element
	 * 
	 * @return The line offset associated to the element
	 */
	public int getLineOffset() {
		return pLineOffset;
	}

	/**
	 * Gets the following tree node at the same or upper level
	 * 
	 * @return The following node, null if none
	 */
	public TreeData getNext() {

		// No parent, no brother
		if (pParent == null || pLevel < 0) {
			return null;
		}

		List<TreeData> brotherHood = pParent.pChildren;

		// Find this node index
		int thisIndex = brotherHood.indexOf(this);

		if (thisIndex == -1 || thisIndex == brotherHood.size() - 1) {
			// This node is the last one
			return pParent.getNext();
		}

		return brotherHood.get(thisIndex + 1);
	}

	/**
	 * Retrieves the element parent.
	 * 
	 * @return The element parent. Null if the element is a root
	 */
	public TreeData getParent() {
		return pParent;
	}

	/**
	 * Gets the preceding tree node at the same or upper level
	 * 
	 * @return The preceding node, null if none
	 */
	public TreeData getPrevious() {

		// No parent, no brother
		if (pParent == null || pLevel < 0) {
			return null;
		}

		List<TreeData> brotherHood = pParent.pChildren;

		// Find this node index
		int thisIndex = brotherHood.indexOf(this);

		if (thisIndex <= 0) {
			// This node is the first one
			return pParent.getNext();
		}

		return brotherHood.get(thisIndex - 1);
	}

	/**
	 * Retrieves the element label
	 * 
	 * @return The element label
	 */
	public String getText() {
		return pText;
	}

	/**
	 * Converts the current node into a JFace {@link TreePath}
	 * 
	 * @return The JFace tree path
	 */
	public TreePath getTreePath() {

		Object[] segments = new Object[pLevel + 1];
		TreeData current = this;
		int i = pLevel;

		while (current != null) {
			segments[i--] = current;
			current = current.pParent;
		}

		return new TreePath(segments);
	}

	/**
	 * Tests if the current element has children
	 * 
	 * @return True if the element has children
	 */
	public boolean hasChildren() {
		return !pChildren.isEmpty();
	}

	/**
	 * TreeData hash code, based on title, line and parent.
	 */
	@Override
	public int hashCode() {

		StringBuilder builder = new StringBuilder();
		builder.append(pText);
		builder.append(pLine);
		builder.append(pParent);

		return builder.toString().hashCode();
	}

	/**
	 * Returns true if the element has an upper line decoration
	 * 
	 * @return if the element has an upper line decoration
	 */
	public boolean isUpperlined() {
		return pUpperlined;
	}

	/**
	 * Removes a child from the element
	 * 
	 * @param aChild
	 *            The child to be removed
	 */
	public void removeChild(final TreeData aChild) {

		if (aChild == null) {
			return;
		}

		pChildren.remove(aChild);
		aChild.pParent = null;
		aChild.pLevel = 0;
	}

	/**
	 * @param aDocument
	 *            the document to set
	 */
	public void setDocument(final IDocument aDocument) {
		pDocument = aDocument;
	}

	/**
	 * Sets the element parent.
	 * 
	 * @param parent
	 *            The element parent. Null to make a root
	 */
	public void setParent(final TreeData parent) {
		pParent = parent;
	}

	/** Sets the element label */
	public void setText(final String text) {
		pText = String.valueOf(text);
	}

	/**
	 * @return A simple description of the node that can be used as a label
	 */
	@Override
	public String toString() {
		if (pLine != -1) {
			return pText + " (" + pLine + ")";
		}

		return pText;
	}
}
