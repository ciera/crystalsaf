/**
 * Copyright (c) 2006, 2007, 2008 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman, Kevin
 * Bierhoff, David Dickey, Ciera Jaspan, Thomas LaToza, Gabriel Zenarosa, and others.
 * 
 * This file is part of Crystal.
 * 
 * Crystal is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Crystal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with Crystal. If
 * not, see <http://www.gnu.org/licenses/>.
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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.cmu.cs.crystal.IAnalysisReporter;
import edu.cmu.cs.crystal.IRunCrystalCommand;
import edu.cmu.cs.crystal.annotations.AnalysisTests;
import edu.cmu.cs.crystal.annotations.FailingTest;
import edu.cmu.cs.crystal.annotations.PassingTest;
import edu.cmu.cs.crystal.annotations.UseAnalyses;
import edu.cmu.cs.crystal.internal.AbstractCrystalPlugin;
import edu.cmu.cs.crystal.internal.Crystal;
import edu.cmu.cs.crystal.internal.NullPrintWriter;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;
import edu.cmu.cs.crystal.util.Box;

/**
 * An analysis test that uses test annotations (
 * 
 * PassingTest,
 * FailingTest,
 * UseAnalyses ) to determine which analyses to run, and whether or not the given file is a test.
 *              This test will examine every Java file in your test workspace!
 * 
 * @author Nels E. Beckman
 */
@RunWith(Parameterized.class)
public class AnnotatedTest {
	
	private static final Logger log = Logger.getLogger(AnnotatedTest.class.getName());

	private static class TestType {
		final boolean passingTest;
		final int numErrors;
		final String analysisToRun;

		public TestType(boolean passingTest, int numErrors, String analysisToRun) {
			this.passingTest = passingTest;
			this.numErrors = numErrors;
			this.analysisToRun = analysisToRun;
		}
	}

