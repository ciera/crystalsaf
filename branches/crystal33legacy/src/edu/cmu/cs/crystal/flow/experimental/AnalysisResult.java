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
package edu.cmu.cs.crystal.flow.experimental;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.crystal.cfg.ICFGNode;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.Lattice;
import edu.cmu.cs.crystal.flow.LatticeElement;

/**
 * Encapsulates the results of running an analysis.
 * 
 * Package private, because we'd like to avoid this being referenced 
 * throughout Crystal, but different flow analysis library classes might
 * potentially want to use this. 
 * 
 * @author Nels Beckman
 * @date Jan 24, 2008
 *
 */
public class AnalysisResult<LE extends LatticeElement<LE>> {

	private final Map<ASTNode, Set<ICFGNode>> nodeMap;
	private final Map<ICFGNode, IResult<LE>> labeledResultsAfter;
	private final Map<ICFGNode, IResult<LE>> labeledResultsBefore;
	private final Lattice<LE> lattice;
	
	private final ICFGNode cfgStartNode;
	private final ICFGNode cfgEndNode;
	
	/**
	 * Creates copies of the given maps to encapsulate a new, 
	 * un-modifiable result of an analysis. 
	 * 
	 * @param _nm
	 * @param _lra
	 * @param _lrb
	 * @param _l
	 */
	public AnalysisResult(Map<ASTNode, Set<ICFGNode>> _nm,
				Map<ICFGNode, IResult<LE>> _lra,
				Map<ICFGNode, IResult<LE>> _lrb,
				Lattice<LE> _l, ICFGNode _startNode, ICFGNode _endNode) {
		nodeMap = 
			java.util.Collections.unmodifiableMap(
					new java.util.HashMap<ASTNode, Set<ICFGNode>>(_nm));
		labeledResultsAfter = 
			java.util.Collections.unmodifiableMap(
					new java.util.HashMap<ICFGNode, IResult<LE>>(_lra));
		labeledResultsBefore =
			java.util.Collections.unmodifiableMap(
					new java.util.HashMap<ICFGNode, IResult<LE>>(_lrb));	
		lattice = _l;
		cfgStartNode = _startNode;
		cfgEndNode = _endNode;
	}

	public Map<ASTNode, Set<ICFGNode>> getNodeMap() {
		return nodeMap;
	}

	public Map<ICFGNode, IResult<LE>> getLabeledResultsAfter() {
		return labeledResultsAfter;
	}

	public Map<ICFGNode, IResult<LE>> getLabeledResultsBefore() {
		return labeledResultsBefore;
	}

	public Lattice<LE> getLattice() {
		return lattice;
	}

	public ICFGNode getCfgStartNode() {
		return this.cfgStartNode;
	}

	public ICFGNode getCfgEndNode() {
		return this.cfgEndNode;
	}
	
}
