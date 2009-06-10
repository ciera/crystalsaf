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

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.Assert;
import org.junit.Test;

import edu.cmu.cs.crystal.tac.model.AssignmentInstruction;
import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;


public class EclipseTACTargetSelectionTest {

	@Test
	public void testFactorial() throws Exception {
		CompilationUnit simple = EclipseTACSimpleTestDriver.parseCode("Factorial", FACTORIAL);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(simple);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		
		ConditionalExpression cond = (ConditionalExpression) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		Variable r = tac.variable(cond);
		TACInstruction instr; 

		instr = tac.instruction(cond.getExpression());
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof BinaryOperation);
		
		instr = tac.instruction(cond.getThenExpression());
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof LoadLiteralInstruction);
		Assert.assertEquals(r, ((LoadLiteralInstruction) instr).getTarget());
		Assert.assertEquals("1", ((LoadLiteralInstruction) instr).getLiteral());
		
		instr = tac.instruction(cond.getElseExpression());
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof MethodCallInstruction);
		Assert.assertEquals(r, ((MethodCallInstruction) instr).getTarget());

		Assert.assertEquals(r, tac.variable(cond.getThenExpression()));
		Assert.assertEquals(r, tac.variable(cond.getElseExpression()));
	}
	
	private static final String FACTORIAL = 
		"public class Factorial {" +
		"    public int factorial(int x) {" +
		"        return x == 0 ? 1 : this.factorial(x - 1);" +
		"    }" +
		"}";
	
	@Test
	public void testInitializers() throws Exception {
		CompilationUnit simple = EclipseTACSimpleTestDriver.parseCode("Initializers", INITIALIZERS);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(simple);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		
		List<Statement> stmts = m.getBody().statements();
		Assert.assertTrue(stmts.size() == 5);
		
		TACInstruction decl, init;
		for(int i = 0; i < 4; i++) {
			VariableDeclarationStatement s = (VariableDeclarationStatement) stmts.get(i);
			Assert.assertTrue("Statement: " + s, s.fragments().size() == 1);
			VariableDeclaration d = (VariableDeclaration) s.fragments().get(0);
			decl = tac.instruction(d);
			Assert.assertNotNull("Statement: " + s, decl);
			Assert.assertNotNull("Statement: " + s, d.getInitializer());
			if(d.getInitializer() instanceof ParenthesizedExpression)
				init = tac.instruction(((ParenthesizedExpression) d.getInitializer()).getExpression());
			else
				init = tac.instruction(d.getInitializer());
			Assert.assertNotNull("Statement: " + s, init);
			
			Assert.assertTrue("Statement: " + s, init instanceof AssignmentInstruction);
			Variable t = ((AssignmentInstruction) init).getTarget();
			
			Variable declared = null;
//			if(decl instanceof SourceVariableDeclaration) {
//				declared = ((SourceVariableDeclaration) decl).getDeclaredVariable();
//				Assert.assertEquals("Statement: " + s, declared, t);
//			}
//			else 
			if(decl instanceof EclipseInstructionSequence) {
				TACInstruction[] seq = ((EclipseInstructionSequence) decl).getInstructions();
				Assert.assertTrue("Statement: " + s, seq.length == 2);
				Assert.assertTrue("Statement: " + s, seq[0] instanceof SourceVariableDeclaration);
				declared = ((SourceVariableDeclaration) seq[0]).getDeclaredVariable();
				Assert.assertTrue("Statement: " + s, seq[1] instanceof CopyInstruction);
				Assert.assertEquals("Statement: " + s, declared, ((CopyInstruction) seq[1]).getTarget());
				Assert.assertEquals("Statement: " + s, t, ((CopyInstruction) seq[1]).getOperand());
			}
			else
				Assert.fail("Statement has unexpected translation: " + s);
			
			Assert.assertEquals("Statement: " + s, tac.sourceVariable(d.resolveBinding()), declared);
		}
		
	}
	
	private static final String INITIALIZERS = 
		"public class Initializers {" +
		"    public boolean initializing() {" +
		"        boolean a = true;" +
		"        boolean b = false;" +
		"        boolean c = (a == b);" +
		"        boolean d = b == c;" +
		"        return d;" + 
		"    }" +
		"}";
	
}
