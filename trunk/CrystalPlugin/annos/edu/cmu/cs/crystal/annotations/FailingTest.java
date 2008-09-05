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
package edu.cmu.cs.crystal.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * If this interface is contained anywhere inside of a CompilationUnit, that
 * compilation unit will be analyzed as a test, and the analysis of that file
 * should produce errors or warnings. If a value is provided, and that value is
 * greater than 0, the testing framework will assert that exactly that number
 * of errors were reported. Otherwise, there must be at least one error.
 * 
 * @author Nels E. Beckman
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FailingTest {
	
	/**
	 * The number of errors expected. A value less than or equal to zero indicates
	 * that any number of repoted errors is satisfactory, as long as that number is
	 * greater than 0.
	 */
	int value() default 0;
}
