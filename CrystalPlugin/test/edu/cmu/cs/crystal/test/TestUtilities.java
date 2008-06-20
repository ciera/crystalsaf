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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Assert;

import att.grappa.Edge;
import att.grappa.Graph;
import att.grappa.GrappaConstants;
import att.grappa.Node;
import att.grappa.Parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import edu.cmu.cs.crystal.Crystal;
import edu.cmu.cs.crystal.cfg.IControlFlowGraph;
import edu.cmu.cs.crystal.cfg.eclipse.EclipseCFG;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;

public class TestUtilities {
	public static boolean areGraphsEqual(Graph g1, Graph g2) {
		Vector g1Nodes = g1.vectorOfElements(GrappaConstants.NODE);
		Vector g2Nodes = g2.vectorOfElements(GrappaConstants.NODE);
		Vector g1Edges = g1.vectorOfElements(GrappaConstants.EDGE);
		Vector g2Edges = g2.vectorOfElements(GrappaConstants.EDGE);
		
		if (g1Nodes.size() != g2Nodes.size())
			return false;
		if (g1Edges.size() != g2Edges.size())
			return false;
		
		for (Node node1 : (Vector<Node>)g1Nodes) {
			Node node2 = g2.findNodeByName(node1.getName());
			
			if (node2 == null)
				return false;
		}
		
		boolean foundEdge;
		for (Edge edge1 : (Vector<Edge>)g1Edges) {
			foundEdge = false;
			for (Edge edge2 : (Vector<Edge>)g2Edges) {
				if (edge1.getTail().getName().equals(edge2.getTail().getName()) &&
				 edge1.getHead().getName().equals(edge2.getHead().getName())) {
					if (!edge1.getAttribute(Edge.LABEL_ATTR).equals(edge2.getAttribute(Edge.LABEL_ATTR)))
						return false;
					foundEdge = true;
					g2Edges.remove(edge2);
					break;
				}
			}
			if (!foundEdge)
				return false;
		}
		
		
		return true;
	}

	static public void runTest(String code, String subFolder, String file, boolean doCompare, boolean store) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		CompilationUnit node;
		Graph testGraph;
		OutputStream out;
		
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		//parser.setBindingsRecovery(true);
		parser.setSource(code.toCharArray());
		parser.setUnitName("Foo.java");
		//parser.setProject(JavaCore.create(null));
		node = (CompilationUnit)parser.createAST(null);
		
		for (IProblem problem : node.getProblems()) {
			System.out.println("Compile problem for " + subFolder + "_" + file);
			System.out.println(problem.getMessage());
		}
	
		Assert.assertEquals(0, node.getProblems().length);
		List<MethodDeclaration> methods = WorkspaceUtilities.scanForMethodDeclarationsFromAST(node);
		Assert.assertEquals(1, methods.size());
		IControlFlowGraph cfg = new EclipseCFG(methods.get(0));
		
		testGraph = cfg.getDotGraph();
		if (store) {
			out = new FileOutputStream("test/" + subFolder + "_" + file);
		}
		else {
			out = new FileOutputStream("test/lastrun/" + subFolder + "_" + file);	
		}
		testGraph.printGraph(out);
		out.close();
		
		if (doCompare) {
			InputStream original = new FileInputStream("test/" + subFolder + "_" + file);
			Parser graphParser = new Parser(original);
			Graph realGraph;
			
			graphParser.parse();
			realGraph = graphParser.getGraph();
			
			Assert.assertTrue(areGraphsEqual(testGraph, realGraph));
		}
	}


	static public void runTest(String code, String subFolder, String file) throws Exception {
		runTest(code, subFolder, file, true, false);
	}
}
