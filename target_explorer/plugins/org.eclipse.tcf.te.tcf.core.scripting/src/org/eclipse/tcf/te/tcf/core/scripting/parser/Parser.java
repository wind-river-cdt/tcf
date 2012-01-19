/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.scripting.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.JSON;

/**
 * Script parser implementation.
 */
public class Parser {
	// Reference to the script to parse
	private final String script;

	// Define some patterns to match the lines against
	private static final Pattern EMPTY_LINE = Pattern.compile("\\s*"); //$NON-NLS-1$
	private static final Pattern COMMENT_LINE = Pattern.compile("\\s*#.*"); //$NON-NLS-1$
	private static final Pattern CONNECT_LINE = Pattern.compile("\\s*connect\\s+.*"); //$NON-NLS-1$
	private static final Pattern COMMAND_LINE = Pattern.compile("\\s*tcf\\s+(\\w+)\\s+(\\w+)(.*)"); //$NON-NLS-1$

	/**
     * Constructor.
     */
    public Parser(String script) {
    	Assert.isNotNull(script);
    	this.script = script;
    }

    /**
     * Parse the given script and returns the extracted command tokens.
     *
     * @return The list of command tokens found in the script, or an empty list.
     * @throws IOException - if the script parsing fails.
     */
    public Token[] parse() throws IOException {
    	List<Token> tokens = new ArrayList<Token>();

    	BufferedReader reader = new BufferedReader(new StringReader(script));

    	String line;
    	while ((line = reader.readLine()) != null) {
    		// All the following lines are ignored if matched
    		if (EMPTY_LINE.matcher(line).matches()) continue;
    		if (COMMENT_LINE.matcher(line).matches()) continue;
    		if (CONNECT_LINE.matcher(line).matches()) continue;

    		// If it is a command line, get the groups from it
    		Matcher matcher = COMMAND_LINE.matcher(line);
    		if (matcher.matches()) {
    			String serviceName = matcher.group(1).trim();
    			String commandName = matcher.group(2).trim();
    			String arguments = matcher.group(3);

    			// Create a new token
    			Token token = new Token();
    			token.setServiceName(serviceName);
    			token.setCommandName(commandName);

    			// Parse the arguments
    			parseArguments(token, arguments);

    			// Add the token to the list
    			tokens.add(token);
    		}
    	}

    	reader.close();

    	return tokens.toArray(new Token[tokens.size()]);
    }

