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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

public class SystemTestUtils {
	
//	private static final Pattern timestamp = Pattern.compile("^\\S{3} \\d\\d?, \\d{4} \\d\\d?:\\d\\d:\\d\\d [AP]M (\\w*.)+\\w* \\w*\n");
	private static final Pattern date = Pattern.compile("\\S{3} [0123]?\\d, \\d{4}");
	private static final Pattern time = Pattern.compile("[01]?\\d:[012345]\\d:[012345]\\d [AP]M");
	
	public static void assertEqualContent(File referenceFile, File compareFile) throws IOException {
		Assert.assertTrue(referenceFile.exists());
		Assert.assertTrue(compareFile.exists());
		Assert.assertTrue(referenceFile.canRead());
		Assert.assertTrue(compareFile.canRead());
		
		LineNumberReader master = new LineNumberReader(new FileReader(referenceFile));
		LineNumberReader compare = new LineNumberReader(new FileReader(compareFile));
		
		try {
			String masterLine = master.readLine();
			String compareLine = compare.readLine();
			while(masterLine != null) {
				Assert.assertNotNull("Unexpected end of file in " + compareFile + ". Expected: " + masterLine, compareLine);
				
				// replace dates and times (from logger) with dummy values
				Matcher m = date.matcher(masterLine);
				Matcher n = date.matcher(compareLine);
				m = time.matcher(m.replaceAll("Jan 01, 2008"));
				n = time.matcher(n.replaceAll("Jan 01, 2008"));
				masterLine = m.replaceAll("12:00:00 PM");
				compareLine = n.replaceAll("12:00:00 PM");
				
				Assert.assertEquals(masterLine, compareLine);
				
				masterLine = master.readLine();
				compareLine = compare.readLine();
			}
			Assert.assertNull("Additional content in " + compareFile + ": " + compareLine, compareLine);
		}
		finally {
			master.close();
			compare.close();
		}
	}

}
