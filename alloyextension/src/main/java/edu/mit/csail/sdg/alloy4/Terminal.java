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
package edu.mit.csail.sdg.alloy4;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;

public class Terminal
{
	/** The system-specific file separator (forward-slash on UNIX, back-slash on Windows, etc.) */
	private static final String fs = System.getProperty("file.separator");

	
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
		Options optionsArg = new Options();
		Option helpOpt = new Option("?", "help", false, "print this message");
		Option alloyOpt = new Option("alloy", true, "the alloy input file to check");
		alloyOpt.setRequired(true);
		Option commandOpt = new Option("c", "command", true, "the command to run");
		Option runAllCommandsOpt = new Option("a", "runall", false, "run all commands");
		Option keepResultsOpt = new Option("k", "keep", false, "keep result files");
		Option solverOpt = new Option("s", "solver", true, "name of SAT Solver. Options are: "
				+ getAvaliableSatSolvers());

		optionsArg.addOption(helpOpt);
		optionsArg.addOption(alloyOpt);
		optionsArg.addOption(commandOpt);
		optionsArg.addOption(runAllCommandsOpt);
		optionsArg.addOption(keepResultsOpt);
		optionsArg.addOption(solverOpt);

		CommandLine line = null;
		try
		{
			// parse the command line arguments
			line = parser.parse(optionsArg, args);

			if (line.hasOption(helpOpt.getOpt()))
			{
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("vdm2alloy", optionsArg);
				return 0;
			}

		} catch (ParseException exp)
		{
			System.err.println("Unexpected exception:" + exp.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("vdm2alloy", optionsArg);
			return 1;
		}

		String intputAlloyModel = null;
		if (line.hasOption(alloyOpt.getOpt()))
		{
			intputAlloyModel = line.getOptionValue(alloyOpt.getOpt());
		}

		// Setup solver and alloy libs
		copyFromJAR();

		A4Reporter rep = new A4Reporter();

		A4Options options = new A4Options();

		options.solver = A4Options.SatSolver.MiniSatProverJNI;
		if (line.hasOption(solverOpt.getOpt()))
		{
			String solverName = line.getOptionValue(solverOpt.getOpt());
			A4Options.SatSolver solver = getSolver(solverName);
			if (solver == null)
			{
				System.err.println("Solver could not be resolved with value: "
						+ solverName);
				return 1;
			}
			options.solver = solver;
		}

		Module someWorld = null;
		try
		{
			someWorld = CompUtil.parseEverything_fromFile(rep, null, intputAlloyModel);
			System.out.println();
		} catch (Err e)
		{
			System.err.println(e);
			System.err.println();
			System.err.flush();
			return 1;
		}

		List<Command> commandsToRun = new Vector<Command>();

		if (line.hasOption(commandOpt.getOpt()))
		{
			String commandName = line.getOptionValue(commandOpt.getOpt());
			for (Command command : someWorld.getAllCommands())
			{
				if (command.label.equals(commandName))
				{
					commandsToRun.add(command);
					break;
				}
			}
			if (commandsToRun.isEmpty())
			{
				System.err.println("Command: " + commandName + " not found in "
						+ someWorld.getAllCommands());
				return 1;
			}
		}

		if (commandsToRun.isEmpty()
				|| line.hasOption(runAllCommandsOpt.getOpt()))
		{
			commandsToRun.addAll(someWorld.getAllCommands());
		}

		System.out.println("Alloy Analysis");
		int i = 0;
		for (Command command : commandsToRun)
		{
			i++;
			A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, someWorld.getAllReachableSigs(), command, options);
			show(ans, commandsToRun, command, i);

			if (line.hasOption(keepResultsOpt.getOpt()))
			{
				final String tempdir = alloyHome() + fs + "binary"
						+ File.separatorChar;
				if (ans.satisfiable())
				{
					ans.writeXML(tempdir + "tmp" + i + "cnf.xml");
				}
				final File tempCNF = new File(tempdir + "tmp.cnf");
				copyFile(tempCNF, new File(tempdir + "tmp" + i + ".cnf"));
			}
		}
		System.out.println("Done.");
		return 0;
	}

	private static String getAvaliableSatSolvers()
	{
		StringBuilder sb = new StringBuilder();
		for (Field f : A4Options.SatSolver.class.getFields())
		{
			if (A4Options.SatSolver.class.isAssignableFrom(f.getType()))
			{
				sb.append(f.getName() + ", ");
			}
		}
		if (sb.length() > 2)
		{
			sb.delete(sb.length() - 2, sb.length());
		}
		return sb.toString();
	}

	private static A4Options.SatSolver getSolver(String name)
	{
		if (getAvaliableSatSolvers().contains(name))
		{
			for (Field f : A4Options.SatSolver.class.getFields())
			{
				if (f.getName().equals(name)
						&& A4Options.SatSolver.class.isAssignableFrom(f.getType()))
				{
					try
					{
						return (A4Options.SatSolver) f.get(null);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException
	{
		if (!sourceFile.exists())
		{
			return;
		}
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

	private static void show(A4Solution ai, List<Command> cmds, Command r,
			int i2)
	{
		List<String> result = new ArrayList<String>(cmds.size());
		final String tempXML = /* tempdir+File.separatorChar+i+". */alloyHome()
				+ fs + "binary" + File.separatorChar + "cnf.xml";
		final String tempCNF = /* tempdir+File.separatorChar+i+". */alloyHome()
				+ fs + "binary" + File.separatorChar + "cnf";

		if (ai == null)
			result.add(null);
		else if (ai.satisfiable())
			result.add(tempXML);
		else if (ai.highLevelCore().a.size() > 0)
			result.add(tempCNF + ".core");
		else
			result.add("");

		if (result.size() > 0)
		{
			// System.out.println( "" + result.size() + " commands were executed. The results are:\n");
			for (int i = 0; i < result.size(); i++)
			{
				// Command r=world.getAllCommands().get(i);
				if (result.get(i) == null)
				{
					System.out.print("   #" + (i2) + ": Unknown.\n");
					continue;
				}
				if (result.get(i).endsWith(".xml"))
				{
					System.out.print("   #" + (i2) + ": ");
					System.out.print(r.check ? "Counterexample found. "
							: "Instance found. "/* + "XML: "+result.get(i) */);
					System.out.print(r.label
							+ (r.check ? " is invalid" : " is consistent"));
					if (r.expects == 0)
						System.out.print(", contrary to expectation");
					else if (r.expects == 1)
						System.out.print(", as expected");
				} else if (result.get(i).endsWith(".core"))
				{
					System.out.print("   #" + (i2) + ": ");
					System.out.print(r.check ? "No counterexample found. "
							: "No instance found. " + "CORE: "/* +result.get(i) */);
					System.out.print(r.label
							+ (r.check ? " may be valid"
									: " may be inconsistent"));
					if (r.expects == 1)
						System.out.print(", contrary to expectation");
					else if (r.expects == 0)
						System.out.print(", as expected");
				} else
				{
					if (r.check)
						System.out.print("   #" + (i2)
								+ ": No counterexample found. " + r.label
								+ " may be valid");
					else
						System.out.print("   #" + (i2)
								+ ": No instance found. " + r.label
								+ " may be inconsistent");
					if (r.expects == 1)
						System.out.print(", contrary to expectation");
					else if (r.expects == 0)
						System.out.print(", as expected");
				}
				// System.out.print(".\n");
			}
			System.out.print("\n");
		}

		// System.out.print(ans.getOriginalCommand());
		// for (Field f : ans.getClass().getDeclaredFields())
		// {
		// f.setAccessible(true);
		// if (f.getName().equals("solved") && !getBoolean(f, ans))
		// {
		// System.out.print(" ---OUTCOME---Unknown\n.");
		// return;
		// } else if (f.getName().equals("eval") && getObject(f, ans) == null)
		// {
		// System.out.print(" ---OUTCOME---Unsatisfiable.\n");
		// return;
		// }
		//
		// }
		// System.out.print(" ---INSTANCE---\n");
	}

	// private static Object getObject(Field f, Object obj)
	// {
	// f.setAccessible(true);
	//
	// try
	// {
	// return f.get(obj);
	//
	// } catch (IllegalArgumentException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalAccessException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return false;
	// }

	static boolean getBoolean(Field f, Object obj)
	{
		f.setAccessible(true);

		try
		{
			return (Boolean) f.get(obj);

		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private static String alloyHome()
	{
		return new File("").getAbsolutePath();
	}

	/** Copy the required files from the JAR into a temporary directory. */
	private static void copyFromJAR()
	{
		// Compute the appropriate platform
		String os = System.getProperty("os.name").toLowerCase(Locale.US).replace(' ', '-');
		if (os.startsWith("mac-"))
			os = "mac";
		else if (os.startsWith("windows-"))
			os = "windows";
		String arch = System.getProperty("os.arch").toLowerCase(Locale.US).replace(' ', '-');
		if (arch.equals("powerpc"))
			arch = "ppc-" + os;
		else
			arch = arch.replaceAll("\\Ai[3456]86\\z", "x86") + "-" + os;
		if (os.equals("mac"))
			arch = "x86-mac"; // our pre-compiled binaries are all universal binaries
		// Find out the appropriate Alloy directory
		final String platformBinary = alloyHome() + fs + "binary";
		// Write a few test files
		try
		{
			(new File(platformBinary)).mkdirs();
			Util.writeAll(platformBinary + fs + "tmp.cnf", "p cnf 3 1\n1 0\n");
		} catch (Err er)
		{
			// The error will be caught later by the "berkmin" or "spear" test
		}
		// Copy the platform-dependent binaries
		Util.copy(true, false, platformBinary, arch + "/libminisat.so", arch
				+ "/libminisatx1.so", arch + "/libminisat.jnilib", arch
				+ "/libminisatprover.so", arch + "/libminisatproverx1.so", arch
				+ "/libminisatprover.jnilib", arch + "/libzchaff.so", arch
				+ "/libzchaffx1.so", arch + "/libzchaff.jnilib", arch
				+ "/berkmin", arch + "/spear");
		Util.copy(false, false, platformBinary, arch + "/minisat.dll", arch
				+ "/minisatprover.dll", arch + "/zchaff.dll", arch
				+ "/berkmin.exe", arch + "/spear.exe");
		// Copy the model files
		Util.copy(false, true, alloyHome(), "models/book/appendixA/addressBook1.als", "models/book/appendixA/addressBook2.als", "models/book/appendixA/barbers.als", "models/book/appendixA/closure.als", "models/book/appendixA/distribution.als", "models/book/appendixA/phones.als", "models/book/appendixA/prison.als", "models/book/appendixA/properties.als", "models/book/appendixA/ring.als", "models/book/appendixA/spanning.als", "models/book/appendixA/tree.als", "models/book/appendixA/tube.als", "models/book/appendixA/undirected.als", "models/book/appendixE/hotel.thm", "models/book/appendixE/p300-hotel.als", "models/book/appendixE/p303-hotel.als", "models/book/appendixE/p306-hotel.als", "models/book/chapter2/addressBook1a.als", "models/book/chapter2/addressBook1b.als", "models/book/chapter2/addressBook1c.als", "models/book/chapter2/addressBook1d.als", "models/book/chapter2/addressBook1e.als", "models/book/chapter2/addressBook1f.als", "models/book/chapter2/addressBook1g.als", "models/book/chapter2/addressBook1h.als", "models/book/chapter2/addressBook2a.als", "models/book/chapter2/addressBook2b.als", "models/book/chapter2/addressBook2c.als", "models/book/chapter2/addressBook2d.als", "models/book/chapter2/addressBook2e.als", "models/book/chapter2/addressBook3a.als", "models/book/chapter2/addressBook3b.als", "models/book/chapter2/addressBook3c.als", "models/book/chapter2/addressBook3d.als", "models/book/chapter2/theme.thm", "models/book/chapter4/filesystem.als", "models/book/chapter4/grandpa1.als", "models/book/chapter4/grandpa2.als", "models/book/chapter4/grandpa3.als", "models/book/chapter4/lights.als", "models/book/chapter5/addressBook.als", "models/book/chapter5/lists.als", "models/book/chapter5/sets1.als", "models/book/chapter5/sets2.als", "models/book/chapter6/hotel.thm", "models/book/chapter6/hotel1.als", "models/book/chapter6/hotel2.als", "models/book/chapter6/hotel3.als", "models/book/chapter6/hotel4.als", "models/book/chapter6/mediaAssets.als", "models/book/chapter6/memory/abstractMemory.als", "models/book/chapter6/memory/cacheMemory.als", "models/book/chapter6/memory/checkCache.als", "models/book/chapter6/memory/checkFixedSize.als", "models/book/chapter6/memory/fixedSizeMemory.als", "models/book/chapter6/memory/fixedSizeMemory_H.als", "models/book/chapter6/ringElection.thm", "models/book/chapter6/ringElection1.als", "models/book/chapter6/ringElection2.als", "models/examples/algorithms/dijkstra.als", "models/examples/algorithms/dijkstra.thm", "models/examples/algorithms/messaging.als", "models/examples/algorithms/messaging.thm", "models/examples/algorithms/opt_spantree.als", "models/examples/algorithms/opt_spantree.thm", "models/examples/algorithms/peterson.als", "models/examples/algorithms/ringlead.als", "models/examples/algorithms/ringlead.thm", "models/examples/algorithms/s_ringlead.als", "models/examples/algorithms/stable_mutex_ring.als", "models/examples/algorithms/stable_mutex_ring.thm", "models/examples/algorithms/stable_orient_ring.als", "models/examples/algorithms/stable_orient_ring.thm", "models/examples/algorithms/stable_ringlead.als", "models/examples/algorithms/stable_ringlead.thm", "models/examples/case_studies/INSLabel.als", "models/examples/case_studies/chord.als", "models/examples/case_studies/chord2.als", "models/examples/case_studies/chordbugmodel.als", "models/examples/case_studies/com.als", "models/examples/case_studies/firewire.als", "models/examples/case_studies/firewire.thm", "models/examples/case_studies/ins.als", "models/examples/case_studies/iolus.als", "models/examples/case_studies/sync.als", "models/examples/case_studies/syncimpl.als", "models/examples/puzzles/farmer.als", "models/examples/puzzles/farmer.thm", "models/examples/puzzles/handshake.als", "models/examples/puzzles/handshake.thm", "models/examples/puzzles/hanoi.als", "models/examples/puzzles/hanoi.thm", "models/examples/systems/file_system.als", "models/examples/systems/file_system.thm", "models/examples/systems/javatypes_soundness.als", "models/examples/systems/lists.als", "models/examples/systems/lists.thm", "models/examples/systems/marksweepgc.als", "models/examples/systems/views.als", "models/examples/toys/birthday.als", "models/examples/toys/birthday.thm", "models/examples/toys/ceilingsAndFloors.als", "models/examples/toys/ceilingsAndFloors.thm", "models/examples/toys/genealogy.als", "models/examples/toys/genealogy.thm", "models/examples/toys/grandpa.als", "models/examples/toys/grandpa.thm", "models/examples/toys/javatypes.als", "models/examples/toys/life.als", "models/examples/toys/life.thm", "models/examples/toys/numbering.als", "models/examples/toys/railway.als", "models/examples/toys/railway.thm", "models/examples/toys/trivial.als", "models/examples/tutorial/farmer.als", "models/util/boolean.als", "models/util/graph.als", "models/util/integer.als", "models/util/natural.als", "models/util/ordering.als", "models/util/relation.als", "models/util/seqrel.als", "models/util/sequence.als", "models/util/sequniv.als", "models/util/ternary.als", "models/util/time.als");
		// Record the locations
		System.setProperty("alloy.theme0", alloyHome() + fs + "models");
		System.setProperty("alloy.home", alloyHome());

		try
		{
			// It doesn't work to set the env variable after java is started but this hack makes it work see
			// http://blog.cedarsoft.com/2010/11/setting-java-library-path-programmatically/
			System.setProperty("java.library.path", platformBinary);
			Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
