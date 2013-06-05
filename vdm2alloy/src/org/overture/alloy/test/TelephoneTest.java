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
