package org.overture.alloy;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Terminal;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4viz.VizGUI;
import org.apache.commons.cli.*;
import org.overture.alloy.ast.Part;
import org.overture.alloy.ast.Run;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Settings;
import org.overture.typechecker.util.TypeCheckerUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by macbookpro on 28/05/15.
 */
public class VdmToAlloy {
    public  String nameType;
    public  String type;
    public  String path;
    public String error="";
    public boolean typeInvariantsat=false;
    public String scope;
    public String command;
    public String filename;
    public A4Solution ans;


    public VdmToAlloy(String scope,boolean typeInvariantsat,String nameType,String type,String path) {
        this.typeInvariantsat=typeInvariantsat;
        this.nameType=nameType;
        this.type=type;
        this.path=path;
        this.scope=scope;
        this.command = "";
        this.ans = null;
        this.filename = "";
    }

    public int execute() throws Exception
    {
        ContextSlicing c =  new ContextSlicing();
        // create the command line parser
        String s = "";


        File input = new File(path);
        File output=null;



        Settings.dialect = Dialect.VDM_SL;
        TypeCheckerUtil.TypeCheckResult<List<AModuleModules>> result = TypeCheckerUtil.typeCheckSl(input);


        if (result.errors.isEmpty()) {
            File tmpFile = null;
            if (output == null) {
                tmpFile = File.createTempFile("vdm2alloy", ".als");
            } else {
                tmpFile = output;
            }


            /***************   Slicing  ******************/
              //  NewSlicing slicing = new NewSlicing(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")));
            //result.result.get(0).apply(slicing, new ContextSlicing(nameType,c.inverseTranslation(type)));//t = ATypeDefinition , f = AExplicitFunctionDefinition , v = AValueDefinition , st = AStateDefinition,op = AImplicitOperationDefinition

            //System.out.println(slicing.getNodeList().toString());

          //  System.out.println(slicing.toString());


            /******************** Not allowed types ************************/
            /*NotAllowed notAllowed = new NotAllowed();
            slicing.getModuleModules().apply(notAllowed, new ContextSlicing());
            NotAllowedTypes o = new NotAllowedTypes(notAllowed.getNotAllowed());
            if(o.hasNoAllowedType()) {
                this.error+="There are some problems on the file " + input +"\n"+o.toString();
                return 1;
            }*/



            /***************   Proof Obligations  ******************/

           //Proofs proof = new Proofs(slicing.getModuleModules());


            //System.out.println(proof.getNode().toString());
            /*********************** Translation ******************/
            Alloy2VdmAnalysis analysis = new Alloy2VdmAnalysis(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")),false);
            //slicing.getModuleModules().apply(analysis, new Context());
            result.result.get(0).apply(analysis,new Context());

            //System.out.println(analysis.components);

            /*if(!this.typeInvariantsat) {
               Alloy2VdmAnalysis analysisProof = new Alloy2VdmAnalysis(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")), true);
               proof.getNode().apply(analysisProof, new Context());
               analysis.components.addAll(analysisProof.getComponentsPO());*/
          /* }else {
                if (notAllowed.getHasNat()) {
                    analysis.components.add(new Run(this.nameType, this.scope, "1"));
                } else {
                    analysis.components.add(new Run(this.nameType, this.scope));
                }
            }*/


           FileWriter outFile = new FileWriter(tmpFile);
            PrintWriter out = new PrintWriter(outFile);
            for (Part string : analysis.components) {
                out.println(string);
            }

            out.close();

            // Alloy4 sends diagnostic messages and progress reports to the A4Reporter.
            // By default, the A4Reporter ignores all these events (but you can extend the A4Reporter to display the event for the user)
            A4Reporter rep = new A4Reporter() {
                // For example, here we choose to display each "warning" by printing it to System.out
                @Override public void warning(ErrorWarning msg) {
                    System.out.print("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
                    System.out.flush();
                }
            };


            String filename = tmpFile.getAbsolutePath();
            // Parse+typecheck the model
            System.out.println("=========== Parsing+Typechecking "+filename+" =============");
            try {
                Module world = CompUtil.parseEverything_fromFile(rep, null, filename);

                // Choose some default options for how you want to execute the commands
                A4Options options = new A4Options();

                options.solver = A4Options.SatSolver.SAT4J;
                int i = 1;
                for (Command command : world.getAllCommands()) {
                    System.out.println(i + " : " + command.toString());
                    i++;
                    // Execute the command
                    System.out.println("============ Command " + command + ": ============");
                    A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
                    // Print the outcome
                    System.out.println(ans);
                    this.command = command.toString() + "\n";
                    this.filename = tmpFile.getAbsolutePath();
                    this.ans = ans;
                }
            }
            catch (Exception e) {
                System.err.println("Erro: " + e);
            }

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
                /*if (line.hasOption(extraAlloyTest.getOpt())) {
                    String testInputPath = line.getOptionValue(extraAlloyTest.getOpt());
                    System.out.println("Running Alloy on file: "
                            + testInputPath);
                    Terminal.main(new String[]{"-alloy", testInputPath, "-a", "-s", "SAT4J"});
                }*/

        }else
        {
            this.error+="Errors in input VDM model";
            return 1;
        }
        return 0;
    }

    public String getCommand() {
        return this.command;
    }

    public String getFilename() {
        return this.filename;
    }

    public A4Solution getANS() {
        return this.ans;
    }

    public static void p(String string){
        System.out.println(string);
    }
}