    /**
     * Parse the arguments string and add the extracted arguments
     * to the given token.
     *
     * @param token The token. Must not be <code>null</code>.
     * @param arguments The arguments string or <code>null</code>.
     */
    protected void parseArguments(Token token, String arguments) {
    	Assert.isNotNull(token);

    	if (arguments == null || "".equals(arguments.trim())) { //$NON-NLS-1$
    		return;
    	}

    	// Tokenize by space, but do special handling for maps and lists
    	StringTokenizer tokenizer = new StringTokenizer(arguments, " "); //$NON-NLS-1$
    	while (tokenizer.hasMoreTokens()) {
    		String tok = tokenizer.nextToken();
    		if (tok == null || "".equals(tok.trim())) continue; //$NON-NLS-1$

    		if (tok.equals("null")) { //$NON-NLS-1$
    			token.addArgument(null);
    			continue;
    		}

    		if (tok.startsWith("\"")) { //$NON-NLS-1$
    			// String type

    			String fullTok = tok;
    			boolean complete = isComplete(fullTok, '"', '"');
    			while (!complete && tokenizer.hasMoreTokens()) {
    				fullTok = fullTok + " " + tokenizer.nextToken(); //$NON-NLS-1$
    				complete = isComplete(fullTok, '"', '"');
    			}

    			if (complete) {
    				fullTok = fullTok.trim();
    				if (fullTok.startsWith("\"")) fullTok = fullTok.substring(1); //$NON-NLS-1$
    				if (fullTok.endsWith("\"")) fullTok = fullTok.substring(0, fullTok.length() - 1); //$NON-NLS-1$
    				token.addArgument(fullTok);
    				continue;
    			}
    		}

    		if ("true".equalsIgnoreCase(tok) || "false".equalsIgnoreCase(tok)) { //$NON-NLS-1$ //$NON-NLS-2$
    			token.addArgument(Boolean.valueOf(tok));
    			continue;
    		}

    		try {
    			Integer i = Integer.decode(tok);
    			token.addArgument(i);
    			continue;
    		} catch (NumberFormatException e) { /* ignored on purpose */ }

    		try {
    			Long l = Long.decode(tok);
    			token.addArgument(l);
    			continue;
    		} catch (NumberFormatException e) { /* ignored on purpose */ }

    		try {
    			Float f = Float.valueOf(tok);
    			token.addArgument(f);
    			continue;
    		} catch (NumberFormatException e) { /* ignored on purpose */ }

    		try {
    			Double d = Double.valueOf(tok);
    			token.addArgument(d);
    			continue;
    		} catch (NumberFormatException e) { /* ignored on purpose */ }

    		// If it starts with '{' or '[', it's a map or list type
    		if (tok.startsWith("{")) { //$NON-NLS-1$
    			// Map type

    			String fullTok = tok;
    			boolean complete = isComplete(fullTok, '{', '}');
    			while (!complete && tokenizer.hasMoreTokens()) {
    				fullTok = fullTok + " " + tokenizer.nextToken(); //$NON-NLS-1$
    				complete = isComplete(fullTok, '{', '}');
    			}

    			if (complete) {
    				fullTok = fullTok + "\0"; //$NON-NLS-1$
    				try {
    					Object[] args = JSON.parseSequence(fullTok.getBytes());
    					if (args != null) {
    						for (Object arg : args) {
    							if (arg != null) token.addArgument(arg);
    						}
    						continue;
    					}
    				} catch (IOException e) { /* ignored on purpose */ e.printStackTrace(); }
    			}
    		}

    		if (tok.startsWith("[")) { //$NON-NLS-1$
    			// List type

    			String fullTok = tok;
    			boolean complete = isComplete(fullTok, '[', ']');
    			while (!complete && tokenizer.hasMoreTokens()) {
    				fullTok = fullTok + " " + tokenizer.nextToken(); //$NON-NLS-1$
    				complete = isComplete(fullTok, '[', ']');
    			}

    			if (complete) {
    				fullTok = fullTok + "\0"; //$NON-NLS-1$
    				try {
    					Object[] args = JSON.parseSequence(fullTok.getBytes());
    					if (args != null) {
    						for (Object arg : args) {
    							if (arg != null) token.addArgument(arg);
    						}
    						continue;
    					}
    				} catch (IOException e) { /* ignored on purpose */ }
    			}
    		}

    		// Add the argument token as is
    		token.addArgument(tok);
    	}
    }

    /**
     * Counts the number of opening and closing characters inside the given
     * string and returns <code>true</code> if the number matches.
     *
     * @param tok The arguments token. Must not be <code>null</code>.
     * @param opening The opening character.
     * @param closing The closing character.
     *
     * @return <code>True</code> if the number of opening characters matches the number of closing characters, <code>false</code> otherwise.
     */
    protected boolean isComplete(String tok, char opening, char closing) {
    	Assert.isNotNull(tok);

    	int countOpening = 0;
    	int countClosing = 0;

    	boolean same = opening == closing;

    	for (int i = 0; i < tok.length(); i++) {
    		char c = tok.charAt(i);

    		if (c == opening && same) {
    			if (countOpening > countClosing) countClosing++;
    			else countOpening++;
    		} else {
    			if (c == opening) { countOpening++; continue; }
    			if (c == closing) { countClosing++; continue; }
    		}
    	}

    	return countOpening > 0 && countOpening == countClosing;
    }
}
