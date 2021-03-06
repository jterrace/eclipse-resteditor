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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.isandlatech.plugins.rest.i18n.Messages;

/**
 * Refresh action for the outline page
 * 
 * @author Thomas Calmant
 */
public class RefreshOutlineAction extends Action {

	/** The associated outline page */
	private RestContentOutlinePage pOutline;

	/**
	 * Creates the refresh action
	 * 
	 * @param aParentPage
	 *            Parent outline page
	 */
	public RefreshOutlineAction(final RestContentOutlinePage aParentPage) {
		pOutline = aParentPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getText()
	 */
	@Override
	public String getText() {
		return Messages.getString("outline.refresh");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {

		TreeSelection previousSelection = (TreeSelection) pOutline
				.getSelection();

		// Refresh tree content (new objects)
		pOutline.update();

		// Reset the tree selection
		OutlineUtil.postUpdateSelection(pOutline, previousSelection);
	}
}
