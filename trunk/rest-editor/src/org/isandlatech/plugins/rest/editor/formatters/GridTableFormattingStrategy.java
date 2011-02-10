/**
 * File:   GridTableFormattingStrategy.java
 * Author: Thomas Calmant
 * Date:   2 févr. 2011
 */
package org.isandlatech.plugins.rest.editor.formatters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.isandlatech.plugins.rest.parser.RestLanguage;

/**
 * @author Thomas Calmant
 * 
 */
public class GridTableFormattingStrategy extends AbstractFormattingStrategy {

	/**
	 * Extract columns content of the given line
	 * 
	 * @param aLine
	 *            A grid table line ('|' column separation)
	 * @return A column-content mapping
	 */
	private Map<Integer, String> extractLineContent(final String aLine) {

		Map<Integer, String> lineContent = new HashMap<Integer, String>();

		int column = 0;
		StringBuffer currentPart = new StringBuffer();

		for (String token : aLine.split("\\|")) {

			if (token.length() != 0
					&& token.charAt(token.length() - 1) == RestLanguage.ESCAPE_CHARACTER) {
				currentPart.append(token).append(
						RestLanguage.GRID_TABLE_ROW_MARKER);

			} else {
				if (column > 0) {
					currentPart.append(token);

					// TODO If the pipe is followed immediately by a letter,
					// then
					// it's a substitution
					// if (token.length() > 1
					// && Character.isLetterOrDigit(token.charAt(1))) {
					// System.out.println("Substitution found : " + token);
					// continue;
					// }

					// Surround with spaces (for eye candy grids)
					String currentPartString = ' ' + currentPart.toString()
							.trim() + ' ';

					System.out.println("New column content found : "
							+ currentPartString);

					lineContent.put(column, currentPartString);
					currentPart = new StringBuffer();
				}
				column++;
			}
		}

		return lineContent;
	}

	@Override
	public String format(final String aContent, final boolean aIsLineStart,
			final String aIndentation, final int[] aPositions) {

		String[] tableLines = getLines(normalizeEndOfLines(aContent));

		int maxCols = 0;
		int nbLines = -1;

		Map<Integer, Map<Integer, String>> tableContent = new HashMap<Integer, Map<Integer, String>>();
		List<Character> kindOfSeparators = new LinkedList<Character>();

		// Analyze the grid
		for (String line : tableLines) {

			if (line.charAt(0) == RestLanguage.GRID_TABLE_MARKER) {

				System.out.println("New line");

				// Look for the character used for separation ('-' or '=' in
				// theory...)
				char separator = RestLanguage.GRID_TABLE_MARKER;
				for (int i = 1; separator == RestLanguage.GRID_TABLE_MARKER
						&& i < line.length(); i++) {

					if (line.charAt(i) != RestLanguage.GRID_TABLE_MARKER) {
						separator = line.charAt(i);
					}
				}

				System.out.println("Found separator : " + separator);

				// Force normalization
				if (separator != '=') {
					separator = '-';
				}

				System.out.println("Selected separator : " + separator);

				// Store the separator
				kindOfSeparators.add(separator);

				// Grid border
				int nbCols = countOccurrences(RestLanguage.GRID_TABLE_MARKER,
						line);

				if (nbCols > maxCols) {
					maxCols = nbCols;
				}

				nbLines++;

			} else {
				// Row content
				Map<Integer, String> oldMap = tableContent.get(nbLines);
				Map<Integer, String> newMap = extractLineContent(line);

				if (oldMap == null) {
					oldMap = newMap;
				} else {
					mergeMaps(oldMap, newMap);
				}

				// Should not be necessary
				tableContent.put(nbLines, oldMap);
			}
		}

		// Set up a new grid
		return generateGrid(tableContent, maxCols, kindOfSeparators);
	}

