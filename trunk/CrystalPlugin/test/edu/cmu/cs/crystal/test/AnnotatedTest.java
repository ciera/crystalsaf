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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.cmu.cs.crystal.Crystal;
import edu.cmu.cs.crystal.IAnalysisReporter;
import edu.cmu.cs.crystal.IRunCrystalCommand;
import edu.cmu.cs.crystal.annotations.FailingTest;
import edu.cmu.cs.crystal.annotations.PassingTest;
import edu.cmu.cs.crystal.annotations.UseAnalyses;
import edu.cmu.cs.crystal.internal.AbstractCrystalPlugin;
import edu.cmu.cs.crystal.internal.Box;
import edu.cmu.cs.crystal.internal.NullPrintWriter;
import edu.cmu.cs.crystal.internal.Option;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;

/**
 * An analysis test that uses test annotations ( @PassingTest, @FailingTest,
 * @UseAnalyses ) to determine which analyses to run, and whether or not the
 * given file is a test. This test will examine every Java file in your test
 * workspace!
 * 
 * @author Nels E. Beckman
 */
@RunWith(Parameterized.class)
public class AnnotatedTest {

	private static class TestType {
		final boolean passingTest;
		final int numErrors;
		final Set<String> analysisToRun;
		
		public TestType(boolean passingTest, int numErrors,	Set<String> analysisToRun) {
			this.passingTest = passingTest;
			this.numErrors = numErrors;
			this.analysisToRun = analysisToRun;
		}
	}
	
	/**
	 * Given a compilation unit, returns the type of test that it is, or
	 * none if this compilation unit is not a test.
	 */
	public static Option<TestType> findTestType(CompilationUnit comp_unit) {
		final Box<Boolean> is_a_test = Box.box(false);
		final Box<Boolean> is_passing_test = Box.box(false);
		final Box<Integer> failures = Box.box(0);
		final Set<String> analyses = new LinkedHashSet<String>();
						
		ASTVisitor annotation_visitor = new ASTVisitor() {
			// Visitor will find @PassingTest, @FailingTest, @UseAnalyses
			
			private Integer intValueFromBinding(IAnnotationBinding binding) {
				for( IMemberValuePairBinding pair : binding.getAllMemberValuePairs() ) {
					if( "value".equals(pair.getName()) ) {
						return ((Integer)pair.getValue());
					}
				}
				
				throw new RuntimeException("Anntation had no value field.");
			}
			
			private Collection<? extends String> stringSetValueFromBinding(
					IAnnotationBinding binding) {
				for( IMemberValuePairBinding pair : binding.getAllMemberValuePairs() ) {
					if( "value".equals(pair.getName()) ) {
						Object value = pair.getValue();
						
						if( value instanceof String[] ) {
							return Arrays.asList((String[])value);	
						}
						else if( value instanceof String ) {
							return Collections.singleton((String)value);
						}
						else if( value instanceof Object[] ) {
							List<String> result = new LinkedList<String>();
							for( Object obj : ((Object[])value) ) {
								if( obj instanceof String ) 
									result.add((String)obj);
								else
									throw new RuntimeException("Value was not of expected type.");
							}
							return result;
						}
						else {
							throw new RuntimeException("Value was not of expected type.");
						}
						
					}
				}
				
				throw new RuntimeException("Anntation had no value field.");
			}
			
			@Override
			public boolean visit(MarkerAnnotation node) {
				// A marker annotation looks like, @PassingTest
				String annotation = node.resolveTypeBinding().getQualifiedName();
				
				if( FailingTest.class.getName().equals(annotation) ) {
					is_a_test.setValue(true);
					is_passing_test.setValue(false);
				}
				else if( PassingTest.class.getName().equals(annotation) ) {
					is_a_test.setValue(true);
					is_passing_test.setValue(true);
				}
				
				return true;
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				// A normal annotation looks like @FailingTest(value=2)
				String annotation = node.resolveTypeBinding().getQualifiedName();
				
				if( FailingTest.class.getName().equals(annotation) ) {
					failures.setValue(intValueFromBinding(node.resolveAnnotationBinding()));
					is_a_test.setValue(true);
					is_passing_test.setValue(false);
				}
				else if( UseAnalyses.class.getName().equals(annotation) ) {
					analyses.addAll(stringSetValueFromBinding(node.resolveAnnotationBinding()));
				}
				
				return true;
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				// A single member annotation looks like @FailingTest(2)
				String annotation = node.resolveTypeBinding().getQualifiedName();
				
				if( FailingTest.class.getName().equals(annotation) ) {
					failures.setValue(intValueFromBinding(node.resolveAnnotationBinding()));
					is_a_test.setValue(true);
					is_passing_test.setValue(false);
				}
				else if( UseAnalyses.class.getName().equals(annotation) ) {
					analyses.addAll(stringSetValueFromBinding(node.resolveAnnotationBinding()));
				}
				
				return true;
			}		
		};
		
		// visitor mutates state
		comp_unit.accept(annotation_visitor);
		
		if( is_a_test.getValue() && analyses.isEmpty() ) {
			throw new RuntimeException("No analyses specified.");
		}
		
		if( is_a_test.getValue() )
			return Option.some(new TestType(is_passing_test.getValue(),
					                        failures.getValue(),
					                        analyses));
		else
			return Option.none();
		
	}
	
