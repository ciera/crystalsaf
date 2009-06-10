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
package edu.cmu.cs.crystal.tac.eclipse;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.tac.model.TACInstruction;

/**
 * @author Kevin
 *
 */
public class EclipseTACTestRunAnalysis extends AbstractCrystalMethodAnalysis {
	
	private static final Logger log = Logger.getLogger(EclipseTACTestRunAnalysis.class.getName());

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis#analyzeMethod(org.eclipse.jdt.core.dom.MethodDeclaration)
	 */
	@Override
	public void analyzeMethod(MethodDeclaration d) {
		final EclipseTAC tac = new EclipseTAC(d.resolveBinding());
		d.getBody().accept(new ASTVisitor() {

			/* (non-Javadoc)
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#postVisit(org.eclipse.jdt.core.dom.ASTNode)
			 */
			@Override
			public void postVisit(ASTNode node) {
				try {
					TACInstruction instr = tac.instruction(node);
					if(log.isLoggable(Level.FINE)) {
						log.fine("Node: " + node);
						log.fine("Instruction: " + instr);
					}
				}
				catch(Throwable t) {
					log.log(Level.SEVERE, "Exception in node: " + node, t);
				}
				super.postVisit(node);
			}

		});
	}

}
