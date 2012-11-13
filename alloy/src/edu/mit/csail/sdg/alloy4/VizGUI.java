package edu.mit.csail.sdg.alloy4;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class VizGUI
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create the Options
		Options optionsArg = new Options();
		Option helpOpt = new Option("?", "help", false, "print this message");
		Option inputFileOpt = new Option("f", "file", true, "the name of the xml file to show");
		inputFileOpt.setRequired(true);

		optionsArg.addOption(helpOpt);
		optionsArg.addOption(inputFileOpt);

		CommandLine line = null;
		try
		{
			// parse the command line arguments
			line = parser.parse(optionsArg, args);

			if (line.hasOption(helpOpt.getOpt()))
			{
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("vizgui", optionsArg);
				return;
			}

		} catch (ParseException exp)
		{
			System.err.println("Unexpected exception:" + exp.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("vizgui", optionsArg);
			return;
		}

		String inputFile = null;
		if (line.hasOption(inputFileOpt.getOpt()))
		{
			inputFile = line.getOptionValue(inputFileOpt.getOpt());
			if (inputFile == null)
			{
				System.err.println("Invalid input file");
			}
		}

		new edu.mit.csail.sdg.alloy4viz.VizGUI(true, inputFile, null);

	}

}