	/**
	 * Generate the grid table
	 * 
	 * @param aTableContent
	 *            Line -> Column -> Content mapping
	 * @param aMaxNbColumns
	 *            Maximum number of column for a line
	 * @param aKindOfSeparators
	 *            For each line, which separator must be used
	 * @return The formatted grid table string
	 */
	private String generateGrid(
			final Map<Integer, Map<Integer, String>> aTableContent,
			final int aMaxNbColumns, final List<Character> aKindOfSeparators) {

		StringBuffer content = new StringBuffer();

		int[] columnsSizes = new int[aMaxNbColumns];
		Arrays.fill(columnsSizes, 0);

		// 1st pass : columns sizes
		for (Map<Integer, String> lineContent : aTableContent.values()) {
			int column = 0;
			for (String columnContent : lineContent.values()) {
				int columnLength = columnContent.length();

				if (columnLength > columnsSizes[column]) {
					columnsSizes[column] = columnLength;
				}

				column++;
			}
		}

		// Generate marker lines
		String markerLineSimple = generateMarkerLine(aMaxNbColumns,
				columnsSizes, '-');
		String markerLineDouble = generateMarkerLine(aMaxNbColumns,
				columnsSizes, '=');

		System.out.println("Generated lines :\n" + markerLineSimple
				+ markerLineDouble);

		// -1 : the line break does'nt count
		final int markerLineLength = markerLineSimple.length() - 1;

		// 2nd pass : fill the grid
		int currentSeparatorLine = 0;
		for (Map<Integer, String> lineContent : aTableContent.values()) {

			// add starting marker
			switch (aKindOfSeparators.get(currentSeparatorLine++)) {
			case '=':
				content.append(markerLineDouble).append('|');
				break;

			default:
				content.append(markerLineSimple).append('|');
				break;
			}

			// 1 for the starting pipe
			int linePos = 1;

			int column = 0;
			for (String columnContent : lineContent.values()) {

				// Surround with spaces
				content.append(columnContent);
				linePos += columnContent.length();

				for (int i = columnContent.length(); i < columnsSizes[column]; i++) {
					content.append(' ');
					linePos++;
				}

				content.append('|');
				column++;
				linePos++;
			}

			// Insufficient number of columns : span the last one
			if (linePos < markerLineLength) {
				int len = markerLineLength - linePos;
				char[] padding = new char[len];

				Arrays.fill(padding, ' ');
				padding[len - 1] = '|';

				content.append(padding);
			}

			content.append('\n');
		}

		// add ending marker line
		content.append(markerLineSimple);

		return content.toString();
	}

	/**
	 * Generates a grid separation line
	 * 
	 * @param aMaxNbColumns
	 *            Maximum number of columns
	 * @param aColumnsSizes
	 *            An array containing column sizes
	 * @param aSeparator
	 *            The character to be used to fill the line
	 * @return The corresponding separation line
	 */
	private String generateMarkerLine(final int aMaxNbColumns,
			final int[] aColumnsSizes, final char aSeparator) {

		// Generate marker lines
		int maxWidth = aMaxNbColumns;
		for (int columnWidth : aColumnsSizes) {
			maxWidth += columnWidth;
		}

		char[] markerLineChars = new char[maxWidth + 1];
		Arrays.fill(markerLineChars, aSeparator);

		int nextMarker = 0;
		for (int columnWidth : aColumnsSizes) {
			markerLineChars[nextMarker] = RestLanguage.GRID_TABLE_MARKER;
			nextMarker += columnWidth + 1;
		}

		markerLineChars[maxWidth] = '\n';
		return new String(markerLineChars);
	}

	private Map<Integer, String> mergeMaps(final Map<Integer, String> aMapOrg,
			final Map<Integer, String> aMapAdd) {

		List<Integer> treated = new ArrayList<Integer>(aMapOrg.size());

		for (Integer column : aMapOrg.keySet()) {
			String toAdd = aMapAdd.get(column);

			if (toAdd != null) {
				// Surround with spaces (for eye candy grids)
				StringBuffer newContent = new StringBuffer();
				newContent.append(' ').append(aMapOrg.get(column).trim())
						.append(' ').append(toAdd.trim()).append(' ');

				aMapOrg.put(column, newContent.toString());
			}

			treated.add(column);
		}

		for (Integer column : aMapAdd.keySet()) {
			if (!treated.contains(column)) {
				// If we have not treated this column, then it is a valid one
				// not present in aMapOrg
				aMapOrg.put(column, aMapAdd.get(column));
			}
		}

		return aMapOrg;
	}
}
