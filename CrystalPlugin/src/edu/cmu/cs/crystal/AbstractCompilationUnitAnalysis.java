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
package edu.cmu.cs.crystal;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.annotations.CrystalAnnotation;
import edu.cmu.cs.crystal.internal.Option;

/**
 * Carries out an analysis on each CompilationUnit.
 * 
 * @author David Dickey
 * 
 */
public abstract class AbstractCompilationUnitAnalysis implements ICrystalAnalysis {
	
	protected IAnalysisReporter reporter = null;
	protected IAnalysisInput analysisInput = null;
	
	/**
	 * This method is intended to be used to simply
	 * return an arbitrary name that can be used to
	 * help identify this analysis.
	 * 
	 * @return	a name
	 */
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * Newest version of runAnalysis to run on a single compilation unit.
	 * 
	 * @param compUnit The ICompilationUnit that represents the file we are analyzing
	 * @param reporter The IAnalysisReport that allows an analysis to report issues.
	 * @param rootNode The ASTNode which represents this compilation unit.
	 */
	public void runAnalysis(IAnalysisReporter reporter,
			IAnalysisInput input, ICompilationUnit compUnit, 
			CompilationUnit rootNode) {
		this.reporter = reporter;
		this.analysisInput = input;
		analyzeCompilationUnit(rootNode);
	}

	public Map<String, Class<? extends CrystalAnnotation>> getAnnotationClasses() {
		return null;
	}
	
	/**
	 * Invoked once for each compilation unit.
	 */
	public abstract void analyzeCompilationUnit(CompilationUnit d);
}
