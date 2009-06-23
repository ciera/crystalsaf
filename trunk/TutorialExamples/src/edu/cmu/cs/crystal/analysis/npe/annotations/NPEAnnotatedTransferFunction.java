package edu.cmu.cs.crystal.analysis.npe.annotations;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.annotations.AnnotationSummary;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.simple.AbstractingTransferFunction;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.simple.TupleLatticeOperations;
import edu.cmu.cs.crystal.tac.model.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;

public class NPEAnnotatedTransferFunction extends AbstractingTransferFunction<TupleLatticeElement<Variable, NullLatticeElement>> {
	/**
	 * The operations for this lattice. We want to have a tuple lattice from variables to null lattice elements, so we
	 * give it an instance of NullLatticeOperations. We also want the default value to be maybe null.
	 */
	TupleLatticeOperations<Variable, NullLatticeElement> ops =
		new TupleLatticeOperations<Variable, NullLatticeElement>(new NullLatticeOperations(), NullLatticeElement.MAYBE_NULL);
	private AnnotationDatabase annoDB;
	
	public NPEAnnotatedTransferFunction(AnnotationDatabase annoDB) {
		this.annoDB = annoDB;
	}

	/**
	 * The operations will create a default lattice which will map all variables to maybe null (since that was our default).
	 * 
	 * Of course, "this" should never be null.
	 */
	public TupleLatticeElement<Variable, NullLatticeElement> createEntryValue(
			MethodDeclaration method) {
		TupleLatticeElement<Variable, NullLatticeElement> def = ops.getDefault();
		def.put(getAnalysisContext().getThisVariable(), NullLatticeElement.NOT_NULL);
		
		AnnotationSummary summary = annoDB.getSummaryForMethod(method.resolveBinding());
		
		for (int ndx = 0; ndx < method.parameters().size(); ndx++) {
			SingleVariableDeclaration decl = (SingleVariableDeclaration) method.parameters().get(ndx);
			Variable paramVar = getAnalysisContext().getSourceVariable(decl.resolveBinding());
			
			if (summary.getParameter(ndx, AnnotatedNPEAnalysis.NON_NULL_ANNO) != null) //is this parameter annotated with @Nonnull?
				def.put(paramVar, NullLatticeElement.NOT_NULL);
		}
		
		return def;
	}

	/**
	 * Just return our lattice ops.
	 */
	public ILatticeOperations<TupleLatticeElement<Variable, NullLatticeElement>> getLatticeOperations() {
		return ops;
	}

	@Override
	public TupleLatticeElement<Variable, NullLatticeElement> transfer(
			ArrayInitInstruction instr,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		value.put(instr.getTarget(), NullLatticeElement.NOT_NULL);
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, NullLatticeElement> transfer(
			CopyInstruction instr,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		value.put(instr.getTarget(), value.get(instr.getOperand()));
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, NullLatticeElement> transfer(
			LoadLiteralInstruction instr,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		if (instr.isNull())
			value.put(instr.getTarget(), NullLatticeElement.NULL);
		else
			value.put(instr.getTarget(), NullLatticeElement.NOT_NULL);
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, NullLatticeElement> transfer(
			MethodCallInstruction instr,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		AnnotationSummary summary = annoDB.getSummaryForMethod(instr.resolveBinding());
		
		for (int ndx = 0; ndx < instr.getArgOperands().size(); ndx++) {
			Variable paramVar = instr.getArgOperands().get(ndx);
			
			if (summary.getParameter(ndx, AnnotatedNPEAnalysis.NON_NULL_ANNO) != null) //is this parameter annotated with @Nonnull?
				value.put(paramVar, NullLatticeElement.NOT_NULL);
		}
		
		if (summary.getReturn(AnnotatedNPEAnalysis.NON_NULL_ANNO) != null) 
			value.put(instr.getTarget(), NullLatticeElement.NOT_NULL);
		
		//clearly, the receiver is not null if this method call does actually occur ;)
		value.put(instr.getReceiverOperand(), NullLatticeElement.NOT_NULL);

		return value;
	}

	@Override
	public TupleLatticeElement<Variable, NullLatticeElement> transfer(
			NewArrayInstruction instr,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		value.put(instr.getTarget(), NullLatticeElement.NOT_NULL);
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, NullLatticeElement> transfer(
			NewObjectInstruction instr,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		value.put(instr.getTarget(), NullLatticeElement.NOT_NULL);
		return value;
	}
}
