package org.overture.alloy.test;
import java.io.File;

import org.overture.alloy.Main;

public class TelephoneOriginalTest extends Vdm2AlloyBaseTest
{
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		copyFile(new File(getAlloyInputDir(), "m2.als"), new File(getOutputDir(), "m2.als"));
	}

	public void testTelephoneOriginal() throws Exception
	{
		File vdm = new File(getInputDir(), "telephone.vdmsl");
		File output = new File(getOutputDir(), "telephone.als");
		assertEquals(Main.execute(new String[] { "-vdm", vdm.getPath(), "-o",
				output.getPath(), "-v", "-test2",
				new File(getOutputDir(), "m2.als").getPath() }), 0);
	}

}
