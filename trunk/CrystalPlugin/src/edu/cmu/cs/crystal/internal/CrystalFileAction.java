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
package edu.cmu.cs.crystal.internal;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import edu.cmu.cs.crystal.Crystal;

public class CrystalFileAction implements IObjectActionDelegate {

	private ISelection selection;
	
	/**
	 * Constructor for Action1.
	 */
	public CrystalFileAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		List<ICompilationUnit> reanalyzeList = null;
		
		if (!selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				for (Object element : ((IStructuredSelection)selection).toList()) {
					List<ICompilationUnit> temp =
						WorkspaceUtilities.collectCompilationUnits((IJavaElement) element);
					if(temp == null)
						continue;
					if(reanalyzeList == null)
						reanalyzeList = temp;
					else
						reanalyzeList.addAll(temp);
				}
			}
		}
		
		if(reanalyzeList != null) {
			Crystal crystal = AbstractCrystalPlugin.getCrystalInstance();
			crystal.runAnalyses(reanalyzeList);
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
