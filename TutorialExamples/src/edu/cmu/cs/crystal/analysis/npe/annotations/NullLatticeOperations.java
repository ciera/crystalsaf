package edu.cmu.cs.crystal.analysis.npe.annotations;

import edu.cmu.cs.crystal.simple.SimpleLatticeOperations;

/**
 * The lattice operations for a null lattice. As NullLatticeElement is an enum,
 * we can directly compare references and do not need to clone anything.
 * 
 * @author ciera
 *
 */
public class NullLatticeOperations extends SimpleLatticeOperations<NullLatticeElement> {

	@Override
	public boolean atLeastAsPrecise(NullLatticeElement left,
			NullLatticeElement right) {
		if (left == right)
			return true;
		else if (left == NullLatticeElement.BOTTOM)
			return true;
		else if (right == NullLatticeElement.MAYBE_NULL)
			return true;
		else
			return false;
	}

	@Override
	public NullLatticeElement bottom() {
		return NullLatticeElement.BOTTOM;
	}

	@Override
	public NullLatticeElement copy(NullLatticeElement original) {
		return original;
	}

	@Override
	public NullLatticeElement join(NullLatticeElement left,
			NullLatticeElement right) {
		if (left == right)
			return left;
		else if (left == NullLatticeElement.BOTTOM)
			return right;
		else if (right == NullLatticeElement.BOTTOM)
			return left;
		else 
			return NullLatticeElement.MAYBE_NULL;
	}

}
