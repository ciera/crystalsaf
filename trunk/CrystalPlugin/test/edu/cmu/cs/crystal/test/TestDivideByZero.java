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
package edu.cmu.cs.crystal.test;

import java.util.Collections;
import java.util.Set;

/**
 * Tests the DivideByZeroAnalysis analysis. Also is a template for creating your
 * own {@code FailcessTest}.
 * 
 * @author Nels E. Beckman
 */
public class TestDivideByZero extends FailcessTest {

	@Override protected String getSeparator() { return "_"; }
	@Override protected String getTestProject() { return "CrystalTestProject"; }
	@Override protected String getFailurePrefix() { return "XXX"; }
	@Override protected Set<String> getFilesToIgnore() { 
		return Collections.singleton("/" + getTestProject() + "/src/XXX_DivideByZ_ignored.java");
	}
}
