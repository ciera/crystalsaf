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

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.junit.Assert;
import org.junit.Test;

import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.ReturnInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.ThisVariable;


/**
 * @author Kevin Bierhoff
 *
 */
public class EclipseTACSimpleTest {

	@Test
	public void testSimpleBinop() throws Exception {
		CompilationUnit simple = EclipseTACSimpleTestDriver.parseCode("SimpleBinop", SIMPLE_BINOP);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(simple);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		InfixExpression infix = (InfixExpression) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(infix);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof BinaryOperation);
		BinaryOperation binop = (BinaryOperation) instr;
		Assert.assertEquals(tac.variable(infix.getLeftOperand()), binop.getOperand1());
		Assert.assertEquals(tac.variable(infix.getRightOperand()), binop.getOperand2());
	}
	
	private static final String SIMPLE_BINOP = 
		"public class SimpleBinop {" +
		"    public int add() {" +
		"        return 3 + 4;" +
		"    }" +
		"}";
	
	@Test
	public void testSimpleCall() throws Exception {
		CompilationUnit simple = EclipseTACSimpleTestDriver.parseCode("SimpleCall", SIMPLE_CALL);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(simple);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		MethodInvocation invoke = (MethodInvocation) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(invoke);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof MethodCallInstruction);
		MethodCallInstruction call = (MethodCallInstruction) instr;
		Assert.assertNull(((ThisVariable) tac.implicitThisVariable(m.resolveBinding())).getQualifier());
		Assert.assertEquals(tac.implicitThisVariable(m.resolveBinding()), call.getReceiverOperand());
		EclipseTACSimpleTestDriver.assertOperands(invoke.arguments(), call.getArgOperands(), tac);
		Assert.assertTrue(call.getArgOperands().get(0) instanceof SourceVariable);
		Assert.assertEquals(
				tac.sourceVariable(((SingleVariableDeclaration) m.parameters().get(0)).resolveBinding()),
				call.getArgOperands().get(0));
	}
	
	private static final String SIMPLE_CALL = 
		"public class SimpleCall {" +
		"    public int infinite(int x) {" +
		"        return infinite(x);" +
		"    }" +
		"}";
	
	@Test
	public void testStaticCall() throws Exception {
		CompilationUnit simple = EclipseTACSimpleTestDriver.parseCode("StaticCall", STATIC_CALL);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(simple);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		MethodInvocation invoke = (MethodInvocation) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(invoke);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof MethodCallInstruction);
		MethodCallInstruction call = (MethodCallInstruction) instr;
		Assert.assertEquals(tac.typeVariable(m.resolveBinding().getDeclaringClass()), call.getReceiverOperand());
		EclipseTACSimpleTestDriver.assertOperands(invoke.arguments(), call.getArgOperands(), tac);
		Assert.assertTrue(call.getArgOperands().get(0) instanceof SourceVariable);
		Assert.assertEquals(
				tac.sourceVariable(((SingleVariableDeclaration) m.parameters().get(0)).resolveBinding()),
				call.getArgOperands().get(0));
	}
	
	private static final String STATIC_CALL = 
		"public class StaticCall {" +
		"    public static int infinite(int x) {" +
		"        return infinite(x);" +
		"    }" +
		"}";
	
	@Test
	public void testSimpleReturn() throws Exception {
		CompilationUnit simple = EclipseTACSimpleTestDriver.parseCode("SimpleReturn", SIMPLE_RETURN);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(simple);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		ReturnStatement ret = (ReturnStatement) EclipseTACSimpleTestDriver.getLastStatementReturn(m);
		Assert.assertTrue(ret.getExpression() != null);
		TACInstruction instr = tac.instruction(ret);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof ReturnInstruction);
		ReturnInstruction binop = (ReturnInstruction) instr;
		Assert.assertEquals(tac.variable(ret.getExpression()), binop.getReturnedVariable());
	}
	
	private static final String SIMPLE_RETURN = 
		"public class SimpleReturn {" +
		"    public int one() {" +
		"        return 1;" +
		"    }" +
		"}";
	
}
