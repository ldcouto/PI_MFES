package org.overture.alloy.test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import junit.framework.TestCase;

public abstract class Vdm2AlloyBaseTest extends TestCase
{

	public File getOutputDir()
	{
		File output = new File("generated");
		output = new File(output, this.getName().substring(this.getName().toLowerCase().indexOf("test")
				+ "test".length()));
		output.mkdirs();

		return output;
	}

	public File getInputDir()
	{
		File input = new File("specifications/" + getTestName()
				+ "/vdm/".replace('/', File.separatorChar));
		return input;
	}

	public File getAlloyInputDir()
	{
		File input = new File("specifications/" + getTestName()
				+ "/alloy/".replace('/', File.separatorChar));
		return input;
	}

	public String getTestName()
	{
		return this.getName().substring(this.getName().toLowerCase().indexOf("test")
				+ "test".length());
	}

	@Override
	protected void setUp() throws Exception
	{
		copyFile(new File("specifications/vdmutil.als"), new File(getOutputDir(), "vdmutil.als"));
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException
	{
		if (!destFile.exists())
		{
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try
		{
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally
		{
			if (source != null)
			{
				source.close();
			}
			if (destination != null)
			{
				destination.close();
			}
		}
	}


}
