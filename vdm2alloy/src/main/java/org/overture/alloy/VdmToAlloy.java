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
    public boolean hasNatural = false;





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
                NewSlicing slicing = new NewSlicing(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")));
                result.result.get(0).apply(slicing, new ContextSlicing(nameType,c.inverseTranslation(type)));//t = ATypeDefinition , f = AExplicitFunctionDefinition , v = AValueDefinition , st = AStateDefinition,op = AImplicitOperationDefinition,fi=AImplicitFunctionDefinition

                this.hasNatural=slicing.isHasNatural();
                System.out.println("cena:"+slicing.isHasNatural());

            //System.out.println("\n\n\n\n\n--------------------------------------------\n\n"+slicing.toString());


            /******************** Not allowed types ************************/
            NotAllowed notAllowed = new NotAllowed();
            slicing.getModuleModules().apply(notAllowed, new ContextSlicing());
            NotAllowedTypes o = new NotAllowedTypes(notAllowed.getNotAllowed());
            if(o.hasNoAllowedType()) {
                this.error += "There are some problems on the file " + input + "\n" + o.toString();
                return 1;
            }



            /***************   Proof Obligations  ******************/

         //  Proofs proof = new Proofs(slicing.getModuleModules());


           // System.out.println("PROOF OBLIGATION "+proof.getNode().toString());
            /*********************** Translation ******************/
            Alloy2VdmAnalysis analysis = new Alloy2VdmAnalysis(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")),false);
            slicing.getModuleModules().apply(analysis, new Context());
           // result.result.get(0).apply(analysis,new Context());

            //System.out.println(analysis.components);

            /*if(!this.typeInvariantsat) {
               Alloy2VdmAnalysis analysisProof = new Alloy2VdmAnalysis(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")), true);
               proof.getNode().apply(analysisProof, new Context());
               analysis.components.addAll(analysisProof.getComponentsPO());*/
           //}else {
                if (notAllowed.getHasNat()) {
                    analysis.components.add(new Run(this.nameType, this.scope, "1"));
                } else {
                    analysis.components.add(new Run(this.nameType, this.scope));
                }
            //}


           FileWriter outFile = new FileWriter(tmpFile);
            PrintWriter out = new PrintWriter(outFile);
            for (Part string : analysis.components) {
                out.println(string);
            }

            out.close();
            this.filename = tmpFile.getAbsolutePath();


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

    public String getError(){
        return this.error;
    }

    public boolean getNatural (){
        return this.hasNatural;
    }

    public static void p(String string){
        System.out.println(string);
    }
}
