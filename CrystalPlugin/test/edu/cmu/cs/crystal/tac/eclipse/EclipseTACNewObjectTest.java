/**
 * Copyright (c) 2006-2009 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman,    
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

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Assert;
import org.junit.Test;

import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.TACInstruction;

/**
 * Test cases for the handling of inner classes in {@link NewObjectInstruction}.
 * @author Kevin Bierhoff
 * @since 3.3.8
 */
public class EclipseTACNewObjectTest {

	@Test
	public void testOuter() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("MainClass", MAIN);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		ClassInstanceCreation instance = (ClassInstanceCreation) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(instance);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof NewObjectInstruction);
		NewObjectInstruction newobj = (NewObjectInstruction) instr;
		
		Assert.assertFalse(newobj.isAnonClassType());
		Assert.assertNotNull(newobj.resolveInstantiatedType());
		Assert.assertEquals(instance.resolveTypeBinding(), newobj.resolveInstantiatedType());
		
		Assert.assertFalse(newobj.hasOuterObjectSpecifier());
		Assert.assertNull(newobj.getOuterObjectSpecifierOperand());
	}
	
	private static final String MAIN = 
		"public class MainClass {" +
		"    public Object create() {" +
		"        return new MainClass();" +
		"    }" +
		"}";
	
	@Test
	public void testTopLevel() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("TopLevel", TOP_LEVEL);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		ClassInstanceCreation instance = (ClassInstanceCreation) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(instance);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof NewObjectInstruction);
		NewObjectInstruction newobj = (NewObjectInstruction) instr;
		
		Assert.assertFalse(newobj.isAnonClassType());
		Assert.assertNotNull(newobj.resolveInstantiatedType());
		Assert.assertEquals(instance.resolveTypeBinding(), newobj.resolveInstantiatedType());
		
		Assert.assertFalse(newobj.hasOuterObjectSpecifier());
		Assert.assertNull(newobj.getOuterObjectSpecifierOperand());
	}
	
	private static final String TOP_LEVEL = 
		"public class TopLevel {" +
		"    public Object create() {" +
		"        return new Outer();" +
		"    }" +
		"}" +
		"class Outer { }";
	
	@Test
	public void testInner() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("Outer", INNER);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		ClassInstanceCreation instance = (ClassInstanceCreation) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(instance);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof NewObjectInstruction);
		NewObjectInstruction newobj = (NewObjectInstruction) instr;
		
		Assert.assertFalse(newobj.isAnonClassType());
		Assert.assertNotNull(newobj.resolveInstantiatedType());
		Assert.assertEquals(instance.resolveTypeBinding(), newobj.resolveInstantiatedType());
		
		Assert.assertTrue(newobj.hasOuterObjectSpecifier());
		Assert.assertNotNull(newobj.getOuterObjectSpecifierOperand());
		Assert.assertEquals(tac.thisVariable(), newobj.getOuterObjectSpecifierOperand());
	}
	
	private static final String INNER = 
		"public class Outer {" +
		"    public Object create() {" +
		"        return new Inner();" +
		"    }" +
		"    private class Inner {" +
		"    }" +
		"}";
	
	@Test
	public void testExplicitInner() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("OuterExplicit", EXPLICIT_INNER);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		ClassInstanceCreation instance = (ClassInstanceCreation) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(instance);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof NewObjectInstruction);
		NewObjectInstruction newobj = (NewObjectInstruction) instr;
		
		Assert.assertFalse(newobj.isAnonClassType());
		Assert.assertNotNull(newobj.resolveInstantiatedType());
		Assert.assertEquals(instance.resolveTypeBinding(), newobj.resolveInstantiatedType());
		
		Assert.assertTrue(newobj.hasOuterObjectSpecifier());
		Assert.assertNotNull(newobj.getOuterObjectSpecifierOperand());
		Assert.assertEquals(tac.variable(instance.getExpression()), newobj.getOuterObjectSpecifierOperand());
	}
	
	private static final String EXPLICIT_INNER = 
		"public class OuterExplicit {" +
		"    public Object create(OuterExplicit x) {" +
		"        return x.new Inner();" +
		"    }" +
		"    private class Inner {" +
		"    }" +
		"}";
	
	@Test
	public void testStaticInner() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("OuterStaticInner", STATIC_INNER);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		ClassInstanceCreation instance = (ClassInstanceCreation) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(instance);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof NewObjectInstruction);
		NewObjectInstruction newobj = (NewObjectInstruction) instr;
		
		Assert.assertFalse(newobj.isAnonClassType());
		Assert.assertNotNull(newobj.resolveInstantiatedType());
		Assert.assertEquals(instance.resolveTypeBinding(), newobj.resolveInstantiatedType());
		
		Assert.assertFalse(newobj.hasOuterObjectSpecifier());
		Assert.assertNull(newobj.getOuterObjectSpecifierOperand());
	}
	
	private static final String STATIC_INNER = 
		"public class OuterStaticInner {" +
		"    public Object create() {" +
		"        return new Inner();" +
		"    }" +
		"    private static class Inner {" +
		"    }" +
		"}";
	
	@Test
	public void testLocal() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("OuterLocal", LOCAL);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		ClassInstanceCreation instance = (ClassInstanceCreation) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(instance);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof NewObjectInstruction);
		NewObjectInstruction newobj = (NewObjectInstruction) instr;
		
		Assert.assertFalse(newobj.isAnonClassType());
		Assert.assertNotNull(newobj.resolveInstantiatedType());
		Assert.assertEquals(instance.resolveTypeBinding(), newobj.resolveInstantiatedType());
		
		Assert.assertFalse(newobj.hasOuterObjectSpecifier());
		Assert.assertNull(newobj.getOuterObjectSpecifierOperand());
	}
	
	private static final String LOCAL = 
		"public class OuterLocal {" +
		"    public Object create() {" +
		"        final class Inner {" +
		"        }" +
		"        return new Inner();" +
		"    }" +
		"}";
	
	@Test
	public void testAnonymous() throws Exception {
		CompilationUnit cu = EclipseTACSimpleTestDriver.parseCode("OuterAnon", ANON);
		MethodDeclaration m = EclipseTACSimpleTestDriver.getFirstMethod(cu);
		EclipseTAC tac = new EclipseTAC(m.resolveBinding());
		ClassInstanceCreation instance = (ClassInstanceCreation) EclipseTACSimpleTestDriver.getLastStatementReturn(m).getExpression();
		TACInstruction instr = tac.instruction(instance);
		Assert.assertTrue(instr != null);
		Assert.assertTrue(instr instanceof NewObjectInstruction);
		NewObjectInstruction newobj = (NewObjectInstruction) instr;
		
		Assert.assertTrue(newobj.isAnonClassType());
		Assert.assertNotNull(newobj.resolveInstantiatedType());
		Assert.assertEquals(instance.resolveTypeBinding(), newobj.resolveInstantiatedType());
		
		Assert.assertFalse(newobj.hasOuterObjectSpecifier());
		Assert.assertNull(newobj.getOuterObjectSpecifierOperand());
	}
	
	private static final String ANON = 
		"public class OuterAnon {" +
		"    public Object create() {" +
		"        return new Object() {" +
		"            public int hashCode() { return -1; }" +
		"        };" +
		"    }" +
		"}";
	
}