	/**
	 * Given a compilation unit, returns the type of test that it is, or none if this compilation
	 * unit is not a test.
	 */
	public static Collection<TestType> findTestTypes(CompilationUnit comp_unit) {
		final Box<Boolean> is_a_simple_test = Box.box(false);
		final Box<Boolean> is_passing_test = Box.box(false);
		final Box<Integer> failures = Box.box(0);
		final Set<String> analyses = new LinkedHashSet<String>();
		final Set<TestType> tests = new LinkedHashSet<TestType>();

		ASTVisitor annotation_visitor = new ASTVisitor() {
			// Visitor will find @PassingTest, @FailingTest, @UseAnalyses, and @AnalysisTests
			// @AnalysisTests should not be used with the others!
			
			private TestType parseFailOrPassAnnotation(IAnnotationBinding binding, boolean isPass) {
				int numErrors = 0;
				String analysis = "";
				for (IMemberValuePairBinding pair : binding.getAllMemberValuePairs()) {
					if ("value".equals(pair.getName())) {
						numErrors = (Integer) pair.getValue();
					}
					else if ("analysis".equals(pair.getName())) {
						analysis = (String) pair.getValue();
					}
					else
						throw new RuntimeException("Unknown parameter " + pair.getName());
				}
				return new TestType(isPass, numErrors, analysis);
			}

			private Collection<? extends TestType> parseAnalysisTests(IAnnotationBinding binding) {
				Collection<TestType> tests = new LinkedList<TestType>();
				
				for (IMemberValuePairBinding pair : binding.getAllMemberValuePairs()) {
					if ("fail".equals(pair.getName())) {
						Object[] failures = (Object[]) pair.getValue();
						for (Object failure : failures) {
							tests.add(parseFailOrPassAnnotation((IAnnotationBinding)failure, false));
						}
					}
					else if ("pass".equals(pair.getName())) {
						Object[] passes = (Object[]) pair.getValue();
						for (Object pass : passes) {
							tests.add(parseFailOrPassAnnotation((IAnnotationBinding)pass, true));
						}						
					}
					else
						throw new RuntimeException("Unknown parameter in @AnalysisTests " + pair.getName());
				}
				return tests;
			}

			private Integer intValueFromBinding(IAnnotationBinding binding) {
				for (IMemberValuePairBinding pair : binding.getAllMemberValuePairs()) {
					if ("value".equals(pair.getName())) {
						return ((Integer) pair.getValue());
					}
				}

				throw new RuntimeException("Annotation had no value field.");
			}

			private Collection<? extends String> stringSetValueFromBinding(
			    IAnnotationBinding binding) {
				for (IMemberValuePairBinding pair : binding.getAllMemberValuePairs()) {
					if ("value".equals(pair.getName())) {
						Object value = pair.getValue();

						if (value instanceof String[]) {
							return Arrays.asList((String[]) value);
						}
						else if (value instanceof String) {
							return Collections.singleton((String) value);
						}
						else if (value instanceof Object[]) {
							List<String> result = new LinkedList<String>();
							for (Object obj : ((Object[]) value)) {
								if (obj instanceof String)
									result.add((String) obj);
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

				throw new RuntimeException("Annotation had no value field.");
			}

			@Override
			public boolean visit(MarkerAnnotation node) {
				// A marker annotation looks like, @PassingTest
				String annotation = node.resolveTypeBinding().getQualifiedName();

				if (FailingTest.class.getName().equals(annotation)) {
					is_a_simple_test.setValue(true);
					is_passing_test.setValue(false);
				}
				else if (PassingTest.class.getName().equals(annotation)) {
					is_a_simple_test.setValue(true);
					is_passing_test.setValue(true);
				}

				return true;
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				// A normal annotation looks like @FailingTest(value=2)
				String annotation = node.resolveTypeBinding().getQualifiedName();

				if (FailingTest.class.getName().equals(annotation)) {
					failures.setValue(intValueFromBinding(node.resolveAnnotationBinding()));
					is_a_simple_test.setValue(true);
					is_passing_test.setValue(false);
				}
				else if (UseAnalyses.class.getName().equals(annotation)) {
					analyses.addAll(stringSetValueFromBinding(node.resolveAnnotationBinding()));
				}
				else if (AnalysisTests.class.getName().equals(annotation)) {
					tests.addAll(parseAnalysisTests(node.resolveAnnotationBinding()));
					return false;
				}

				return true;
			}


			@Override
			public boolean visit(SingleMemberAnnotation node) {
				// A single member annotation looks like @FailingTest(2)
				String annotation = node.resolveTypeBinding().getQualifiedName();

				if (FailingTest.class.getName().equals(annotation)) {
					failures.setValue(intValueFromBinding(node.resolveAnnotationBinding()));
					is_a_simple_test.setValue(true);
					is_passing_test.setValue(false);
				}
				else if (UseAnalyses.class.getName().equals(annotation)) {
					analyses.addAll(stringSetValueFromBinding(node.resolveAnnotationBinding()));
				}

				return true;
			}

			@Override
			public boolean visit(AnnotationTypeDeclaration node) {
				visitAnnotations(node);
				// visit only top-level types
				return false;
			}

			@Override
			public boolean visit(EnumDeclaration node) {
				visitAnnotations(node);
				// visit only top-level types
				return false;
			}

			@Override
			public boolean visit(TypeDeclaration node) {
				visitAnnotations(node);
				// visit only top-level types
				return false;
			}

			public void visitAnnotations(AbstractTypeDeclaration node) {
				// manually visit annotations
				for(IExtendedModifier m : (List<IExtendedModifier>) node.modifiers()) {
					if(m instanceof Annotation) {
						((Annotation) m).accept(this);
					}
				}
			}
		};

		// visitor mutates state
		comp_unit.accept(annotation_visitor);

		if (is_a_simple_test.getValue() && analyses.isEmpty()) {
			throw new RuntimeException("No analyses specified for the simple test: "
			    + comp_unit.getJavaElement().getElementName());
		}

		if (is_a_simple_test.getValue()) {
			for (String analysis : analyses) 
				tests.add(new TestType(is_passing_test.getValue(), failures.getValue(), analysis));
		}
		
		return tests;
	}

	// Provides the list of files upon which to test
	// By definition of the JUnit Parameterized test, this must return a
	// collection of Object[]. The object[] will be passed into the constructor for the test.
	// Therefore, each item in the collection represents a single test case
	@Parameters
	public static Collection<Object[]> testFiles() {
		List<Object[]> result = new LinkedList<Object[]>();

		List<ICompilationUnit> allCompUnits = WorkspaceUtilities.scanForCompilationUnits();

		if( allCompUnits == null )
			throw new RuntimeException("scanForCompilationUnits() returned null. You may have chosen a workspace that doesn't exist.");
		
		// Not all compilation units are tests...
		int cur = 0;
		for (ICompilationUnit icu : allCompUnits) {
			// the scans the mode for @UseAnalyses
			// this is to avoid parsing every single comp unit which is really slow
			boolean lookCloser = false;
			try {
				for(IType t : icu.getAllTypes()) {
					for(IAnnotation a : t.getAnnotations()) {
						if(a.getElementName().contains("UseAnalyses") || a.getElementName().contains("AnalysisTests"))
							lookCloser = true;
					}
				}
			} 
			catch (JavaModelException e) {
				lookCloser = true;
			}
			
			if(!lookCloser)
				continue;
			
			CompilationUnit cu =
			    (CompilationUnit) WorkspaceUtilities.getASTNodeFromCompilationUnit(icu);
			Collection<TestType> tests = findTestTypes(cu);

			for (TestType test : tests) {
				// These three array elements correspond to the three parameters
				// accepted by AnnotatedTest's constructor.
				result.add(new Object[] { icu, test, cur++ });
			}
		}

		return result;
	}

	private final ICompilationUnit icu;
	private final TestType test;
	private final int testIndex;

	public AnnotatedTest(ICompilationUnit icu, TestType test, Integer index) {
		this.icu = icu;
		this.test = test;
		this.testIndex = index != null ? index.intValue() : -1;
	}

	private String fileName() {
		return icu.getElementName();
	}

	@Test
	public void testAnalysisOnFile() throws Throwable {

		// Boxed integer which will be incremented every time an error is reported
		final Box<Integer> failures_encountered = Box.box(0);

		// Create run command
		IRunCrystalCommand run_command = new IRunCrystalCommand() {
			public Set<String> analyses() {
				Set<String> singleSet = new HashSet<String>();
				singleSet.add(AnnotatedTest.this.test.analysisToRun);
				return singleSet;
			}

			public List<ICompilationUnit> compilationUnits() {
				return Collections.singletonList(AnnotatedTest.this.icu);
			}

			public IAnalysisReporter reporter() {
				return new IAnalysisReporter() {
					public void clearMarkersForCompUnit(ITypeRoot compUnit) {}

					public PrintWriter debugOut() {
						return NullPrintWriter.instance();
					}

					public PrintWriter userOut() {
						return NullPrintWriter.instance();
					}

					public void reportUserProblem(String p, ASTNode n, String a) {
						reportUserProblem(p, n, a, SEVERITY.INFO);
					}

					public void reportUserProblem(String p, ASTNode n, String a, SEVERITY v) {
						// One more error reported
						failures_encountered.setValue(failures_encountered.getValue() + 1);
					}
				};
			}
		};

		// Run analysis
		Crystal crystal = AbstractCrystalPlugin.getCrystalInstance();
		if(testIndex >= 0)
			log.info("[" + testIndex + "] " + AnnotatedTest.this.icu.getElementName());
		try {
			crystal.runAnalyses(run_command, null);
		}
		catch(Throwable t) {
			// log exception, since Eclipse's JUnit GUI doesn't tell us the file where it happened
			log.log(Level.SEVERE, 
					"Exception in [" + testIndex + "] " + AnnotatedTest.this.icu.getElementName(), 
					t);
			// re-throw so JUnit knows about the failure
			throw t;
		}
		
		// assert success/failure
		if (this.test.passingTest) {
			assertEquals(
			    fileName() + " is supposed to pass, but reported errors for " + test.analysisToRun, 0, failures_encountered
			        .getValue().intValue());
		}
		else if (this.test.numErrors <= 0) {
			assertTrue(
			    fileName() + " is supposed to fail, but reported no errors for " + test.analysisToRun, failures_encountered
			        .getValue() > 0);
		}
		else {
			assertEquals(
			    fileName() + " reported a different number of errors than expected for " + test.analysisToRun,
			    this.test.numErrors, failures_encountered.getValue().intValue());
		}
	}

}
