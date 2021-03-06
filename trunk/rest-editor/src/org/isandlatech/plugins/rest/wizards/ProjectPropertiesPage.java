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

package org.isandlatech.plugins.rest.wizards;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.isandlatech.plugins.rest.i18n.Messages;

/**
 * Sphinx project properties
 * 
 * @author Thomas Calmant
 */
public class ProjectPropertiesPage extends AbstractWizardPage {

	/** Project authors */
	private Text pAuthors;

	/** Activate "todo" extension */
	private Button pExtensionTodo;

	/** Generated documents language */
	private Combo pLanguage;

	/** Project sub-version */
	private Text pRelease;

	/** Separate source and build */
	private Button pSeparateSourceBuild;

	/** Generated HTML documents theme */
	private Combo pTheme;

	/** Project version */
	private Text pVersion;

	/**
	 * Sets up the wizard page
	 * 
	 * @param aPageName
	 *            Page name
	 */
	protected ProjectPropertiesPage(final String aPageName) {
		super(aPageName);
		setTitle(Messages.getString("wizard.newproject.pages.project.title"));
		setDescription(Messages
				.getString("wizard.newproject.pages.project.description"));
	}

	@Override
	public boolean canFlipToNextPage() {
		return !getAuthors().isEmpty() && !getVersion().isEmpty();
	}

	@Override
	protected void createFields() {
		pAuthors = addTextField(Messages
				.getString("wizard.newproject.pages.project.authors"));
		pVersion = addTextField(Messages
				.getString("wizard.newproject.pages.project.version"));
		pRelease = addTextField(Messages
				.getString("wizard.newproject.pages.project.release"));

		// TODO use preferences to select the default one
		pLanguage = addComboBox(
				Messages.getString("wizard.newproject.pages.project.language"),
				IConfigConstants.LANGUAGES);
		pLanguage.select(1);

		// TODO use preferences to select the default one
		pTheme = addComboBox(
				Messages.getString("wizard.newproject.pages.project.theme"),
				IConfigConstants.HTML_THEMES);
		pTheme.select(1);

		pSeparateSourceBuild = addCheckBox(Messages
				.getString("wizard.newproject.pages.project.separatedsources"));
		pSeparateSourceBuild.setSelection(true);

		pExtensionTodo = addCheckBox(Messages
				.getString("wizard.newproject.pages.project.ext.todo"));
		pExtensionTodo.setSelection(true);
	}

	/**
	 * @return the authors
	 */
	public String getAuthors() {
		return pAuthors.getText();
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return pLanguage.getText();
	}

	@Override
	protected int getNbColumns() {
		return 2;
	}

	/**
	 * @return the version release
	 */
	public String getRelease() {
		String release = pRelease.getText().trim();

		if (release.isEmpty()) {
			release = getVersion();
		}

		return release;
	}

	/**
	 * @return the HTML theme
	 */
	public String getTheme() {
		return pTheme.getText();
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return pVersion.getText().trim();
	}

	/**
	 * @return True if the "todo::" directive extension is activated
	 */
	public boolean isExtensionTodoActivated() {
		return pExtensionTodo.getSelection();
	}

	/**
	 * @return True if source and build folders must be separated
	 */
	public boolean isSourceBuildSeparated() {
		return pSeparateSourceBuild.getSelection();
	}
}
