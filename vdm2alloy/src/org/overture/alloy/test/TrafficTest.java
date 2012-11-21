package org.overture.alloy.test;
import java.io.File;

import org.overture.alloy.Main;

public class TrafficTest extends Vdm2AlloyBaseTest
{
	final String test2 = "trafficTest.als";
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		copyFile(new File(getAlloyInputDir(), test2), new File(getOutputDir(), test2));
	}

	public void testTraffic() throws Exception
	{
		File vdm = new File(getInputDir(), "traffic.vdmsl");
		File output = new File(getOutputDir(), "traffic.als");
		assertEquals(Main.execute(new String[] { "-vdm", vdm.getPath(), "-o",
				output.getPath(), "-v", "-test2",
				new File(getOutputDir(), test2).getPath() }), 0);
	}

}
