/**
 * File:   MarkupRule.java
 * Author: Thomas Calmant
 * Date:   21 janv. 2011
 */
package org.isandlatech.plugins.rest.editor.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;

import eclihx.ui.internal.ui.editors.ScannerController;

/**
 * @author Thomas Calmant
 */
public class MarkupRule extends AbstractRule {

	/** Marker end string */
	private String pEnd;

	/** False if first character mustn't be a white space */
	private boolean pNoSpace;

	/** Marker start string */
	private String pStart;

	/**
	 * Configures the rule
	 * 
	 * @param aMarkupDelimiter
	 *            Start and end marker
	 * @param aToken
	 *            Token returned on success
	 */
	public MarkupRule(final String aMarkupDelimiter, final IToken aToken) {
		this(aMarkupDelimiter, aMarkupDelimiter, aToken, true);
	}

	/**
	 * Configures the rule
	 * 
	 * @param aMarkupStart
	 *            Start marker
	 * @param aMarkupEnd
	 *            End marker
	 * @param aToken
	 *            Token returned on success
	 */
	public MarkupRule(final String aMarkupStart, final String aMarkupEnd,
			final IToken aToken) {
		this(aMarkupStart, aMarkupEnd, aToken, true);
	}

	/**
	 * Configures the rule
	 * 
	 * @param aMarkupStart
	 *            Start marker
	 * @param aMarkupEnd
	 *            End marker
	 * @param aToken
	 *            Token returned on success
	 * @param aNoStartSpace
	 *            If true, the marker must be follow by anything but a
	 *            whitespace
	 */
	public MarkupRule(final String aMarkupStart, final String aMarkupEnd,
			final IToken aToken, final boolean aNoStartSpace) {
		super(aToken);

		pStart = aMarkupStart;
		pEnd = aMarkupEnd;
		pNoSpace = aNoStartSpace;
	}

	@Override
	public IToken evaluate(final ICharacterScanner aScanner) {
		ScannerController controller = new ScannerController(aScanner);
		int readChar;
		int contentLength = 0;

		// Test markup beginning
		for (int i = 0; i < pStart.length(); i++) {
			readChar = controller.read();

			if (readChar == ICharacterScanner.EOF
					|| pStart.charAt(i) != readChar) {
				return undefinedToken(controller);
			}
		}

		// Test first character
		if (pNoSpace) {
			readChar = controller.read();

			if (readChar == ICharacterScanner.EOF
					|| Character.isWhitespace(readChar)) {
				return undefinedToken(controller);
			}
		}

		// Test markup ending
		int currentMarkupPos = 0;
		String currentEnd = "";

		while ((readChar = controller.read()) != ICharacterScanner.EOF) {
			contentLength++;

			if (pEnd.charAt(currentMarkupPos) == readChar) {
				currentEnd += (char) readChar;
				currentMarkupPos++;

				if (currentEnd.endsWith(pEnd)) {
					do {
						readChar = controller.read();
						if (readChar != ICharacterScanner.EOF) {
							currentEnd += (char) readChar;
						} else {
							break;
						}
					} while (currentEnd.endsWith(pEnd));

					if (readChar != ICharacterScanner.EOF) {
						controller.unread();
					}

					return getSuccessToken();
				}

			} else {
				currentMarkupPos = 0;
				currentEnd = "";
			}
		}

		return undefinedToken(controller);
	}
}
