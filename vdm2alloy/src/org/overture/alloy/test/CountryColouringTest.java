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
		assertEquals(Main.execute(new String[] { "-vdm", vdm.getPath(), "-o",
				output.getPath(), "-v", "-test2",
				new File(getOutputDir(), test2).getPath() }), 0);
	}

}
