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

public class TelephoneTest extends Vdm2AlloyBaseTest
{
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		copyFile(new File(getAlloyInputDir(), "m2.als"), new File(getOutputDir(), "m2.als"));
	}

	public void testTelephone() throws Exception
	{
		File vdm = new File(getInputDir(), "telephone.vdmsl");
		File output = new File(getOutputDir(), "telephone.als");
		assertEquals(Main.execute(new String[] { "-vdm", copy(vdm), "-o",
				output.getPath(), verbose, "-test2",
				new File(getOutputDir(), "m2.als").getPath() }), 0);
	}

	

}
