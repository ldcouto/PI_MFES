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
package org.overture.alloy;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.overture.alloy.ast.Part;
import org.overture.alloy.ast.Pred;
import org.overture.alloy.ast.Run;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.config.Settings;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;
import edu.mit.csail.sdg.alloy4.Terminal;

public class Main
{
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		execute(args);
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static int execute(String[] args) throws Exception
	{
		// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create the Options
		Options options = new Options();
		Option helpOpt = new Option("?", "help", false, "print this message");

		Option vdmOpt = new Option("vdm", true, "the vdm input file to translate");
		vdmOpt.setRequired(true);
		Option outputOpt = new Option("o", "output", true, "a file path used to write the translated result to");
		Option suppressAlloyCheckOpt = new Option("nocheck", "noalloycheck", false, "run alloy to on the generated file");
		Option verboseOpt = new Option("v", "verbose", false, "verbose output when generating");
		Option extraAlloyTest = new Option("test2", "alloytest2", true, "exstra model used when checking the output");

		options.addOption(helpOpt);
		options.addOption(vdmOpt);
		options.addOption(outputOpt);
		options.addOption(suppressAlloyCheckOpt);
		options.addOption(verboseOpt);
		options.addOption(extraAlloyTest);

		CommandLine line = null;
		try
		{
			// parse the command line arguments
			line = parser.parse(options, args);

			if (line.hasOption(helpOpt.getOpt()))
			{
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("vdm2alloy", options);
				return 0;
			}

		} catch (ParseException exp)
		{
			System.err.println("Unexpected exception:" + exp.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("vdm2alloy", options);
			return 1;
		}

		File input = null;
		File output = null;

		if (line.hasOption(vdmOpt.getOpt()))
		{
			input = new File(line.getOptionValue(vdmOpt.getOpt()));
		}

		if (line.hasOption(outputOpt.getOpt()))
		{
			String fileName = line.getOptionValue(outputOpt.getOpt());
			if (!fileName.endsWith(".als"))
			{
				fileName += ".als";
			}
			output = new File(fileName);
		}

		boolean verbose = line.hasOption(verboseOpt.getOpt());

		Settings.dialect = Dialect.VDM_SL;
		TypeCheckResult<List<AModuleModules>> result = TypeCheckerUtil.typeCheckSl(input);


		if (result.errors.isEmpty()) {
            File tmpFile = null;
            if (output == null) {
                tmpFile = File.createTempFile("vdm2alloy", ".als");
            } else {
                tmpFile = output;
            }



            Slicing slicing = new Slicing(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")));
             result.result.get(0).apply(slicing, new ContextSlicing());
            System.out.println(slicing.getNodeList().toString());



            Alloy2VdmAnalysis analysis = new Alloy2VdmAnalysis(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")));
            result.result.get(0).apply(analysis, new Context());




            NotAllowedTypes notA = new NotAllowedTypes(analysis.getNotAllowedTypes(),0);
            if (notA.hasnoAllowedType()) {
                System.out.println("There are some problems on the file "+input+"\n\n"+notA.toString());
            } else {

                //System.out.println(analysis.);


                // Alloy2VdmAnalysis analysis = new Alloy2VdmAnalysis(tmpFile.getName().substring(0,
                // tmpFile.getName().indexOf(".")));
                // result.result.get(0).apply(analysis);

                analysis.components.add(new Pred("show", "", ""));
                analysis.components.add(new Run("show"));

                FileWriter outFile = new FileWriter(tmpFile);
                PrintWriter out = new PrintWriter(outFile);
                for (Part string : analysis.components) {
                    if (verbose) {
                        System.out.println(string);
                    }
                    out.println(string);
                }

                out.close();

                if (!line.hasOption(suppressAlloyCheckOpt.getOpt())) {
                    System.out.println("\n------------------------------------");
                    System.out.println("Running Alloy...\n");
                    System.out.println("Temp file: " + tmpFile.getAbsolutePath());

                    System.out.println("Running Alloy on file: "
                            + tmpFile.getName());
                    int exitCode = Terminal.execute(new String[]{"-alloy",
                            tmpFile.getAbsolutePath(), "-a", "-s", "SAT4J"});
                    if (exitCode != 0) {
                        return exitCode;
                    }

                    if (line.hasOption(extraAlloyTest.getOpt())) {
                        String testInputPath = line.getOptionValue(extraAlloyTest.getOpt());
                        System.out.println("Running Alloy on file: "
                                + testInputPath);
                        Terminal.main(new String[]{"-alloy", testInputPath, "-a", "-s", "SAT4J"});
                    }
                }
            }
        }else
		{
			System.err.println("Errors in input VDM model");
			return 1;
		}
		return 0;
	}

}
