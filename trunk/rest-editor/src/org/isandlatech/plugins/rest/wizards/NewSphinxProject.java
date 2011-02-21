/**
 * File:   NewSphinxProject.java
 * Author: Thomas Calmant
 * Date:   17 févr. 2011
 */
package org.isandlatech.plugins.rest.wizards;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.isandlatech.plugins.rest.RestPlugin;

/**
 * Sphinx project creation wizard
 * 
 * @author Thomas Calmant
 */
public class NewSphinxProject extends BasicNewProjectResourceWizard {

	/** Advanced project properties */
	private ProjectAdvancedPropertiesPage pAdvancedPropertiesPage;

	/** Project main properties */
	private ProjectPropertiesPage pPropertiesPage;

	/**
	 * Sets the window title
	 */
	public NewSphinxProject() {
		super();
		setWindowTitle("New Sphinx documentation project");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard#addPages
	 * ()
	 */
	@Override
	public void addPages() {
		super.addPages();

		IWizardPage startingPage = getStartingPage();
		startingPage.setTitle("Sphinx project");
		startingPage
				.setDescription("Creates a Sphinx documentation project from scratch");

		pPropertiesPage = new ProjectPropertiesPage("Project properties");
		pAdvancedPropertiesPage = new ProjectAdvancedPropertiesPage(
				"Advanced project properties");

		addPage(pPropertiesPage);
		addPage(pAdvancedPropertiesPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	@Override
	public boolean canFinish() {
		return pPropertiesPage.canFlipToNextPage();
	}

	/**
	 * Generates the index.rst file content, based on the sphinx-quickstart
	 * result
	 * 
	 * @param aProjectName
	 *            The project name
	 * @return The master document content
	 */
	protected String generateMasterDocumentContent(final String aProjectName) {

		StringBuilder builder = new StringBuilder();
		String longProjectName = "Welcome to " + aProjectName
				+ "'s Documentation";

		char[] decorationArray = new char[longProjectName.length()];
		Arrays.fill(decorationArray, '#');

		// Main comment
		builder.append(".. " + aProjectName + " Documentation master file.\n\n");

		// Project title section
		builder.append(decorationArray).append("\n").append(longProjectName)
				.append("\n").append(decorationArray);

		// Summary directive
		builder.append("\n\n.. :toctree::\n   :maxdepth: 2\n   \n   \n\n");

		// Indices and tables
		builder.append("Indices and tables\n" + "==================\n\n"
				+ "* :ref:`genindex`\n" + "* :ref:`search`\n");

		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {

		// Create the project
		if (!super.performFinish()) {
			return false;
		}

		// Retrieve the project name
		String projectName = super.getNewProject().getName();

		// Prepare the configuration
		ConfigGenerator generator = new ConfigGenerator();

		generator.setBaseProjectInformations(projectName,
				pPropertiesPage.getAuthors(), pPropertiesPage.getVersion(),
				pPropertiesPage.getRelease(),
				pAdvancedPropertiesPage.getMasterDocumentName());

		// Static and Template folders
		generator.setStaticFoldersPrefix(pAdvancedPropertiesPage
				.getStaticFoldersPrefix());

		// Output language (HTML, and LaTex if possible)
		generator.setLanguage(pPropertiesPage.getLanguage());

		// Master document (index.rst) file name
		generator.setStringProperty(IConfigConstants.MASTER_DOCUMENT_NAME,
				pAdvancedPropertiesPage.getMasterDocumentName(),
				IConfigConstants.DEFAULT_MASTER_DOCUMENT_NAME);

		// Constructs the project content
		try {
			setProjectContent(generator);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Generates the project Makefile, like sphinx-quickstart would do
	 * 
	 * @param aTemplateFile
	 *            Path of the template file (bundle relative)
	 * @throws IOException
	 *             The template file can't be opened
	 */
	protected String prepareTemplate(final String aTemplateFile)
			throws IOException {
		StringBuilder builder = new StringBuilder();

		// Read the template file
		Path templatePath = new Path(aTemplateFile);

		InputStream templateStream = FileLocator.openStream(RestPlugin
				.getDefault().getBundle(), templatePath, false);

		BufferedReader templateReader = new BufferedReader(
				new InputStreamReader(templateStream));

		int charsRead = 0;
		char[] buffer = new char[2048];

		while ((charsRead = templateReader.read(buffer)) > 0) {
			builder.append(buffer, 0, charsRead);
		}

		// Replace the source folder variable
		int varStart = builder
				.indexOf(IConfigConstants.RESOURCE_TEMPLATE_SOURCE_VAR);

		// This block should never be skipped
		if (varStart != -1) {
			String sourceFolder = ".";

			if (pPropertiesPage.isSourceBuildSeparated()) {
				sourceFolder = IConfigConstants.SOURCE_FOLDER_NAME;
			}

			builder.replace(varStart, varStart
					+ IConfigConstants.RESOURCE_TEMPLATE_SOURCE_VAR.length(),
					sourceFolder);
		}

		// Return the result
		return builder.toString();
	}

	/**
	 * Prepares the folders hierarchy and creates the conf.py file
	 * 
	 * @param aConfigGenerator
	 *            Configuration generator
	 * @throws CoreException
	 *             An error occurred while create folders or files
	 * @throws IOException
	 *             An error occured while reading the template files
	 */
	protected void setProjectContent(final ConfigGenerator aConfigGenerator)
			throws CoreException, IOException {

		IFolder folder;
		String rootFolder = "";
		IProject project = getNewProject();

		// Separated source and build folders
		if (pPropertiesPage.isSourceBuildSeparated()) {

			// Create source folder
			folder = project.getFolder(IConfigConstants.SOURCE_FOLDER_NAME);
			if (!folder.exists()) {
				folder.create(false, true, null);
			}

			// Create build folder
			folder = project.getFolder(IConfigConstants.BUILD_FOLDER_NAME);
			if (!folder.exists()) {
				folder.create(false, true, null);
			}

			// Set the source relative files prefix
			rootFolder = IConfigConstants.SOURCE_FOLDER_NAME + "/";
		}

		// Prepare the static and template folders
		folder = project.getFolder(rootFolder
				+ aConfigGenerator.getProperty(IConfigConstants.STATIC_PATH));
		if (!folder.exists()) {
			folder.create(false, true, null);
		}

		folder = project.getFolder(rootFolder
				+ aConfigGenerator.getProperty(IConfigConstants.TEMPLATE_PATH));
		if (!folder.exists()) {
			folder.create(false, true, null);
		}

		// Set the config.py file
		String configContent = aConfigGenerator.generateConfigurationContent();
		writeFile(rootFolder + IConfigConstants.CONFIG_FILE_NAME, configContent);

		// Set the "index.rst" file
		String indexFile = rootFolder
				+ aConfigGenerator
						.getProperty(IConfigConstants.MASTER_DOCUMENT_NAME)
				+ IConfigConstants.REST_FILE_EXTENSION;

		writeFile(indexFile, generateMasterDocumentContent(project.getName()));

		// Write the make files (always at the root of the project)
		writeFile("Makefile",
				prepareTemplate(IConfigConstants.RESOURCE_MAKEFILE_TEMPLATE));

		writeFile("make.bat",
				prepareTemplate(IConfigConstants.RESOURCE_MAKEBAT_TEMPLATE));
	}

	/**
	 * Writes the given content in the given file
	 * 
	 * @param aFileName
	 *            Resource name
	 * @param aContent
	 *            File content
	 * @throws CoreException
	 *             Error while setting resource content
	 */
	protected void writeFile(final String aFileName, final String aContent)
			throws CoreException {
		IFile outputFile = getNewProject().getFile(aFileName);

		// Prepare the input stream object
		InputStream stream = new InputStream() {
			private final Reader fReader = new StringReader(aContent);

			@Override
			public int read() throws IOException {
				return fReader.read();
			}
		};

		// Write file content
		if (!outputFile.exists()) {
			outputFile.create(stream, false, null);
		} else {
			outputFile.setContents(stream, 0, null);
		}
	}
}