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
package edu.cmu.cs.crystal.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Assert;

import att.grappa.Graph;
import att.grappa.Parser;
import edu.cmu.cs.crystal.cfg.IControlFlowGraph;
import edu.cmu.cs.crystal.cfg.eclipse.CFGTestUtils;
import edu.cmu.cs.crystal.cfg.eclipse.EclipseNodeFirstCFG;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;

@Deprecated
public class TestUtilities {

	static public void runTest(String code, String subFolder, String file,
			boolean doCompare, boolean store) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		CompilationUnit node;
		Graph testGraph;
		OutputStream out;

		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		// parser.setBindingsRecovery(true);
		parser.setSource(code.toCharArray());
		parser.setUnitName("Foo.java");
		// parser.setProject(JavaCore.create(null));
		node = (CompilationUnit) parser.createAST(null);

		for (IProblem problem : node.getProblems()) {
			System.out.println("Compile problem for " + subFolder + "_" + file);
			System.out.println(problem.getMessage());
		}

		Assert.assertEquals(0, node.getProblems().length);
		List<MethodDeclaration> methods = WorkspaceUtilities
				.scanForMethodDeclarationsFromAST(node);
		Assert.assertEquals(1, methods.size());
		IControlFlowGraph cfg = new EclipseNodeFirstCFG(methods.get(0));

		testGraph = cfg.getDotGraph();
		if (store) {
			out = new FileOutputStream("test/" + subFolder + "_" + file);
		} else {
			out = new FileOutputStream("test/lastrun/" + subFolder + "_" + file);
		}
		testGraph.printGraph(out);
		out.close();

		if (doCompare) {
			InputStream original = new FileInputStream("test/" + subFolder
					+ "_" + file);
			Parser graphParser = new Parser(original);
			Graph realGraph;

			graphParser.parse();
			realGraph = graphParser.getGraph();

			Assert
					.assertTrue(CFGTestUtils.areGraphsEqual(testGraph,
							realGraph));
		}
	}

	static public void runTest(String code, String subFolder, String file)
			throws Exception {
		runTest(code, subFolder, file, true, false);
	}
}
