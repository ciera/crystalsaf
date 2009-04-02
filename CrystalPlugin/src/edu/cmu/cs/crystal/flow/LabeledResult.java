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
package edu.cmu.cs.crystal.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.crystal.ILabel;

/**
 * @author Kevin Bierhoff
 *
 */
public class LabeledResult<LE> implements IResult<LE> {
	private Map<ILabel, LE> labelMap;
	private LE defaultValue;
	
	/**
	 * Create a result for the given labels with the given default value.
	 * @param <LE>
	 * @param labels
	 * @param defaultValue
	 * @return A new result for the given labels with the given default value.
	 */
	public static <LE> LabeledResult<LE> 
	createResult(List<ILabel> labels, LE defaultValue) {
		return new LabeledResult<LE>(labels, defaultValue);
	}
	
	public LabeledResult(List<ILabel> labels, LE defaultValue) {
		this(defaultValue);
		/*
		 * Each label will also have defaultValue as the default value.
		 */
		for( ILabel lab : labels ) {
			this.labelMap.put(lab, defaultValue);
		}
	}
	
	public LabeledResult(LE defaultValue) {
		labelMap = new HashMap<ILabel, LE>();
		this.defaultValue = defaultValue;
	}
	
	public void put(ILabel label, LE value) {
		labelMap.put(label, value);
	}
	
	public LE get(ILabel label) {
		LE value = labelMap.get(label);
		if (value == null)
			value = defaultValue;
		return value;
	}

	public Set<ILabel> keySet() {
		return Collections.unmodifiableSet(labelMap.keySet());
	}

	public IResult<LE> join(IResult<LE> otherResult, IAbstractLatticeOperations<LE, ?> op) {
		LE otherLattice, thisLattice;
		LabeledResult<LE> mergedResult;
		Set<ILabel> mergedLabels = new HashSet<ILabel>();
		Set<ILabel> otherLabels = otherResult.keySet();
		
		mergedLabels.addAll(keySet());
		mergedLabels.addAll(otherResult.keySet());

		otherLattice = op.copy(otherResult.get(null));
		mergedResult = new LabeledResult<LE>(op.join(op.copy(defaultValue), otherLattice, null));
		
		for (ILabel label : mergedLabels) {
			if (otherLabels.contains(label) && labelMap.containsKey(label)) {
				otherLattice = op.copy(otherResult.get(label));
				thisLattice = op.copy(get(label));
				mergedResult.put(label, op.join(thisLattice, otherLattice, null));
			}
			else if (otherLabels.contains(label)) {
				otherLattice = op.copy(otherResult.get(label));
				mergedResult.put(label, otherLattice);
			}
			else {
				thisLattice = op.copy(get(label));
				mergedResult.put(label, thisLattice);
			}
		}
		
		return mergedResult;
	}

}

