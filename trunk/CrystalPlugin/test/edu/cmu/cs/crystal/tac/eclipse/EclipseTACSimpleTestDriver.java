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
package edu.cmu.cs.crystal.tac.eclipse;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.Assert;

import edu.cmu.cs.crystal.IAnalysisInput;
import edu.cmu.cs.crystal.IAnalysisReporter;
import edu.cmu.cs.crystal.ICrystalAnalysis;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

/**
 * This class makes 3-address code unit tests available as a Crystal analysis.
 * It also provides static helper methods used by those unit tests.
 * The preferred way of running unit tests, however, is using Eclipse's JUnit
 * plugin test feature.
 * @author Kevin Bierhoff
 *
 */
public class EclipseTACSimpleTestDriver implements ICrystalAnalysis {
	
	private static final Logger log = Logger.getLogger(EclipseTACSimpleTestDriver.class.getName());
	
	private boolean runOnce = true;

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.ICrystalAnalysis#runAnalysis()
	 */
	public void runAnalysis(IAnalysisReporter reporter, IAnalysisInput input, ITypeRoot compUnit, CompilationUnit rootNode) {
		if (runOnce) {
			runSimpleTests();
			runTargetSelectionTests();
			runFieldTests();
			runArrayTests();
			runOnce = false;
		}
	}
	
	private void runSimpleTests() {
		EclipseTACSimpleTest simple = new EclipseTACSimpleTest();
		try {
			simple.testSimpleBinop();
			log.info("Passed test: SimpleBinop");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: SimpleBinop", t);
		}

		try {
			simple.testSimpleCall();
			log.info("Passed test: SimpleCall");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: SimpleCall", t);
		}

		try {
			simple.testStaticCall();
			log.info("Passed test: StaticCall");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: StaticCall", t);
		}

		try {
			simple.testSimpleReturn();
			log.info("Passed test: SimpleReturn");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: SimpleReturn", t);
		}

	}
	
	private void runTargetSelectionTests() {
		EclipseTACTargetSelectionTest selectionTests = new EclipseTACTargetSelectionTest();
		try {
			selectionTests.testFactorial();
			log.info("Passed test: Factorial");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: Factorial", t);
		}
		
		try {
			selectionTests.testInitializers();
			log.info("Passed test: Initializers");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: Initializers", t);
		}
		
	}
	
	private void runFieldTests() {
		EclipseTACFieldTest fieldTests = new EclipseTACFieldTest();
		try {
			fieldTests.testReadField();
			log.info("Passed test: ReadField");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: ReadField", t);
		}

		try {
			fieldTests.testReadThis();
			log.info("Passed test: ReadThis");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: ReadThis", t);
		}

		try {
			fieldTests.testObjectRead();
			log.info("Passed test: ObjectRead");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: ObjectRead", t);
		}
		
		try {
			fieldTests.testFieldWrite();
			log.info("Passed test: FieldWrite");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: FieldWrite", t);
		}
		
		try {
			fieldTests.testFieldInc();
			log.info("Passed test: FieldInc");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: FieldInc", t);
		}
		
		try {
			fieldTests.testNestedFieldWrite();
			log.info("Passed test: NestedFieldWrite");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: NestedFieldWrite", t);
		}
		
		try {
			fieldTests.testPrivateOuterField();
			log.info("Passed test: PrivateOuterField");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: PrivateOuterField", t);
		}
		
		try {
			fieldTests.testVisibleOuterField();
			log.info("Passed test: VisibleOuterField");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: VisibleOuterField", t);
		}
		
	}
	
	private void runArrayTests() {
		EclipseTACArrayTest arrayTests = new EclipseTACArrayTest();
		try {
			arrayTests.testArrayRead();
			log.info("Passed test: ArrayRead");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: ArrayRead", t);
		}

		try {
			arrayTests.testArrayWrite();
			log.info("Passed test: ArrayWrite");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: ArrayWrite", t);
		}

		try {
			arrayTests.testArrayInc();
			log.info("Passed test: ArrayInc");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: ArrayInc", t);
		}
		
		try {
			arrayTests.testNestedArrayWrite();
			log.info("Passed test: NestedArrayWrite");
		}
		catch(Throwable t) {
			log.log(Level.SEVERE, "Exception during test: NestedArrayWrite", t);
		}

	}
	
	public static void assertOperands(List<Expression> exprs, List<Variable> operands, EclipseTAC tac) {
		Assert.assertEquals(exprs.size(), operands.size());
		for(int i = 0; i < exprs.size(); i++) {
			Assert.assertEquals(tac.variable(exprs.get(i)), operands.get(i));
		}
	}
	
	public static void assertMethodParameter(Variable x, MethodDeclaration m, int parameterIndex, EclipseTAC tac) {
		Assert.assertTrue(x instanceof SourceVariable);
		Assert.assertEquals(
				tac.sourceVariable(((SingleVariableDeclaration) m.parameters().get(parameterIndex)).resolveBinding()),
				x);
	}

	public static MethodDeclaration getFirstMethod(CompilationUnit compUnit) {
		Assert.assertEquals(0, compUnit.getProblems().length);
		List<MethodDeclaration> methods = WorkspaceUtilities.scanForMethodDeclarationsFromAST(compUnit);
		Assert.assertFalse(methods.isEmpty());
		return methods.get(0);
	}
	
	public static VariableDeclarationFragment getFirstField(CompilationUnit compUnit) {
		Assert.assertEquals(0, compUnit.getProblems().length);
		List<BodyDeclaration> decls = ((AbstractTypeDeclaration) compUnit.types().get(0)).bodyDeclarations(); 
		for(BodyDeclaration b : decls) {
			if(b instanceof FieldDeclaration)
				return (VariableDeclarationFragment) ((FieldDeclaration) b).fragments().get(0);
		}
		Assert.fail("No fields found in first type declared in " + compUnit);
		return null; // unreachable
	}
	
	public static ReturnStatement getLastStatementReturn(MethodDeclaration methodDecl) {
		return (ReturnStatement) getLastStatement(methodDecl);
	}
	
	public static Statement getLastStatement(MethodDeclaration methodDecl) {
		List stmts = methodDecl.getBody().statements();
		Assert.assertFalse(stmts.isEmpty());
		return (Statement) stmts.get(stmts.size()-1);
	}
	
	public static CompilationUnit parseCode(String qualifiedCompUnitName, String code) throws CoreException {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		CompilationUnit node;
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("CrystalTest");
		project.open(null /* IProgressMonitor */);
		
		IJavaProject javaProject = JavaCore.create(project);

		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setProject(javaProject);
		parser.setSource(code.toCharArray());
		parser.setUnitName("/CrystalTest/" + qualifiedCompUnitName);
		parser.setResolveBindings(true);
		node = (CompilationUnit) parser.createAST(null);
		
		Message[] msgs = node.getMessages();
		if(msgs.length > 0) {
			StringBuffer errs = new StringBuffer();
			errs.append("Compiler problems for ");
			errs.append(qualifiedCompUnitName);
			for(Message m : msgs) {
				errs.append('\n');
				errs.append(m.getMessage());
			}
			throw new IllegalArgumentException(errs.toString());
		}
		
		return node;
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.INamed#getName()
	 */
	public String getName() {
		return getClass().getName();
	}

	public void afterAllCompilationUnits() {}

	public void beforeAllCompilationUnits() {}

	public IAnalysisInput getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	public IAnalysisReporter getReporter() {
		// TODO Auto-generated method stub
		return null;
	}
}
