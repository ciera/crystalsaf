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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import att.grappa.Graph;
import att.grappa.Parser;
import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.cfg.IControlFlowGraph;
import edu.cmu.cs.crystal.cfg.eclipse.CFGTestUtils;

@Deprecated
public abstract class CFGTestAnalysis extends AbstractCrystalMethodAnalysis {

	@Override
	public void analyzeMethod(MethodDeclaration method) {
		IControlFlowGraph cfg = getCFG(method);
		String className, methodName, testName;
		Graph testGraph;
		File out = null, original, projectRoot;
		FileOutputStream outStream;
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				"CrystalTest");
		projectRoot = new File(project.getLocationURI()); // project.getLocationURI
		// will return null
		// for closed
		// projects.

		try {
			className = method.resolveBinding().getDeclaringClass().getName();
			methodName = method.getName().getIdentifier();
			testName = className + "_" + methodName + ".dot";

			out = new File(projectRoot, "test/lastrun/" + testName);
			testGraph = cfg.getDotGraph();
			outStream = new FileOutputStream(out);
			testGraph.printGraph(outStream);

			original = new File(projectRoot, "test/" + testName);
			if (original.exists()) {
				Parser graphParser = new Parser(new FileInputStream(original));
				Graph realGraph;

				graphParser.parse();
				realGraph = graphParser.getGraph();
				if (!CFGTestUtils.areGraphsEqual(testGraph, realGraph))
					System.err.println("Failed test: " + testName);
				else
					System.out.println("Passed test: " + testName);
			} else {
				System.out.println("(Did not check test: " + testName + ")");
			}
			outStream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("error from: " + out.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("error from: " + out.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error from: " + out.getAbsolutePath());
		}
	}

	abstract public IControlFlowGraph getCFG(MethodDeclaration method);

}
