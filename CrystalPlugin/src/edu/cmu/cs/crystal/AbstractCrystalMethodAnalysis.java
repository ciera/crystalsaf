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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import edu.cmu.cs.crystal.annotations.CrystalAnnotation;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;

/**
 * Responsible for carrying out the analysis logic on the methods of the target
 * program.
 * 
 * @author David Dickey
 * @author Jonathan Aldrich
 * 
 */
public abstract class AbstractCrystalMethodAnalysis implements ICrystalAnalysis {
	protected Crystal crystal;
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
	 * Carries out the analysis.
	 * <p>
	 * {@link #beforeAllMethods()} is run before any method is analyzed.<br/>
	 * Then each method is analysed by {@link #analyzeMethod(MethodDeclaration)}.<br/>
	 * Finally {@link #afterAllMethods()} is run after all methods have
	 * been analyzed.
	 */
	public final void runAnalysis(Crystal crystal, ICompilationUnit compUnit, CompilationUnit rootNode) {
		this.crystal = crystal;
		
		beforeAllMethods(compUnit, rootNode);
		
		List<MethodDeclaration> methods = WorkspaceUtilities.scanForMethodDeclarationsFromAST(rootNode);
		for (MethodDeclaration md : methods) {
			analyzeMethod(md);
		}
		
		afterAllMethods(compUnit, rootNode);
	}

	public Map<String, Class<? extends CrystalAnnotation>> getAnnotationClasses() {
		return null;
	}

	
	/**
	 * This method is invoked once before any methods are analyzed. 
	 * It can be used to perform pre-analysis functionality, if needed.
	 */
	public void beforeAllMethods(ICompilationUnit compUnit, CompilationUnit rootNode) {
	}

	/**
	 * Invoked for each method.
	 */
	public abstract void analyzeMethod(MethodDeclaration d);

	/**
	 * This method is invoked once after all methods are analyzed. 
	 * It can be used to perform post-analysis functionality, if needed.
	 */
	public void afterAllMethods(ICompilationUnit compUnit, CompilationUnit rootNode) {
	}
}
