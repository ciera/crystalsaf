/**
 * Copyright (c) 2006-2009 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman,    
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.cmu.cs.crystal.util.Utilities;

/**
 * @author ciera
 * @since Crystal 3.4.0
 */
public class LoopCounter extends ASTVisitor {
	private int currentLoopDepth;
	private Map<ASTNode, Integer> loopDepth = Collections.emptyMap();
	private MethodDeclaration decl;
	
	public LoopCounter() {
		currentLoopDepth = -1;
	}
	
	/**
	 * @param node
	 * @return the depth of the node in loops. 0 is no loops, 1 is nested in one loop,
	 * etc.
	 */
	public int getLoopDepth(ASTNode node) {		
		if(!loopDepth.containsKey(node)) {
			final MethodDeclaration d = Utilities.getMethodDeclaration(node);
			if (d == null) {
				return 0; // not in method --> cannot be in loop
			}
			else {
				if (decl == null || !decl.equals(d))
					reset(d);
				decl.accept(this);
			}
		}
		// NEB: There's an annoying bug, and this is the only way I can
		// see how to fix it now: If 'node' is code from a nested class of
		// some kind and IT is not inside a method (e.g., it's a field 
		// initializer) then getMethodDeclaration(node) will return the
		// outer method, which we do not want. I think it is safe to return
		// 0 here, if node is not in loopDepth.
		return loopDepth.containsKey(node) ? 
				loopDepth.get(node) : 0;
	}
	
	public boolean isInLoop(ASTNode node) {
		return getLoopDepth(node) != 0;
	}
	
	private void reset(MethodDeclaration method) {
		if (method == null)
			throw new IllegalArgumentException("Need a method declaration for counting loops");
		decl = method;
		currentLoopDepth = 0;
		loopDepth = new HashMap<ASTNode, Integer>();
		loopDepth.put(method, currentLoopDepth);
	}

	
	@Override
	public boolean visit(MethodDeclaration d) {
		if (decl == null || !decl.equals(d))
			reset(d);
		return super.visit(d);
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
	public boolean visit(AnonymousClassDeclaration node) {
		// We don't want to descend into anonymous classes
		return false;
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		// We don't want to descend into local classes
		return false;
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		// We don't want to descend into local classes
		return false;
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
