/**
 * This package is used for flow analyses. There are five type hierarchies to notice here:
 * <ul>
 * <li>IFlowAnalysisDefinition, and it's subtypes, are the transfer functions of a flow analysis. Every flow
 * analysis will need to implement transfer functions.
 * <li>ILatticeOperations are the operations on a lattice. Every flow analysis will need to implement this.
 * <li>ILabel, and its subtypes, are only used for branch-sensitive flow analyses.
 * <li>IResult, and its subtypes, are also for branch-sensitive flow analyses.
 * <li>IFlowAnalysis, and its subtypes, will run the worklist algorithm. Unless you are creating
 * a new category of flow analyses, you do not need to extend from this hierarchy, but you will need to instantiate one
 * of these within an ICrystalAnalysis.
 * </ul>
 */
package edu.cmu.cs.crystal.flow;