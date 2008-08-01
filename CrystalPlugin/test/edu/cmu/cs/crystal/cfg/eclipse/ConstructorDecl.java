/**
 * Copyright (c) 2006, 2007, 2008 Jonathan Aldrich, Nels E. Beckman, Kevin Bierhoff, David Dickey,
 * Ciera Jaspan, Thomas LaToza, Gabriel Zenarosa, and others.
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
package edu.cmu.cs.crystal.cfg.eclipse;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.cmu.cs.crystal.internal.WorkspaceUtilities;

/**
 * @author Ciera Jaspan
 * 
 */
public class ConstructorDecl {
	static List<MethodDeclaration> decls;

	@BeforeClass
	public static void setup() throws CoreException {
		CompilationUnit compUnit = CFGTestUtils.parseCode(ConstructorDecl.class.getName());
		decls = WorkspaceUtilities.scanForMethodDeclarationsFromAST(compUnit);
	}

	@Test
	public void emptyTest() throws Exception {
		for (MethodDeclaration decl : decls) {
			if (decl.isConstructor() && decl.getName().getIdentifier().equals("ConstructorEmpty")) {
				Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
				break;
			}
		}
	}

	public class ConstructorEmpty {
		String field = "foo";

		public ConstructorEmpty() {}
	}

	@Test
	public void superTest1() throws Exception {
		for (MethodDeclaration decl : decls) {
			if (decl.isConstructor() && decl.getName().getIdentifier().equals("ConstructorSub1")) {
				Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
				break;
			}
		}
	}

	public class ConstructorSub1 extends ConstructorEmpty {
		String field2 = "foo";
		String nulled;

		public ConstructorSub1(int x) {
			super();
			x++;
		}
	}

	@Test
	public void superTest2() throws Exception {
		for (MethodDeclaration decl : decls) {
			if (decl.isConstructor() && decl.getName().getIdentifier().equals("ConstructorSub2")) {
				Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
				break;
			}
		}
	}

	public class ConstructorSub2 extends ConstructorEmpty {
		String field2 = "foo";
		String field3 = "bar";
		static final String field4 = "static";

		public ConstructorSub2(int x) {
			super();
		}
	}

	@Test
	public void thisTest1() throws Exception {
		for (MethodDeclaration decl : decls) {
			if (decl.isConstructor() && decl.getName().getIdentifier().equals("ConstructorSelf1")) {
				List<SingleVariableDeclaration> params = decl.parameters();
				if (params.get(0).getName().getIdentifier().equals("dx")) {
					Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
					break;
				}
			}
		}
	}

	public class ConstructorSelf1 extends ConstructorEmpty {
		String field3 = "foo";

		public ConstructorSelf1(int x) {
			super();
		}

		public ConstructorSelf1(double dx) {
			this((int) dx);
		}
	}

	@Test
	public void thisTest2() throws Exception {
		for (MethodDeclaration decl : decls) {
			if (decl.isConstructor() && decl.getName().getIdentifier().equals("ConstructorSelf2")) {
				List<SingleVariableDeclaration> params = decl.parameters();
				if (params.get(0).getName().getIdentifier().equals("sx")) {
					Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
					break;
				}
			}
		}
	}

	public class ConstructorSelf2 extends ConstructorEmpty {
		String field3 = "foo";

		public ConstructorSelf2(int x) {
			super();
		}

		public ConstructorSelf2(String sx) {
			this(Integer.parseInt(sx));
			sx += "bar";
		}
	}
}
