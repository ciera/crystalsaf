/**
 * Copyright (c) 2006, 2007, 2008 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman,    
 * Kevin Bierhoff, David Dickey, Ciera Jaspan, Thomas LaToza, Gabriel Zenarosa, and others.
 *
 * This file is part of Crystal.
 *
 * Crystal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Crystal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Crystal.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cmu.cs.crystal.test;

import static org.junit.Assert.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cmu.cs.crystal.Crystal;
import edu.cmu.cs.crystal.IAnalysisReporter;
import edu.cmu.cs.crystal.IRunCrystalCommand;
import edu.cmu.cs.crystal.internal.AbstractCrystalPlugin;
import edu.cmu.cs.crystal.internal.Box;
import edu.cmu.cs.crystal.internal.NullPrintWriter;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;

import static org.junit.runners.Parameterized.Parameters;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * A test class that is meant to be completed by analysis writers. This test
 * is so-named because it is meant to be used for analyses that report errors,
 * and therefore a test can be judged as successful or unsuccessful by whether
 * or not a run on a file reported errors or not.
 * 
 * At the abstract level, this class provides several features.<br>
 * <ul>
 * <li>It will run analyses on all the Java files in a specified project.
 * <li>It allows you to ignore certain files.
 * <li>It allows you to specify whether or not an anlysis should pass or fail
 *     a test, and encode that in the test file's name.
 * <li>It allows to to encode which analyses should be run on a file in that file's
 *     name.
 * </ul>
 *  
 *  These features are further discussed in the documentation for those methods.
 *  
 * @author Nels E. Beckman
 */
public abstract class FailcessTest {
	
	/**
	 * Returns the name of the project in the test workspace that contains the
	 * test files.<br>
	 * <br>
	 * Clients must implement this method.
	 */
	protected abstract String getTestProject();
	
	/**
	 * Returns the prefix string for a file name that indicates this is a test
	 * that should fail.<br>
	 * <br>
	 * For instance, I like my failing tests to start with XXX. Therefore, when
	 * an analysis is run on the Java file, XXX_DivideByZero_test1.java, this
	 * class knows that the analysis should report at least one failure.<br>
	 * <br>
	 * Clients must implement this method.
	 */
	protected abstract String getFailurePrefix();
	
	/**
	 * Return a set of <i>Fully-Qualified</i> java file names that will be
	 * ignored by this test. In this case, fully-qualified means the Eclipse
	 * version of fully qualified.<br>
	 * <br>
	 * For instance, if this method returns the singleton,
	 * "/CrystalTestProject/src/XXX_DivideByZ_ignored.java",
	 * the analysis will not be run on that particular file.<br>
	 * <br>
	 * Clients must implement this method.
	 */
	protected abstract Set<String> getFilesToIgnore();
	
	/**
	 * Returns the string that separates information in the test file's name.
	 * This is important because a test's name will encode whether or not the
	 * test should fail, and which analyses should be run.<br>
	 * <br>
	 * For instance, I like to use "_" as a separator. XXX_DivideByZero_test1.java
	 * and DivideByZero_test2.java are both good examples of this.<br>
	 * <br>
	 * Clients must implement this method.
	 */
	protected abstract String getSeparator();
	
	@Test
	public void testAnalysis() throws CoreException {
		Pattern analysis_pattern = createAnalysisMatcher();
		
		for( final ICompilationUnit cu : testFiles() ) {
			IFile cu_file = (IFile)cu.getCorrespondingResource();
			String qualified_file = cu_file.getFullPath().toString();
			String unqualified_file = cu_file.getName();
			
			// Should we ignore this file?
			if( getFilesToIgnore().contains(qualified_file) )
				continue;
			
			// Should it pass or fail?
			boolean should_fail = unqualified_file.startsWith(getFailurePrefix());
			
			// Which analyses should we use on this file?
			final Set<String> analyses = enabledAnalysesFromName(analysis_pattern, unqualified_file);
			
			// Create command
			final Box<Boolean> analysis_succeeded = Box.box(true);
			
			IRunCrystalCommand run_command = new IRunCrystalCommand(){
				public Set<String> analyses() { return analyses; }
				public List<ICompilationUnit> compilationUnits() { 
					return Collections.singletonList(cu); 
				}
				public IAnalysisReporter reporter() {
					return new IAnalysisReporter(){
						public void clearMarkersForCompUnit(ICompilationUnit compUnit) {}
						public PrintWriter debugOut() { return NullPrintWriter.instance(); }
						public void reportUserProblem(String p, ASTNode n, String a) {
							// If an error is reported, we did not succeed.
							analysis_succeeded.setValue(false);
						}
						public PrintWriter userOut() { return NullPrintWriter.instance(); }
					};
				}
			};
			
			// Run analysis
			Crystal crystal = AbstractCrystalPlugin.getCrystalInstance();
			crystal.runAnalyses(run_command, null);
			
			// Assert success/failure
			String err = "Analyses "+ analyses + " were " + (should_fail ? "not" : "") + 
				" supposed to succeed on file " + qualified_file + " but did " +
				(analysis_succeeded.getValue() ? "" : "not") +  ".";
			assertEquals(err, !should_fail, analysis_succeeded.getValue());
		}
	}
		
	private Pattern createAnalysisMatcher() {
		String regex_string = "(" + getFailurePrefix() + getSeparator() + ")?" +
			"((.+)"+ getSeparator() + ")+(.*)"; 
		return Pattern.compile(regex_string);
	}

	private Set<String> enabledAnalysesFromName(Pattern analysis_pattern, String unqualified_file) {
		Matcher match = analysis_pattern.matcher(unqualified_file);
		if( !match.matches() ) {
			fail("Something wrong with filename: " + unqualified_file);
		}
		
		List<String> names = new LinkedList<String>();
		for( int i=3; i<match.groupCount(); i+=2 ) {
			// This strange incrementation is due to the repeating of the patterns, 
			// which happen every two elements, starting at 3.
			names.add(match.group(i));
		}
		
		Set<String> result = new HashSet<String>();
		for( String name : names ) {
			for( String existing : AbstractCrystalPlugin.getAllAnalyses() ) {
				if( existing.startsWith(name) )
					result.add(existing);
			}
		}
		return result;
	}

	private List<ICompilationUnit> testFiles() throws CoreException {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getTestProject());
		project.open(null);
		final IJavaProject java_project = JavaCore.create(project);
		final List<ICompilationUnit> cus = WorkspaceUtilities.collectCompilationUnits(java_project);
		
		return cus;
	}
}
