/**
 * Copyright (c) 2006, 2007, 2008 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman, Kevin
 * Bierhoff, David Dickey, Ciera Jaspan, Thomas LaToza, Gabriel Zenarosa, and others.
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

import java.io.IOException;
import java.net.Socket;
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
public class Try {
	static private class TestException extends Exception {}

	// Assume that we are using a class where every method has a unique name
	static Map<String, MethodDeclaration> methods = new HashMap<String, MethodDeclaration>();

	@BeforeClass
	static public void setup() throws CoreException {
		CompilationUnit compUnit = CFGTestUtils.parseCode(Try.class.getName());
		methods = CFGTestUtils.createMethodNameMap(compUnit);
	}

	@Test
	public void simpleReturningTest() throws Exception {
		MethodDeclaration decl = methods.get("simpleReturning");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public int simpleReturning(String message, int foo) {
		try {
			if (foo < 3) {
				throw new Exception(message);
			}
			else
				foo++;
			return foo;
		}
		catch (Exception err) {
			return foo + 2;
		}
	}

	@Test
	public void simpleTest() throws Exception {
		MethodDeclaration decl = methods.get("simple");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void simple(String message, int foo) {
		try {
			if (foo < 3)
				throw new Exception(message);
		}
		catch (Exception err) {
			message = "blah";
		}
	}

	@Test
	public void simpleFinallyTest() throws Exception {
		MethodDeclaration decl = methods.get("simpleFinally");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void simpleFinally(String message, int foo) {
		try {
			if (foo < 3)
				throw new Exception(message);
		}
		catch (Exception err) {
			message = "blah";
		}
		finally {
			message = message + "foobar";
		}
	}

	@Test
	public void simpleFinallyReturningTest() throws Exception {
		MethodDeclaration decl = methods.get("simpleFinallyReturning");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public int simpleFinallyReturning(String message, int foo, int bar, boolean cond) {
		try {
			if (cond) {
				throw new Exception(message);
			}
			else
				return foo;
		}
		catch (Exception err) {
			return bar;
		}
		finally {
			message = "blah";
		}
	}

	@Test
	public void catchThrowsWithFinallyTest() throws Exception {
		MethodDeclaration decl = methods.get("catchThrowsWithFinally");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public int catchThrowsWithFinally(String message, int foo, int bar, boolean cond)
	    throws Exception {
		try {
			if (cond) {
				throw new TestException();
			}
			else
				return foo;
		}
		catch (TestException err) {
			throw new Exception(message);
		}
		finally {
			message = message + "blah";
		}
	}

	@Test
	public void nestTryFinallyTest() throws Exception {
		MethodDeclaration decl = methods.get("nestTryFinally");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void nestTryFinally(String message) {
		try {
			try {
				throw new TestException();
			}
			finally {
				message = message + "foo";
			}
		}
		catch (TestException err) {
			message = message + "bar";
		}
		finally {
			message = message + "blah";
		}

	}

	@Test
	public void catchUsingExceptionTest() throws Exception {
		MethodDeclaration decl = methods.get("catchUsingException");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void catchUsingException() {
		try {
			Socket sock = new Socket();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void foo() throws IOException {
		throw new IOException();
	}

	private void bar() throws IOException {
		throw new IOException();
	}

	@Test
	public void finallyHasTryTest() throws Exception {
		MethodDeclaration decl = methods.get("finallyHasTry");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	private boolean finallyHasTry() {
		try {
			foo();
			return true;
		}
		catch (IOException io) {
			return true;
		}
		finally {
			try {
				bar();
			}
			catch (IOException io) {
				return false;
			}
		}
	}

	@Test
	public void runtimeExpTest() throws Exception {
		MethodDeclaration decl = methods.get("runtimeExp");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	private void runtimeExp() {
		throw new NullPointerException();
	}

	@Test
	public void nastyNastyNasty() throws Exception {
		MethodDeclaration decl = methods.get("await");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void await() throws IOException {
		int count = 0;
		try {
			synchronized(this) {
				try {
					foo();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}
		finally {
			for (;;) {
				try {
					foo();
					count++;
					break;
				}
				catch (IOException err) {
					
				}
			}
		}
	}
}
