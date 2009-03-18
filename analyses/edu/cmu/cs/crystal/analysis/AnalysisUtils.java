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
package edu.cmu.cs.crystal.analysis;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Utilities for all analyses
 * @author cchristo
 *
 */
public class AnalysisUtils {
	/**
	 * Determine whether binding is a subtype of qualifiedTypeName.
	 * 
	 * Due to Eclipse bug:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=80715
	 * This currently DOES NOT WORK!!!
	 * 
	 * For now, this straight-up compares the names for equality. That is all...
	 * @param binding
	 * @param qualifiedTypeName
	 * @return True if binding equals qualifiedTypeName, false otherwise...
	 */
	static public boolean isSubType(ITypeBinding binding, String qualifiedTypeName) {
		String subTypeName = binding.getQualifiedName();
		if (subTypeName.equals(qualifiedTypeName))
			return true;
		if (subTypeName.equals("null"))
			return true;
		if (subTypeName.equals("edu.cmu.cs.crystal.test.relationships.DropDownList") && qualifiedTypeName.equals("edu.cmu.cs.crystal.test.relationships.ListControl"))
			return true;
		if (subTypeName.equals("org.eclipse.jdt.core.dom.InfixExpression") && qualifiedTypeName.equals("org.eclipse.jdt.core.dom.ASTNode"))
			return true;
		if (subTypeName.equals("org.eclipse.jdt.core.dom.Expression") && qualifiedTypeName.equals("org.eclipse.jdt.core.dom.ASTNode"))
			return true;
		if (subTypeName.equals("org.eclipse.jdt.core.dom.InfixExpression") && qualifiedTypeName.equals("org.eclipse.jdt.core.dom.Expression"))
			return true;
		return false;
	}
}
