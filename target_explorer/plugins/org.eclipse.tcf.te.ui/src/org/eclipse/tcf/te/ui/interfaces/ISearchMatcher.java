/*
 * ISearchMatcher.java
 * Created on Feb 15, 2011
 *
 * Copyright 2008 Wind River Systems Inc. All rights reserved.
 */
package org.eclipse.tcf.te.ui.interfaces;



/**
 * The search matcher interface. A class implementing this interface defines 
 * the searching rule. It is used by {@link ExecutionContextViewer#searchNext} 
 * to find context nodes which matches the rule.
 * 
 * @see ISearchCallback
 * @see ExecutionContextViewer
 * @author william.chen@windriver.com
 *
 */
public interface ISearchMatcher {
	/**
	 * If the current context node matches the matching rule.
	 * 
	 * @param context
	 * 				The execution context node to be examined.
	 * @return true if it matches or else false.
	 */
	public boolean match(Object context);
}
