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
package edu.cmu.cs.crystal.analysis.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.internal.Utilities;

public class LoopCountingAnalysis extends AbstractCrystalMethodAnalysis {
	private Map<ASTNode, Integer> loopDepth;
	private MethodDeclaration decl;
	
	public int getLoopDepth(ASTNode node) {		
		if (decl == null || !decl.equals(Utilities.getMethodDeclaration(node)))
			reset(Utilities.getMethodDeclaration(node));
		return loopDepth.get(node).intValue();
	}
	
	public boolean isInLoop(ASTNode node) {
		if (decl == null || !decl.equals(Utilities.getMethodDeclaration(node)))
				reset(Utilities.getMethodDeclaration(node));
		return loopDepth.get(node).intValue() == 0;
	}
	
	@Override
	public void beforeAllMethods(ICompilationUnit compUnit,
			CompilationUnit rootNode) {
		super.beforeAllMethods(compUnit, rootNode);
	}
	
	@Override
	public void analyzeMethod(MethodDeclaration d) {
		if (decl == null || !decl.equals(d))
			reset(d);
	}
	
	private void reset(MethodDeclaration method) {
		decl = method;
		loopDepth = new HashMap<ASTNode, Integer>();
		decl.accept(new LoopCounter());
	}

	private class LoopCounter extends ASTVisitor {
		private int currentLoopDepth;
		
		public LoopCounter() {
			currentLoopDepth = 0;
		}
		
		@Override
		public void preVisit(ASTNode node) {
			loopDepth.put(node, currentLoopDepth);
		}

		@Override
		public boolean visit(DoStatement node) {
			currentLoopDepth++;
			return super.visit(node);
		}

		@Override
		public boolean visit(EnhancedForStatement node) {
			currentLoopDepth++;
			return super.visit(node);
		}

		@Override
		public boolean visit(ForStatement node) {
			currentLoopDepth++;
			return super.visit(node);
		}

		@Override
		public boolean visit(WhileStatement node) {
			currentLoopDepth++;
			return super.visit(node);
		}

		@Override
		public void endVisit(DoStatement node) {
			currentLoopDepth--;
			super.endVisit(node);
		}

		@Override
		public void endVisit(EnhancedForStatement node) {
			currentLoopDepth--;
			super.endVisit(node);
		}

		@Override
		public void endVisit(ForStatement node) {
			currentLoopDepth--;
			super.endVisit(node);
		}

		@Override
		public void endVisit(WhileStatement node) {
			currentLoopDepth--;
			super.endVisit(node);
		}
	}
}
