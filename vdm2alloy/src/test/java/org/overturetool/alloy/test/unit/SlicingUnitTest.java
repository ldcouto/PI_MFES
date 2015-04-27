package org.overturetool.alloy.test.unit;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.overture.alloy.Alloy2VdmAnalysis;
import org.overture.alloy.Context;
import org.overture.alloy.ContextSlicing;
import org.overture.alloy.Slicing;
import org.overture.alloy.ast.Part;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.node.INode;
import org.overture.core.tests.ParamStandardTest;
import org.overture.core.tests.PathsProvider;

import com.google.gson.reflect.TypeToken;

@RunWith(Parameterized.class)
public class SlicingUnitTest extends ParamStandardTest<String>
{


    // Root location of the test input and result files
    private static final String EXAMPLE_TEST_FILES = "src/test/resources/slicingTest";

    // The update property for this test
    private static final String UPDATE_PROPERTY = "tests.update.alloy.Unit";


    public SlicingUnitTest(String nameParameter, String inputParameter,
                             String resultParameter)
    {
        super(nameParameter, inputParameter, resultParameter);
    }

    @Parameters(name = "{index} : {0}")
    public static Collection<Object[]> testData()
    {
        return PathsProvider.computePaths(EXAMPLE_TEST_FILES);
    }

    @Override
    public String processModel(List<INode> ast)
    {
       Slicing analysis = new Slicing(testName);
        try
        {
            ast.get(0).apply(analysis, new ContextSlicing());
        } catch (AnalysisException e)
        {
            fail("Could not process test file " + testName);
        }

        return parts2String(analysis.getNodeList());
    }

    //TODO: Implement more intelligent comparison logic
    @Override
    public void compareResults(String actual, String expected)
    {
        assertEquals(expected,actual);
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


    private String parts2String(List<INode> parts){
        StringBuilder sb = new StringBuilder();
        for (INode p : parts){
            sb.append(p.toString());
            sb.append(" , ");
        }
        return sb.toString();
    }

}
