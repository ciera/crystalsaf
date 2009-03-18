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
package edu.cmu.cs.crystal.cfg.eclipse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Ciera Jaspan
 * 
 */
public class Label {
	// Assume that we are using a class where every method has a unique name
	static Map<String, MethodDeclaration> methods = new HashMap<String, MethodDeclaration>();

	@BeforeClass
	static public void setup() throws CoreException {
		CompilationUnit compUnit = CFGTestUtils
				.parseCode(Label.class.getName());
		methods = CFGTestUtils.createMethodNameMap(compUnit);
	}

	@Test
	public void simpleTest() throws Exception {
		MethodDeclaration decl = methods.get("simple");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void simple(boolean cond, int a, int b) {
		foo: {
			a = b;
			break foo;
		}
	}

	@Test
	public void nestedTest() throws Exception {
		MethodDeclaration decl = methods.get("nested");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void nested(boolean cond, int a, int b) {
		foo: {
			bar: if (cond) {
				b = a;
			}
		}
	}

	@Test
	public void blockbreakTest() throws Exception {
		MethodDeclaration decl = methods.get("blockbreak");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void blockbreak(boolean cond, int a, int b) {
		foo: {
			bar: if (cond) {
				meth1();
				if (cond)
					break bar;
				meth2();
			}
			meth3();
		}
	}

	@Test
	public void blockbreak2Test() throws Exception {
		MethodDeclaration decl = methods.get("blockbreak2");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void blockbreak2(boolean cond) {
		foo: {
			bar: if (cond) {
				meth1();
				if (cond)
					break foo;
				meth2();
			}
			meth3();
		}
	}

	@Test
	public void blockbreak3Test() throws Exception {
		MethodDeclaration decl = methods.get("blockbreak3");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void blockbreak3(boolean cond) {
		foo: {
			bar: if (cond) {
				meth1();
				if (cond)
					break bar;
				meth2();
			} else {
				meth2();
				break bar;
			}
			meth3();
		}
	}

	@Test
	public void blockbreak4Test() throws Exception {
		MethodDeclaration decl = methods.get("blockbreak4");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void blockbreak4(boolean cond) {
		foo: {
			bar: if (cond) {
				meth1();
				if (cond)
					break bar;
				meth2();
			} else
				bar2: {
					meth2();
					break bar2;
				}
			meth3();
		}
	}

	@Test
	public void blockbreak5Test() throws Exception {
		MethodDeclaration decl = methods.get("blockbreak5");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void blockbreak5(boolean cond) {
		foo: {
			bar: if (cond) {
				meth1();
				if (cond)
					break foo;
				meth2();
			} else
				bar2: {
					meth2();
					break foo;
				}
			meth3();
		}
	}

	@Test
	public void blockbreak6Test() throws Exception {
		MethodDeclaration decl = methods.get("blockbreak6");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void blockbreak6(boolean cond) {
		foo: {
			bar: if (cond)
				subbar1: {
					meth1();
					if (cond)
						break subbar1;
					meth2();
				}
			else
				subbar2: {
					meth2();
					break subbar2;
				}
			meth3();
		}
	}

	@Test
	public void loopBreakTest() throws Exception {
		MethodDeclaration decl = methods.get("loopBreak");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void loopBreak(boolean cond) {
		foo: while (cond) {
			if (cond)
				break foo;
		}
	}

	@Test
	public void loopBreak2Test() throws Exception {
		MethodDeclaration decl = methods.get("loopBreak2");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void loopBreak2(boolean cond) {
		foo: while (cond) {
			if (cond)
				break;
		}
	}

	@Test
	public void nestedLoopBreakTest() throws Exception {
		MethodDeclaration decl = methods.get("nestedLoopBreak");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void nestedLoopBreak(boolean cond) {
		foo: while (cond) {
			bar: while (cond) {
				if (cond)
					break foo;
			}
		}
	}

	@Test
	public void nestedLoopBreak2Test() throws Exception {
		MethodDeclaration decl = methods.get("nestedLoopBreak2");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void nestedLoopBreak2(boolean cond) {
		foo: while (cond) {
			bar: while (cond) {
				if (cond)
					break bar;
			}
		}
	}

	@Test
	public void loopContinue1Test() throws Exception {
		MethodDeclaration decl = methods.get("loopContinue1");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void loopContinue1(boolean cond) {
		foo: while (cond) {
			if (cond)
				continue foo;
		}
	}

	@Test
	public void loopContinue2Test() throws Exception {
		MethodDeclaration decl = methods.get("loopContinue2");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void loopContinue2(boolean cond) {
		foo: while (cond) {
			if (cond)
				continue;
		}
	}

	@Test
	public void nestedLoopContinueTest() throws Exception {
		MethodDeclaration decl = methods.get("nestedLoopContinue");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void nestedLoopContinue(boolean cond) {
		foo: while (cond) {
			bar: while (cond) {
				if (cond)
					continue foo;
			}
		}
	}

	@Test
	public void nestedLoopContinue2Test() throws Exception {
		MethodDeclaration decl = methods.get("nestedLoopContinue2");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void nestedLoopContinue2(boolean cond) {
		foo: while (cond) {
			bar: while (cond) {
				if (cond)
					continue bar;
			}
		}
	}

	@Test
	public void labelInLoopTest() throws Exception {
		MethodDeclaration decl = methods.get("labelInLoop");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public static void labelInLoop(boolean cond) {
		for (int ndx = 0; ndx < 4; ndx++)
			foo: {
				meth1();
				if (ndx == 2)
					break foo;
				meth2();
			}
	}

	@Test
	public void nestedSwitch1Test() throws Exception {
		MethodDeclaration decl = methods.get("nestedSwitch1");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public int nestedSwitch1(boolean cond, int val) {
		foo: while (cond) {
			bar: switch (val) {
			case 0:
				return 1;
			case 4:
				continue foo;
			default:
				return 6;
			}
		}
		return 5;
	}

	@Test
	public void nestedSwitch2Test() throws Exception {
		MethodDeclaration decl = methods.get("nestedSwitch2");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public int nestedSwitch2(boolean cond, int val) {
		foo: while (cond) {
			bar: switch (val) {
			case 0:
				return 1;
			case 4:
				break bar;
			default:
				return 6;
			}
		}
		return 5;
	}

	@Test
	public void nestedSwitch3Test() throws Exception {
		MethodDeclaration decl = methods.get("nestedSwitch3");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public int nestedSwitch3(boolean cond, int val) {
		foo: while (cond) {
			bar: switch (val) {
			case 0:
				return 1;
			case 4:
				break foo;
			default:
				return 6;
			}
		}
		return 5;
	}

	private static void meth1() {
		System.out.println("meth1");
	}

	static private void meth2() {
		System.out.println("meth2");
	}

	private void meth3() {
	}

}
