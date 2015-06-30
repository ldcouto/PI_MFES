/*
 * Copyright (C) 2012 Kenneth Lausdahl
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * 
 */
package org.overture.alloy.test;
import java.io.File;

import org.overture.alloy.Main;

public class CountryColouringTest extends Vdm2AlloyBaseTest
{
	final String test2 = "countrytest.als";
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		copyFile(new File(getAlloyInputDir(), test2), new File(getOutputDir(), test2));
	}

	public void testCountryColouring() throws Exception
	{
		File vdm = new File(getInputDir(), "CountryColouring.vdmsl");
		File output = new File(getOutputDir(), "CountryColouring.als");
        System.out.println(output);
		assertEquals(Main.execute(new String[] { "-vdm", copy(vdm), "-o",
				output.getPath(), verbose, "-test2",
				new File(getOutputDir(), test2).getPath() }), 0);
	}

}
