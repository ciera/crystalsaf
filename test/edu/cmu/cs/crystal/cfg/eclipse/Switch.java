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
public class Switch {
	// Assume that we are using a class where every method has a unique name
	static Map<String, MethodDeclaration> methods = new HashMap<String, MethodDeclaration>();

	@BeforeClass
	static public void setup() throws CoreException {
		CompilationUnit compUnit = CFGTestUtils.parseCode(Switch.class
				.getName());
		methods = CFGTestUtils.createMethodNameMap(compUnit);
	}

	@Test
	public void simpleTest() throws Exception {
		MethodDeclaration decl = methods.get("simple");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void simple(int switcher, int foo) {
		switch (switcher) {
		case 0:
			foo++;
			break;
		case 1:
			foo--;
			break;
		default:
			foo *= 2;
		}
	}

	@Test
	public void noDefaultTest() throws Exception {
		MethodDeclaration decl = methods.get("noDefault");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void noDefault(int switcher, int foo) {
		switch (switcher) {
		case 0:
			foo++;
			break;
		case 1:
			foo--;
			break;
		}
	}

	@Test
	public void multiCaseTest() throws Exception {
		MethodDeclaration decl = methods.get("multiCase");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void multiCase(int switcher, int foo) {
		switch (switcher) {
		case 0:
		case 1:
		case 2:
			foo++;
			break;
		case 3:
		default:
			foo--;
			break;
		}
	}

	@Test
	public void runThroughTest() throws Exception {
		MethodDeclaration decl = methods.get("runThrough");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void runThrough(int switcher, int foo) {
		switch (switcher) {
		case 0:
		case 1:
			switcher++;
		case 2:
			foo++;
			break;
		case 3:
			switcher /= 3;
		default:
			foo--;
		}
	}

	@Test
	public void emptyTest() throws Exception {
		MethodDeclaration decl = methods.get("empty");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void empty(int switcher, int foo) {
		switch (switcher) {
		}
	}

	@Test
	public void onlyDefaultTest() throws Exception {
		MethodDeclaration decl = methods.get("onlyDefault");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void onlyDefault(int switcher, int foo) {
		switch (switcher) {
		default:
			foo++;
		}
	}

}
