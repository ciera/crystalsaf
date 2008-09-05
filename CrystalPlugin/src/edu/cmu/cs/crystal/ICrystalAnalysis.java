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

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.annotations.CrystalAnnotation;
import edu.cmu.cs.crystal.internal.Option;

/**
 * Presents the interface for an analysis.
 * 
 * @author David Dickey
 * @author Jonathan Aldrich
 * 
 */
public interface ICrystalAnalysis {
	/**
	 * Run the analysis!
	 * @param reporter The object that is used to report errors. Output.
	 * @param input The input to this analysis.
	 * @param compUnit The compilation unit
	 * @param rootNode The root ASTNode of the compilation unit
	 */
	public void runAnalysis(IAnalysisReporter reporter,	
			IAnalysisInput input, ICompilationUnit compUnit, 
			CompilationUnit rootNode);
	
	/**
	 * @return a unique name for this analysis
	 */
	public String getName();
	
	/**
	 * Get any annotations to register
	 * @return A map of fully qualified names for annotations to the class which should
	 * be instantiated when we find this annotation.
	 * @deprecated Use CrystalAnnotation extension point instead.
	 */
	public Map<String, Class<? extends CrystalAnnotation>> getAnnotationClasses();
}
