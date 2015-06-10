
package org.overturetool.alloy.test.unit;

        import static org.junit.Assert.fail;
        import static org.junit.Assert.assertEquals;

        import java.io.FileNotFoundException;
        import java.lang.reflect.Type;
        import java.util.ArrayList;
        import java.util.Collection;
        import java.util.List;

        import org.junit.runner.RunWith;
        import org.junit.runners.Parameterized;
        import org.junit.runners.Parameterized.Parameters;
        import org.overture.alloy.Alloy2VdmAnalysis;
        import org.overture.alloy.Context;
        import org.overture.alloy.ContextSlicing;
        import org.overture.alloy.NewSlicing;
        import org.overture.alloy.ast.Part;
        import org.overture.ast.analysis.AnalysisException;
        import org.overture.ast.definitions.PDefinition;
        import org.overture.ast.node.INode;
        import org.overture.core.tests.ParamStandardTest;
        import org.overture.core.tests.PathsProvider;

        import com.google.gson.reflect.TypeToken;


@RunWith(Parameterized.class)
public class SlicingUnitTest extends ParamStandardTest<String>
{


    TypeList type = new TypeList();


    // Root location of the test input and result files
    private static final String EXAMPLE_TEST_FILES = "src/test/resources/slicing";

    // The update property for this test
    private static final String UPDATE_PROPERTY = "tests.update.alloy.Unit";


    public SlicingUnitTest(String nameParameter, String inputParameter,
                           String resultParameter)
    {
        super(nameParameter, inputParameter, resultParameter);
    }


    @Parameterized.Parameters(name = "{index} : {0}")
    public static Collection<Object[]> testData()
    {
        return PathsProvider.computePaths(EXAMPLE_TEST_FILES);
    }

    @Override
    public String processModel(List<INode> ast)
    {
        ArrayList<String> arr = null;
        arr = type.getPair(this.testName);


        NewSlicing analysis = new NewSlicing(testName);
        try
        {
            ast.get(0).apply(analysis, new ContextSlicing(arr.get(0),arr.get(1)));
        } catch (AnalysisException e)
        {
            fail("Could not process test file " + testName);
        }


        return analysis.toString();
    }

    //TODO: Implement more intelligent comparison logic
    @Override
    public void compareResults(String actual, String expected)
    {
        assertEquals(expected.replace("\r", ""), actual.replace("\r", ""));
    }

    @Override
    public Type getResultType()
    {
        Type resultType = new TypeToken<String>()
        {
        }.getType();
        return resultType;
    }

    @Override
    protected String getUpdatePropertyString()
    {
        return UPDATE_PROPERTY;
    }





    private String parts2String(List<PDefinition> parts){
        StringBuilder sb = new StringBuilder();
        for (PDefinition p : parts){
            sb.append(p.toString());
            sb.append("\n");
        }


        return sb.toString();
    }
    public void p(String string){
        System.out.println(string);
    }
}
