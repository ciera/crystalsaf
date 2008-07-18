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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.cmu.cs.crystal.Crystal;

/**
 * Begins the execution of the Crystal framework when the corresponding
 * GUI element is triggered.
 * 
 * See the "plugin.xml" file for the mapping between GUI element and this
 * class.
 * 
 * @author David Dickey
 */
public class CrystalUIAction implements IWorkbenchWindowActionDelegate {

	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void run(IAction action) {
		final Crystal crystal = AbstractCrystalPlugin.getCrystalInstance();
		Job j = new Job("Crystal") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Running registered Crystal analyses", IProgressMonitor.UNKNOWN);
				crystal.runAnalyses(monitor);
				if(monitor.isCanceled())
					return Status.CANCEL_STATUS;
				return Status.OK_STATUS;
			}
			
		};
		j.setUser(true);
		j.schedule();
		
//		AbstractCrystalPlugin plugin = AbstractCrystalPlugin.getInstance();
//		plugin.runCrystal();
	}
	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		
	}
	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void dispose() {
		
	}
	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
    public void init(IWorkbenchWindow window) {

    }
}
