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
package edu.cmu.cs.crystal.analysis.constant;

import edu.cmu.cs.crystal.simple.SimpleLatticeOperations;

/**
 * @author ciera
 * @since Crystal 3.4.0
 */
public class BooleanConstantLatticeOps extends SimpleLatticeOperations<BooleanConstantLE> {

	@Override
	public boolean atLeastAsPrecise(BooleanConstantLE left, BooleanConstantLE right) {
		if (right == left)
		{
			return true;
		}
		else if (left == BooleanConstantLE.BOTTOM)
		{
			return true;
		}
		else if (right == BooleanConstantLE.UNKNOWN)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public BooleanConstantLE bottom() {
		return BooleanConstantLE.BOTTOM;
	}

	@Override
	public BooleanConstantLE copy(BooleanConstantLE original) {
		return original;
	}

	@Override
	public BooleanConstantLE join(BooleanConstantLE left, BooleanConstantLE right) {
		if (left == right)
		{
			return left;
		}
		else if (right == BooleanConstantLE.BOTTOM)
		{
			return left;
		}
		else if (left == BooleanConstantLE.BOTTOM)
		{
			return right;
		}
		else
		{
			return BooleanConstantLE.UNKNOWN;
		}
	}
}
