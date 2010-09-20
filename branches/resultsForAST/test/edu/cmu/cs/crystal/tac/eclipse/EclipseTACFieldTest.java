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

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.junit.Assert;
import org.junit.Test;

import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.ThisVariable;
import edu.cmu.cs.crystal.tac.model.Variable;


/**
 * @author kbierhof
 *
 */
public class EclipseTACFieldTest {

	@Test
	public void testReadField() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("FieldRead", FIELD_READ);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		Expression read = (Expression) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(read);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof LoadFieldInstruction);
		LoadFieldInstruction load = (LoadFieldInstruction) instr;
		Assert.assertTrue(load.getSourceObject() instanceof ThisVariable);
		Assert.assertTrue(load.getSourceObject().isUnqualifiedThis());
		Assert.assertNull(((ThisVariable) load.getSourceObject()).getQualifier());
		Assert.assertEquals("f", load.getFieldName());
	}
	
	private static final String FIELD_READ = 
		"public class FieldRead {" +
		"    private int f;" + 
		"    public int readF() {" +
		"        return f;" +
		"    }" +
		"}";
	
	@Test
	public void testReadThis() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("ThisRead", THIS_READ);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		FieldAccess read = (FieldAccess) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(read);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof LoadFieldInstruction);
		LoadFieldInstruction load = (LoadFieldInstruction) instr;
		Assert.assertTrue(load.getSourceObject() instanceof ThisVariable);
		Assert.assertTrue(load.getSourceObject().isUnqualifiedThis());
		Assert.assertNull(((ThisVariable) load.getSourceObject()).getQualifier());
		Assert.assertEquals(tac.variable(read.getExpression()), load.getSourceObject());
		Assert.assertEquals("f", load.getFieldName());
	}
	
	private static final String THIS_READ = 
		"public class ThisRead {" +
		"    private int f;" + 
		"    public int readThisF() {" +
		"        return this.f;" +
		"    }" +
		"}";
	
	@Test
	public void testObjectRead() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("ObjectRead", OBJECT_READ);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		Expression read = (Expression) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(read);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof LoadFieldInstruction);
		LoadFieldInstruction load = (LoadFieldInstruction) instr;
		Assert.assertEquals(
				tac.sourceVariable(((SingleVariableDeclaration) m.parameters().get(0)).resolveBinding()),
				load.getSourceObject());
		Assert.assertEquals("f", load.getFieldName());
	}
	
	private static final String OBJECT_READ = 
		"public class ObjectRead {" +
		"    private int f;" + 
		"    public int readFromX(ObjectRead o) {" +
		"        return o.f;" +
		"    }" +
		"}";
	
	@Test
	public void testFieldWrite() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("FieldWrite", FIELD_WRITE);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		Assignment write = (Assignment) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(write);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof StoreFieldInstruction);
		
		StoreFieldInstruction store = (StoreFieldInstruction) instr;
		Assert.assertTrue(store.getDestinationObject() instanceof ThisVariable);
		Assert.assertTrue(store.getDestinationObject().isUnqualifiedThis());
		Assert.assertEquals(
				tac.sourceVariable(((SingleVariableDeclaration) m.parameters().get(0)).resolveBinding()),
				store.getSourceOperand());
		Assert.assertEquals("f", store.getFieldName());
		
		Assert.assertEquals(tac.variable(write.getRightHandSide()), store.getSourceOperand());
		
		// Make sure there's no FieldLoad generated for the assigned-to field 
		Assert.assertNull(tac.instruction(write.getLeftHandSide()));
	}
	
	/**
	 * Test program for {@link #testFieldWrite()}.  The getter avoids
	 * a compiler warning about unused fields but is otherwise not used.
	 */
	private static final String FIELD_WRITE = 
		"public class FieldWrite {" +
		"    private int f;" + 
		"    public int writeF(int newF) {" +
		"        return f = newF;" +
		"    }" +
		"    public int getF() { return f; }" +
		"}";
	
	@Test
	public void testFieldInc() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("FieldInc", FIELD_INC);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		PostfixExpression inc = (PostfixExpression) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(inc);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof EclipseInstructionSequence);
		LoadFieldInstruction load = (LoadFieldInstruction) tac.instruction(inc.getOperand());
		
		EclipseInstructionSequence seq = (EclipseInstructionSequence) instr;
		Assert.assertEquals(
				"Wrong number of instructions in sequence: " + seq.getInstructions().length, 
				4, seq.getInstructions().length);
		Assert.assertTrue(seq.getInstructions()[0] instanceof CopyInstruction);
		Assert.assertTrue(seq.getInstructions()[1] instanceof LoadLiteralInstruction);
		Assert.assertTrue(seq.getInstructions()[2] instanceof BinaryOperation);
		Assert.assertTrue(seq.getInstructions()[3] instanceof StoreFieldInstruction);
		CopyInstruction copy = (CopyInstruction) seq.getInstructions()[0];
		LoadLiteralInstruction one = (LoadLiteralInstruction) seq.getInstructions()[1];
		BinaryOperation add = (BinaryOperation) seq.getInstructions()[2];
		StoreFieldInstruction store = (StoreFieldInstruction) seq.getInstructions()[3];
		Assert.assertEquals(load.getTarget(), add.getOperand1());
		Assert.assertEquals(one.getTarget(), add.getOperand2());
		Assert.assertEquals(add.getTarget(), store.getSourceOperand());
		Assert.assertEquals(copy.getTarget(), seq.getResultVariable());
		Assert.assertFalse(add.getTarget().equals(seq.getResultVariable()));
		
		Assert.assertEquals(load.getTarget(), copy.getOperand());
		
		Assert.assertTrue(load.resolveFieldBinding().equals(store.resolveFieldBinding()));
	}
	
	private static final String FIELD_INC = 
		"public class FieldInc {" +
		"    private int f;" + 
		"    public int incF() {" +
		"        return f++;" +
		"    }" +
		"}";
	
	@Test
	public void testNestedFieldWrite() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("NestedFieldWrite", NESTED_FIELD_WRITE);
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
		Assert.assertTrue(instr instanceof StoreFieldInstruction);
		StoreFieldInstruction store = (StoreFieldInstruction) instr;
		
		Assert.assertTrue(store.getDestinationObject() instanceof ThisVariable);
		Assert.assertEquals("o", store.getFieldName());
		Assert.assertEquals(tac.variable(write.getRightHandSide()), store.getSourceOperand());
		Assert.assertEquals(tac.variable(write), store.getSourceOperand());
		Assert.assertEquals(args.get(0), store.getSourceOperand());
		
		// Make sure there's no FieldLoad generated for the assigned-to field 
		Assert.assertNull(tac.instruction(write.getLeftHandSide()));
	}
	
	/**
	 * Test program for {@link #testNestedFieldWrite()}.  The getter avoids
	 * a compiler warning about unused fields but is otherwise not used.
	 */
	private static final String NESTED_FIELD_WRITE = 
		"public class NestedFieldWrite {" +
		"    private Object o;" + 
		"    public void test1() {" +
		"        foo(o = null);" +
		"    }" +
		"    private void foo(Object o) { }" +
		"    public Object getO() { return o; }" +
		"}";
	
	@Test
	public void testPrivateOuterField() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("PrivateOuterField", PRIVATE_OUTER_FIELD);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu); // method in inner class
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		Expression read = (Expression) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(read);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof LoadFieldInstruction);
		LoadFieldInstruction load = (LoadFieldInstruction) instr;
		Assert.assertTrue(load.getSourceObject() instanceof ThisVariable);
		Assert.assertFalse(load.getSourceObject().isUnqualifiedThis());
		Assert.assertEquals("f", load.getFieldName());
	}
	
	private static final String PRIVATE_OUTER_FIELD = 
		"public class PrivateOuterField {" +
		"    private int f;" + 
		"    public class ReadsOuter extends PrivateOuterField {" +
		"        public int getF() { return f; }" +
		"    }" +
		"}";
	
	@Test
	public void testVisibleOuterField() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("VisibleOuterField", VISIBLE_OUTER_FIELD);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu); // method in inner class
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		Expression read = (Expression) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(read);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof LoadFieldInstruction);
		LoadFieldInstruction load = (LoadFieldInstruction) instr;
		Assert.assertTrue(load.getSourceObject() instanceof ThisVariable);
		Assert.assertTrue(load.getSourceObject().isUnqualifiedThis());
		Assert.assertNull(((ThisVariable) load.getSourceObject()).getQualifier());
		Assert.assertEquals("f", load.getFieldName());
	}
	
	private static final String VISIBLE_OUTER_FIELD = 
		"public class VisibleOuterField {" +
		"    protected int f;" + 
		"    public class ReadsInner extends VisibleOuterField {" +
		"        public int getF() { return f; }" +
		"    }" +
		"}";
	
}
