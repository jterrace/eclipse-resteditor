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

package org.isandlatech.plugins.rest.editor.userassist.contentassist;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.isandlatech.plugins.rest.editor.userassist.HelpMessagesUtil;
import org.isandlatech.plugins.rest.parser.RestLanguage;

/**
 * Content assistant processor for literal blocks. Fills it with all possible
 * declarative keywords.
 * 
 * @author Thomas Calmant
 */
public class DeclarativeProposalProcessor extends AbstractProposalProcessor {

	/**
	 * Builds an array of completion proposals. Adds a suffix to each
	 * suggestion.
	 * 
	 * @param aSuggestions
	 *            A suggestion -> description mapping
	 * @param aReplacedWord
	 *            Replaced word
	 * @param aOffset
	 *            Offset of the beginning of the replaced word
	 * @return An array of completion proposals, null on error
	 */
	@Override
	protected ICompletionProposal[] buildProposals(final IDocument aDocument,
			final Map<String, String> aSuggestions, final String aReplacedWord,
			final int aOffset) {

		if (aSuggestions == null || aSuggestions.size() == 0) {
			return null;
		}

		ICompletionProposal[] proposals = new ICompletionProposal[aSuggestions
				.size()];

		int i = 0;
		for (Entry<String, String> suggestionEntry : aSuggestions.entrySet()) {
			String suggestion = suggestionEntry.getKey();
			String description = suggestionEntry.getValue();

			if (description == null) {
				description = suggestion;
			}

			// Suggestion with a suffix
			String replacementWord = suggestion + ":: ";

			// Completion proposal
			proposals[i++] = new HoverCompletionProposal(aDocument,
					replacementWord, aOffset, aReplacedWord.length(),
					suggestion, description);
		}

		return proposals;
	}

	/**
	 * Builds a sorted map containing all ReST and Sphinx directives starting
	 * with the given word, and a small description telling if the directive is
	 * a ReST or a Sphinx one.
	 * 
	 * @param aWord
	 *            Directive prefix
	 * @return All directives starting with the given word, or an empty map
	 */
	@Override
	protected Map<String, String> buildSuggestions(final String aWord) {
		SortedMap<String, String> result = new TreeMap<String, String>();

		for (String keyword : RestLanguage.DIRECTIVES) {
			if (keyword.startsWith(aWord)) {
				result.put(keyword, HelpMessagesUtil.getDirectiveHelp(keyword));
			}
		}

		for (String keyword : RestLanguage.SPHINX_DIRECTIVES) {
			if (keyword.startsWith(aWord)) {
				result.put(keyword, HelpMessagesUtil.getDirectiveHelp(keyword));
			}
		}

		for (String keyword : RestLanguage.SPHINX_EXTENSIONS_DIRECTIVES) {
			if (keyword.startsWith(aWord)) {
				result.put(keyword, HelpMessagesUtil.getDirectiveHelp(keyword));
			}
		}

		return result;
	}
}
