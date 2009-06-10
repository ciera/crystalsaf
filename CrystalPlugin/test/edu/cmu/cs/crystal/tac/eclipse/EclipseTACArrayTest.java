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

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.junit.Assert;
import org.junit.Test;

import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.StoreArrayInstruction;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;

/**
 * @author kbierhof
 *
 */
public class EclipseTACArrayTest {

	@Test
	public void testArrayRead() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("ArrayRead", ARRAY_READ);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		ArrayAccess read = (ArrayAccess) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(read);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof LoadArrayInstruction);
		LoadArrayInstruction load = (LoadArrayInstruction) instr;
		
		Assert.assertEquals(tac.variable(read.getArray()), load.getSourceArray());
		Assert.assertTrue(load.getSourceArray() instanceof SourceVariable);
		Assert.assertEquals("a", load.getSourceArray().getSourceString());
		
		Assert.assertEquals(tac.variable(read.getIndex()), load.getArrayIndex());
		Assert.assertTrue(load.getArrayIndex() instanceof SourceVariable);
		Assert.assertEquals("index", load.getArrayIndex().getSourceString());
	}
	
	private static final String ARRAY_READ = 
		"public class ArrayRead {" +
		"    public int elem(int[] a, int index) {" +
		"        return a[index];" +
		"    }" +
		"}";
	
	@Test
	public void testArrayWrite() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("ArrayWrite", ARRAY_WRITE);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		Assignment write = (Assignment) ((ExpressionStatement) EclipseTACSimpleTestDriver.getLastStatement(m)).getExpression();
		TACInstruction instr = tac.instruction(write);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof StoreArrayInstruction);
		StoreArrayInstruction store = (StoreArrayInstruction) instr;

		EclipseTACSimpleTestDriver.assertMethodParameter(store.getDestinationArray(), m, 0, tac);
		EclipseTACSimpleTestDriver.assertMethodParameter(store.getArrayIndex(), m, 1, tac);		
		
		Assert.assertEquals(tac.variable(write.getRightHandSide()), store.getSourceOperand());
		EclipseTACSimpleTestDriver.assertMethodParameter(store.getSourceOperand(), m, 2, tac);		
		
		// Make sure there's no ArrayLoad generated for the assigned-to array 
		Assert.assertNull("Load generated for array assignment", tac.instruction(write.getLeftHandSide()));
	}
	
	private static final String ARRAY_WRITE = 
		"public class ArrayWrite {" +
		"    public void writeElem(int[] a, int index, int newValue) {" +
		"        a[index] = newValue;" +
		"    }" +
		"}";
	
	@Test
	public void testArrayInc() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("ArrayInc", ARRAY_INC);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		PrefixExpression inc = (PrefixExpression) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(inc);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof EclipseInstructionSequence);
		LoadArrayInstruction load = (LoadArrayInstruction) tac.instruction(inc.getOperand());
		
		EclipseInstructionSequence seq = (EclipseInstructionSequence) instr;
		Assert.assertEquals(
				"Wrong number of instructions in sequence: " + seq.getInstructions().length, 
				3, seq.getInstructions().length);
		Assert.assertTrue(seq.getInstructions()[0] instanceof LoadLiteralInstruction);
		Assert.assertTrue(seq.getInstructions()[1] instanceof BinaryOperation);
		Assert.assertTrue(seq.getInstructions()[2] instanceof StoreArrayInstruction);
		LoadLiteralInstruction one = (LoadLiteralInstruction) seq.getInstructions()[0];
		BinaryOperation add = (BinaryOperation) seq.getInstructions()[1];
		StoreArrayInstruction store = (StoreArrayInstruction) seq.getInstructions()[2];
		Assert.assertEquals(load.getTarget(), add.getOperand1());
		Assert.assertEquals(one.getTarget(), add.getOperand2());
		Assert.assertEquals(add.getTarget(), store.getSourceOperand());
		Assert.assertEquals(add.getTarget(), seq.getResultVariable());
		Assert.assertFalse(add.getTarget().equals(load.getTarget()));
	}
	
	private static final String ARRAY_INC = 
		"public class ArrayInc {" +
		"    public int preIncFirst(int[] a) {" +
		"        return ++a[0];" +
		"    }" +
		"}";
	
	@Test
	public void testNestedArrayWrite() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("NestedArrayWrite", NESTED_ARRAY_WRITE);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		MethodInvocation inv = (MethodInvocation) ((ExpressionStatement) EclipseTACSimpleTestDriver.getLastStatement(m)).getExpression();
		TACInstruction instr = tac.instruction(inv);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof MethodCallInstruction);
		MethodCallInstruction call = (MethodCallInstruction) instr;
		
		List<Variable> args = call.getArgOperands();
		EclipseTACSimpleTestDriver.assertOperands(inv.arguments(), args, tac);
		Assert.assertEquals(1, args.size());
		
		Assignment write = (Assignment) inv.arguments().get(0);
		instr = tac.instruction(write);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof StoreArrayInstruction);
		StoreArrayInstruction store = (StoreArrayInstruction) instr;

		Assert.assertEquals(tac.variable(write.getRightHandSide()), store.getSourceOperand());
		Assert.assertEquals(tac.variable(write), store.getSourceOperand());
		Assert.assertEquals(args.get(0), store.getSourceOperand());
		
		// Make sure there's no ArrayLoad generated for the assigned-to array 
		Assert.assertNull("Load generated for array assignment", tac.instruction(write.getLeftHandSide()));
	}
	
	private static final String NESTED_ARRAY_WRITE = 
		"public class NestedArrayWrite {" +
		"    public void test2()	{" +
		"        int[] ints1 = {1, 2, 3};" +
		"        foo(ints1[1] = 5);" +	
		"    }" +	
		"    private void foo(int i) { }" +
		"}";

}
