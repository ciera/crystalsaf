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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import att.grappa.Edge;
import att.grappa.Graph;
import att.grappa.GrappaConstants;
import att.grappa.Node;
import att.grappa.Parser;
import edu.cmu.cs.crystal.cfg.IControlFlowGraph;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;

public class CFGTestUtils {
	static final String PROJECT = "CrystalPlugin";
	private static final Logger log = Logger.getLogger(CFGTestUtils.class
			.getName());

	static public CompilationUnit parseCode(String qualifiedCompUnitName)
			throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				PROJECT);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		CompilationUnit node;

		project.open(null);

		IJavaProject javaProject = JavaCore.create(project);
		ICompilationUnit source = javaProject.findType(qualifiedCompUnitName)
				.getCompilationUnit();

		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source);
		parser.setResolveBindings(true);
		node = (CompilationUnit) parser.createAST(null);

		return node;
	}

	static public boolean testAndCompareCFG(MethodDeclaration method)
			throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				PROJECT);
		IControlFlowGraph cfg = new EclipseNodeFirstCFG(method);
		String className, methodName, testName;
		Graph testGraph;
		File out = null, original, projectRoot;
		FileOutputStream outStream;
		projectRoot = new File(project.getLocationURI());

		className = method.resolveBinding().getDeclaringClass().getName();
		methodName = method.getName().getIdentifier();
		testName = className + "_" + methodName + ".dot";

		out = new File(projectRoot, "test/dotFiles/cfg/last/" + testName);
		testGraph = cfg.getDotGraph();
		outStream = new FileOutputStream(out);

		try {
			testGraph.printGraph(outStream);
			original = new File(projectRoot, "test/dotFiles/cfg/saved/"
					+ testName);
			if (!original.exists())
				throw new FileNotFoundException(original.getAbsolutePath());

			Parser graphParser = new Parser(new FileInputStream(original));
			Graph realGraph;

			graphParser.parse();
			realGraph = graphParser.getGraph();
			return areGraphsEqual(testGraph, realGraph);
		} finally {
			outStream.close();
		}
	}

	public static boolean areGraphsEqual(Graph g1, Graph g2) {
		Vector g1Nodes = g1.vectorOfElements(GrappaConstants.NODE);
		Vector g2Nodes = g2.vectorOfElements(GrappaConstants.NODE);
		Vector g1Edges = g1.vectorOfElements(GrappaConstants.EDGE);
		Vector g2Edges = g2.vectorOfElements(GrappaConstants.EDGE);

		if (g1Nodes.size() != g2Nodes.size()) {
			log.info("different number of nodes: " + g1Nodes.size() + " "
					+ g2Nodes.size());
			return false;
		}
		if (g1Edges.size() != g2Edges.size()) {
			log.info("different number of edges: " + g1Edges.size() + " "
					+ g2Edges.size());
			return false;
		}

		for (Node node1 : (Vector<Node>) g1Nodes) {
			Node node2 = g2.findNodeByName(node1.getName());

			if (node2 == null) {
				log.info("G2 did not have node with name " + node1.getName());
				return false;
			}
		}

		boolean foundEdge;
		for (Edge edge1 : (Vector<Edge>) g1Edges) {
			foundEdge = false;
			for (Edge edge2 : (Vector<Edge>) g2Edges) {
				if (edge1.getTail().getName().equals(edge2.getTail().getName())
						&& edge1.getHead().getName().equals(
								edge2.getHead().getName())
						&& edge1.getAttribute(Edge.LABEL_ATTR).equals(
								edge2.getAttribute(Edge.LABEL_ATTR))) {
					foundEdge = true;
					g2Edges.remove(edge2);
					break;
				}
			}
			if (!foundEdge) {
				log.info("did not find edge with head "
						+ edge1.getHead().getName() + " and tail "
						+ edge1.getTail().getName() + " and label "
						+ edge1.getAttribute(Edge.LABEL_ATTR).getStringValue());
				return false;
			}
		}

		return true;
	}

	public static Map<String, MethodDeclaration> createMethodNameMap(
			CompilationUnit compUnit) {
		Map<String, MethodDeclaration> methods = new HashMap<String, MethodDeclaration>();
		List<MethodDeclaration> decls = WorkspaceUtilities
				.scanForMethodDeclarationsFromAST(compUnit);

		for (MethodDeclaration decl : decls)
			methods.put(decl.getName().getIdentifier(), decl);
		return methods;
	}

}
