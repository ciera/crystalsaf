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
package edu.cmu.cs.crystal.tac;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.Crystal;
import edu.cmu.cs.crystal.ILabel;
import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.IBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.flow.IFlowAnalysisDefinition;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.ITACFlowAnalysis;
import edu.cmu.cs.crystal.flow.ITransferFunction;
import edu.cmu.cs.crystal.flow.LabeledSingleResult;
import edu.cmu.cs.crystal.flow.Lattice;
import edu.cmu.cs.crystal.flow.LatticeElement;
import edu.cmu.cs.crystal.flow.MotherFlowAnalysis;
import edu.cmu.cs.crystal.tac.eclipse.EclipseTAC;

/**
 * This class implements flow analyses over 3-address code instructions
 * ({@link TACInstruction}).  To define a specific analysis, implement 
 * {@link ITACTransferFunction} for conventional or 
 * {@link ITACBranchSensitiveTransferFunction} for branch-sensitive flow analyses.
 * 
 * @author Kevin Bierhoff
 *
 * @param <LE>	The LatticeElement subclass that represents the analysis knowledge
 */
public class TACFlowAnalysis<LE extends LatticeElement<LE>> 
extends MotherFlowAnalysis<LE> implements ITACFlowAnalysis<LE>, ITACAnalysisContext {
	
	private AbstractTACAnalysisDriver<LE, ?> driver;
	
	/**
	 * Creates a branch insensitive flow analysis object.
	 * @param transferFunction
	 */
	public TACFlowAnalysis(ITACTransferFunction<LE> transferFunction) {
		super();
		this.driver = new BranchInsensitiveTACAnalysisDriver<LE>(transferFunction);
		// TODO use the driver as the analysis context
		transferFunction.setAnalysisContext(this);
	}

	/**
	 * Creates a branch sensitive flow analysis object.
	 * @param transferFunction
	 */
	public TACFlowAnalysis(ITACBranchSensitiveTransferFunction<LE> transferFunction) {
		super();
		this.driver = new BranchSensitiveTACAnalysisDriver<LE>(transferFunction);
		// TODO use the driver as the analysis context
		transferFunction.setAnalysisContext(this);
	}

	/**
	 * @param crystal
	 * @param transferFunction
	 * @deprecated Use constructor that only takes transfer function.
	 */
	@Deprecated
	public TACFlowAnalysis(Crystal crystal, ITACTransferFunction<LE> transferFunction) {
		super(crystal);
		this.driver = new BranchInsensitiveTACAnalysisDriver<LE>(transferFunction);
		// TODO use the driver as the analysis context
		transferFunction.setAnalysisContext(this);
	}

	/**
	 * @param crystal
	 * @param transferFunction
	 * @deprecated Use constructor that only takes transfer function.
	 */
	@Deprecated
	public TACFlowAnalysis(Crystal crystal, ITACBranchSensitiveTransferFunction<LE> transferFunction) {
		super(crystal);
		this.driver = new BranchSensitiveTACAnalysisDriver<LE>(transferFunction);
		// TODO use the driver as the analysis context
		transferFunction.setAnalysisContext(this);
	}

	public ASTNode getNode(Variable x, TACInstruction instruction) {
		if(x instanceof TempVariable) {
			return ((TempVariable) x).getNode();
		}
		// TODO return better node for source, type, and keyword variables
		return instruction.getNode();
	}

	/**
	 * Returns the TAC variable for a given ASTNode <i>after
	 * previously analyzing the method surrounding the given node.</i>
	 * It is the caller's responsibility to make sure to call this
	 * method only when analysis results for the surrounding method
	 * are available.
	 * @param node AST node in the previously analyzed method.
	 * @return The TAC variable for a given ASTNode.
	 */
	public Variable getVariable(ASTNode node) {
		if(hasResults(node) || findSurroundingMethod(node) == getCurrentMethod())
			return driver.tac.variable(node);
		throw new IllegalArgumentException("Not currently analyzing method surrounding node: " + node);
	}
	
	/**
	 * Returns the <b>this</b> variable for a given method <i>after
	 * previously analyzing that method.</i>
	 * It is the caller's responsibility to make sure to call this
	 * method only when analysis results for the given method
	 * are available.
	 * @param methodDecl The method for which <b>this</b> is requested.
	 * @return The <b>this</b> variable for the given method. 
	 */
	public ThisVariable getThisVariable(MethodDeclaration methodDecl) {
		if(methodDecl == null || methodDecl != getCurrentMethod())
			throw new IllegalArgumentException("Not currently analyzing method: " + methodDecl);
		return driver.tac.thisVariable();
	}
	
	public ThisVariable getThisVariable() {
		return driver.tac.thisVariable();
	}
	
	/**
	 * Returns the variable for a given parameter or local <i>after
	 * previously analyzing the method declaring the parameter or local.</i>
	 * It is the caller's responsibility to make sure to call this
	 * method only when analysis results for the declaring method 
	 * are available.
	 * @param varBinding Binding of a local or parameter.
	 * @return the variable for the given parameter or local.
	 */
	public SourceVariable getSourceVariable(IVariableBinding varBinding) {
		if(varBinding.getDeclaringMethod() == null)
			throw new IllegalArgumentException("Not a local or parameter: " + varBinding);
		if(varBinding.getDeclaringMethod().equals(getCurrentMethod().resolveBinding()) == false)
			throw new IllegalArgumentException("Not currently analyzing method declaring variable: " + varBinding);
		return driver.tac.sourceVariable(varBinding);
	}
	
	public MethodDeclaration getAnalyzedMethod() {
		return getCurrentMethod();
	}

	/**
	 * Returns the implicit <b>this</b> variable for accessing a
	 * given method or field  <i>after previously analyzing the method
	 * surrounding the access.</i>
	 * It is the caller's responsibility to make sure to call this
	 * method only when analysis results for the method surrounding the
	 * access are available.
	 * @param accessedElement
	 * @return
	 */
	public ThisVariable getImplicitThisVariable(IBinding accessedElement) {
		// TODO make sure this is only called for accesses from currently analyzed method
		return driver.tac.implicitThisVariable(accessedElement);
	}
	
	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.flow.MotherFlowAnalysis#createTransferFunction()
	 */
	@Override
	protected IFlowAnalysisDefinition<LE> createTransferFunction(MethodDeclaration method) {
		driver.switchToMethod(method);
		return driver;
	}

	protected static class AbstractTACAnalysisDriver<LE extends LatticeElement<LE>, TF extends IFlowAnalysisDefinition<LE>> 
	implements IFlowAnalysisDefinition<LE> {

		protected TF tf;
		protected EclipseTAC tac;
		
		public AbstractTACAnalysisDriver(TF tf) {
			super();
			this.tf = tf;
		}
		
		/**
		 * @param methodDecl
		 */
		public void switchToMethod(MethodDeclaration methodDecl) {
			this.tac = EclipseTAC.getInstance(methodDecl);
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.cs.crystal.flow.ITransferFunction#getAnalysisDirection()
		 */
		public AnalysisDirection getAnalysisDirection() {
			return tf.getAnalysisDirection();
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.cs.crystal.flow.ITransferFunction#getLattice(org.eclipse.jdt.core.dom.MethodDeclaration)
		 */
		public Lattice<LE> getLattice(MethodDeclaration methodDeclaration) {
			return tf.getLattice(methodDeclaration);
		}
	}
	
	protected static class BranchInsensitiveTACAnalysisDriver<LE extends LatticeElement<LE>> 
	extends AbstractTACAnalysisDriver<LE, ITACTransferFunction<LE>> 
	implements ITransferFunction<LE> {
		
		public BranchInsensitiveTACAnalysisDriver(ITACTransferFunction<LE> tf) {
			super(tf);
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.cs.crystal.flow.ITransferFunction#transfer(org.eclipse.jdt.core.dom.ASTNode, edu.cmu.cs.crystal.flow.LatticeElement)
		 */
		public LE transfer(ASTNode astNode, LE incoming) {
			LE result;
			TACInstruction instr = tac.instruction(astNode);
			if(instr == null)
				result = incoming;
			else
				result = instr.transfer(tf, incoming);
			return result;
		}
	}		
	protected static class BranchSensitiveTACAnalysisDriver<LE extends LatticeElement<LE>> 
	extends AbstractTACAnalysisDriver<LE, ITACBranchSensitiveTransferFunction<LE>> 
	implements IBranchSensitiveTransferFunction<LE> {
		
		public BranchSensitiveTACAnalysisDriver(ITACBranchSensitiveTransferFunction<LE> tf) {
			super(tf);
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.cs.crystal.flow.IBranchSensitiveTransferFunction#transfer(org.eclipse.jdt.core.dom.ASTNode, java.util.List, edu.cmu.cs.crystal.flow.LatticeElement)
		 */
		public IResult<LE> transfer(ASTNode astNode, List<ILabel> labels, LE value) {
			TACInstruction instr = tac.instruction(astNode);
			if(instr == null)
				return new LabeledSingleResult<LE>(value, labels);
			else
				return instr.transfer(tf, labels, value);
		}
		
	}

}
