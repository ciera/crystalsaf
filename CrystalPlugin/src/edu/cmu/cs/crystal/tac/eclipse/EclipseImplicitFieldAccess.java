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

import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import edu.cmu.cs.crystal.tac.Variable;

/**
 * @author Kevin Bierhoff
 *
 */
public class EclipseImplicitFieldAccess extends EclipseAbstractFieldAccess<SimpleName>
		implements IEclipseFieldAccess {

	/**
	 * @param node
	 * @param query
	 */
	public EclipseImplicitFieldAccess(SimpleName node,
			IEclipseVariableQuery query) {
		super(node, query);
		if((node.getParent() instanceof QualifiedName) && ((QualifiedName) node.getParent()).getName() == node) {
			throw new IllegalArgumentException("Name \"" + node + "\" inside qualifier: " + node.getParent());
		}
		if((node.getParent() instanceof FieldAccess) && (((FieldAccess) node.getParent()).getName() == node)) {
			throw new IllegalArgumentException("Name \"" + node + "\" inside field access: " + node.getParent());
		}
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.tac.eclipse.IEclipseFieldAccess#getAccessedObject()
	 */
	public Variable getAccessedObject() {
		IVariableBinding field = resolveFieldBinding();
		if(field.isField())
			return query.implicitThisVariable(resolveFieldBinding());
		else if(field.isEnumConstant())
			return query.typeVariable(field.getDeclaringClass());
		else
			throw new UnsupportedOperationException("Don't know how to determine accessed object for non-field binding: " + getFieldName());
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.tac.eclipse.IEclipseFieldAccess#getFieldName()
	 */
	public SimpleName getFieldName() {
		return node;
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.tac.eclipse.IEclipseFieldAccess#resolveFieldBinding()
	 */
	public IVariableBinding resolveFieldBinding() {
		return (IVariableBinding) node.resolveBinding();
	}

	public boolean isImplicitThisAccess() {
		return !isStaticFieldAccess();
	}

	public boolean isExplicitSuperAccess() {
		return false;
	}

}