	// Provides the list of files upon which to test
	@Parameters
	public static Collection<Object[]> testFiles() {
		List<Object[]> result = new LinkedList<Object[]>();
		
		List<ICompilationUnit> allCompUnits = WorkspaceUtilities.scanForCompilationUnits();
		
		// Not all compilation units are tests...
		for( ICompilationUnit icu : allCompUnits ) {
			CompilationUnit cu = (CompilationUnit)WorkspaceUtilities.getASTNodeFromCompilationUnit(icu);
			Option<TestType> tt_ = findTestType(cu);
			
			if( tt_.isSome() ) {
				// These two array elements correspond to the two parameters
				// accepted by AnnotatedTest's constructor.
				result.add(new Object[]{ icu, tt_.unwrap() });
			}
		}
		
		return result;
	}
	
	private final ICompilationUnit icu;
	private final TestType test;
	
	public AnnotatedTest(ICompilationUnit icu, TestType test) {
		this.icu = icu;
		this.test = test;
	}

	private String fileName() {
		return icu.getElementName();
	}
	
	@Test
	public void testAnalysOnFile() {
		
		// Boxed integer which will be incremented every time an error is reported
		final Box<Integer> failures_encountered = Box.box(0);
				
		// Create run command
		IRunCrystalCommand run_command = new IRunCrystalCommand() {
			public Set<String> analyses() { return AnnotatedTest.this.test.analysisToRun; }
			public List<ICompilationUnit> compilationUnits() { 
				return Collections.singletonList(AnnotatedTest.this.icu); 
			}
			public IAnalysisReporter reporter() {
				return new IAnalysisReporter(){
					public void clearMarkersForCompUnit(ICompilationUnit compUnit) {}
					public PrintWriter debugOut() { return NullPrintWriter.instance(); }
					public PrintWriter userOut() { return NullPrintWriter.instance(); }
					public void reportUserProblem(String p, ASTNode n, String a) {
						// One more error reported
						failures_encountered.setValue(failures_encountered.getValue() + 1);
					}
				};
			}
		};
		
		// Run analysis
		Crystal crystal = AbstractCrystalPlugin.getCrystalInstance();
		crystal.runAnalyses(run_command, null);
		
		// assert success/failure
		if( this.test.passingTest ) {
			assertEquals(fileName() + " is supposed to pass, but reported errors.", 
					     0, failures_encountered.getValue());
		}
		else {
			assertTrue(fileName() + " is supposed to fail, but reported no errors.",
					   failures_encountered.getValue() > 0);
			assertEquals(fileName() + " reported a different number of errors than expected.",
					     this.test.numErrors, failures_encountered.getValue());
		}
	}
	
}
