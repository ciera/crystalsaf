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

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.Assert;
import org.junit.Test;

import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.ThisVariable;


/**
 * @author Kevin Bierhoff
 * @since 3.3.0
 */
public class EclipseTACFieldInitTest {

	@Test
	public void testFieldInitializer() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("FieldInit", FIELD_INIT);
		MethodDeclaration c = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(c.resolveBinding());
		VariableDeclarationFragment f = EclipseTACSimpleTestDriver.getFirstField(cu);
		Expression init = f.getInitializer();
		Assert.assertNotNull(init);

		TACInstruction decl = tac.instruction(f);
		Assert.assertNotNull(decl); 
		Assert.assertTrue(decl instanceof StoreFieldInstruction);
		
		StoreFieldInstruction store = (StoreFieldInstruction) decl;
		Assert.assertTrue(store.getDestinationObject() instanceof ThisVariable);
		Assert.assertEquals("f", store.getFieldName());

		TACInstruction instr = tac.instruction(init);
		Assert.assertNotNull(instr);
		Assert.assertTrue(instr instanceof LoadLiteralInstruction);
		
		LoadLiteralInstruction load = (LoadLiteralInstruction) instr;
		Assert.assertEquals(load.getTarget(), store.getSourceOperand());
		
		Assignment write = (Assignment) ((ExpressionStatement) EclipseTACSimpleTestDriver.getLastStatement(c)).getExpression();
		TACInstruction again = tac.instruction(write);
		Assert.assertNotNull(again);
		Assert.assertTrue(again instanceof StoreFieldInstruction);
		Assert.assertFalse(store.equals(again));
	}
	
	/**
	 * Test program for {@link #testFieldInitializer()}.  The getter avoids
	 * a compiler warning about unused fields but is otherwise not used.
	 */
	private static final String FIELD_INIT = 
		"public class FieldInit {" +
		"    private int f = 5;" + 
		"    public FieldInit(int f) {" +
		"        super();" +
		"        this.f = f;" +
		"    }" +
		"    public int getF() { return f; }" +
		"}";
	
	@Test
	public void testFieldInitNew() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("FieldInitNew", FIELD_INIT_NEW);
		MethodDeclaration c = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(c.resolveBinding());
		VariableDeclarationFragment f = EclipseTACSimpleTestDriver.getFirstField(cu);
		Expression init = f.getInitializer();
		Assert.assertNotNull(init);

		TACInstruction decl = tac.instruction(f);
		Assert.assertNotNull(decl); 
		Assert.assertTrue(decl instanceof StoreFieldInstruction);
		
		StoreFieldInstruction store = (StoreFieldInstruction) decl;
		Assert.assertTrue(store.getDestinationObject() instanceof ThisVariable);
		Assert.assertEquals("f", store.getFieldName());

		TACInstruction instr = tac.instruction(init);
		Assert.assertNotNull(instr);
		Assert.assertTrue(instr instanceof NewObjectInstruction);
		
		NewObjectInstruction alloc = (NewObjectInstruction) instr;
		Assert.assertEquals(alloc.getTarget(), store.getSourceOperand());
	}
	
	/**
	 * Test program for {@link #testFieldInitNew()}.  The getter avoids
	 * a compiler warning about unused fields but is otherwise not used.
	 */
	private static final String FIELD_INIT_NEW = 
		"public class FieldInitNew {" +
		"    private final Object f = new Object();" + 
		"    public FieldInitNew() {" +
		"    }" +
		"    public Object getF() { return f; }" +
		"}";
	
	@Test
	public void testFieldNoInitializer() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("FieldNoInit", FIELD_NO_INIT);
		MethodDeclaration c = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(c.resolveBinding());
		VariableDeclarationFragment f = EclipseTACSimpleTestDriver.getFirstField(cu);
		Expression init = f.getInitializer();
		Assert.assertNull(init);

		TACInstruction decl = tac.instruction(f);
		Assert.assertNull(decl); // make sure the field declaration doesn't become a local
	}
	
	/**
	 * Test program for {@link #testFieldNoInitializer()}.  The getter avoids
	 * a compiler warning about unused fields but is otherwise not used.
	 */
	private static final String FIELD_NO_INIT = 
		"public class FieldNoInit {" +
		"    private int f;" + 
		"    public FieldNoInit() {" +
		"        this.f = 5;" +
		"    }" +
		"    public int getF() { return f; }" +
		"}";
	
}
