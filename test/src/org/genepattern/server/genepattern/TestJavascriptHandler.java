package org.genepattern.server.genepattern;

import static java.net.URLEncoder.encode;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.genepattern.server.config.GpConfig;
import org.genepattern.server.dm.UrlUtil;
import org.genepattern.util.GPConstants;
import org.genepattern.webservice.TaskInfo;
import org.genepattern.webservice.TaskInfoAttributes;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nazaire on 9/28/15.
 */
public class TestJavascriptHandler
{
    private static final String lsid = "urn:lsid:broad.mit.edu:cancer.software.genepattern.module.visualizer:00045:7.11";
    private static final String mainFile="jsviewer.html";
    private static final Map<String, List<String>> emptyMap=Collections.emptyMap();
    
    private GpConfig gpConfig;
    private TaskInfo taskInfo;
    private TaskInfoAttributes tia;
    private Map<String, List<String>> substitutedValues=new LinkedHashMap<String,List<String>>();
    // use non-default ('/gp') servlet context path
    final String gpContextPath="/gp-custom-context-path";
    // default GenePatternURL set in 'genepattern.properties'
    final String GenePatternURL="http://127.0.0.1:8080/gp/"; 
    // client GpUrl as set in location bar in web browser
    final String clientGpUrl="http://wm97e-54f.broadinstitute.org:8080"+gpContextPath;
    private final int jobNo=41;

    @Before
    public void setUp() throws MalformedURLException {
        gpConfig=mock(GpConfig.class);
        when(gpConfig.getGpPath()).thenReturn("/gp");

        tia = new TaskInfoAttributes();
        tia.put(GPConstants.TASK_TYPE, GPConstants.TASK_TYPE_JAVASCRIPT);
        tia.put("commandLine", "clsfilecreator.html ? <input.file>");

        taskInfo=mock(TaskInfo.class);
        when(taskInfo.getLsid()).thenReturn(lsid);
        when(taskInfo.giveTaskInfoAttributes()).thenReturn(tia);
        when(taskInfo.getTaskInfoAttributes()).thenReturn(tia);

        substitutedValues.put("job.number", Arrays.asList(""+jobNo));
    }

    @Test
    public void generateLaunchUrl_cmsViewer() throws Exception {
        final String mainFile="cmsviewer.html";
        final String commandLine=mainFile+" ? <comparative.marker.selection.filename> <dataset.filename>";
        tia.put("commandLine", commandLine);

        final String lsid = "urn:lsid:broad.mit.edu:cancer.software.genepattern.module.visualizer:00045:7.11";
        final String odfFileUrl = "http://www.broadinstitute.org/cancer/software/genepattern/data/protocols/all_aml_test.preprocessed.comp.marker.odf";
        final String dataFileUrl = "http://www.broadinstitute.org/cancer/software/genepattern/data/protocols/all_aml_test.preprocessed.gct";

        final Map<String, List<String>> substitutedValues=new LinkedHashMap<String,List<String>>();
        substitutedValues.put("comparative.marker.selection.filename", Arrays.asList(odfFileUrl));
        substitutedValues.put("dataset.filename", Arrays.asList(dataFileUrl));
        
        // Note: one encoding (encodeURIcomponent) for path segments (the lsid) ...
        // ... a different encoding for each side of each key=value pair in the query string
        String expectedLaunchUrl="/gp/tasklib/" +  UrlUtil.encodeURIcomponent(lsid) +  "/"+mainFile+"?" +
                encode("comparative.marker.selection.filename", "UTF-8") + "=" + encode(odfFileUrl, "UTF-8") + "&" +
                encode("dataset.filename", "UTF-8") + "=" + encode(dataFileUrl, "UTF-8");

        assertEquals( 
                expectedLaunchUrl,
                JavascriptHandler.generateLaunchUrl(gpConfig, taskInfo, substitutedValues));
    }
    
    public void checkLaunchUrl(final String commandLine) throws Exception {
        checkLaunchUrl(commandLine, "index.html");
    }

    public void checkLaunchUrl(final String commandLine, final String mainFile) throws Exception {
        checkLaunchUrl("/gp", commandLine, mainFile);
    }

    /**
     * Parameterized test of generateLaunchUrl, with empty substituted values map.
     * @param gpPath e.g. "/gp"
     * @param commandLine e.g. "jsviewer.html ? <arg>"
     * @param expectedMainFile e.g. "jsviewer.html"
     * @throws Exception
     */
    public void checkLaunchUrl(final String gpPath, final String commandLine, final String expectedMainFile) throws Exception {
        tia.put("commandLine", commandLine);
        assertEquals(
                "commandLine='"+commandLine+"'",
                //expected
                gpPath+"/tasklib/" + UrlUtil.encodeURIcomponent(lsid) + "/" + UrlUtil.encodeURIcomponent(expectedMainFile),
                //actual
                JavascriptHandler.generateLaunchUrl(gpConfig, taskInfo, emptyMap));
    }

    @Test
    public void generateLaunchUrl() throws Exception {
        checkLaunchUrl("jsviewer.html ? <input.filename> <param>", mainFile);
    }

    @Test
    public void customGpPath() throws Exception {
        when(gpConfig.getGpPath()).thenReturn("/customGpPath");
        checkLaunchUrl("/customGpPath", "jsviewer.html ? <input.filename> <param>", "jsviewer.html");
    }

    @Test
    public void emptyGpPath() throws Exception {
        when(gpConfig.getGpPath()).thenReturn("");
        checkLaunchUrl("", "jsviewer.html ? <input.filename> <param>", "jsviewer.html");
    }

    @Test
    public void nullGpPath() throws Exception {
        when(gpConfig.getGpPath()).thenReturn(null);
        checkLaunchUrl("jsviewer.html ? <input.filename> <param>", "jsviewer.html");
    }

    @Test
    public void nullGpConfig() throws Exception {
        gpConfig=null;
        checkLaunchUrl("jsviewer.html ? <input.filename> <param>", "jsviewer.html");
    }

    @Test
    public void encodeMainFileName() throws Exception {
        final String mainFile="JavaScript Viewer.html";
        checkLaunchUrl(mainFile+" ? <input.filename> <param>", mainFile);
    }

    @Test
    public void generateLaunchUrl_noArgs() throws Exception {
        checkLaunchUrl("jsviewer.html ? ", mainFile);
    }

    @Test
    public void generateLaunchUrl_noDelim() throws Exception {
        checkLaunchUrl("jsviewer.html <input.filename> <param>", mainFile);
    }

    @Test
    public void generateLaunchUrl_noDelim_noArgs() throws Exception {
        checkLaunchUrl("jsviewer.html", mainFile);
    }

    @Test
    public void generateLaunchUrl_altFormat() throws Exception {
        checkLaunchUrl("jsviewer.html?input.filename=<input.filename>&param=<param>", "jsviewer.html");
    }

    @Test
    public void noMainFile_noDelim_withArgs() throws Exception {
        checkLaunchUrl("<input.filename> <param>");
    }

    @Test
    public void noMainFile_noDelim_withArgs_withWhitespace() throws Exception {
        checkLaunchUrl(" <input.filename> <param>");
    }

    @Test
    public void noMainFile_withArgs() throws Exception {
        checkLaunchUrl(" ? <input.filename> <param>");
    }

    @Test
    public void commandLineNotSet() throws Exception {
        tia.remove("commandLine");
        assertEquals(
                "commandLine not set, expecting default launchUrl",
                //expected
                "/gp/tasklib/" + UrlUtil.encodeURIcomponent(lsid) + "/index.html",
                //actual
                JavascriptHandler.generateLaunchUrl(gpConfig, taskInfo, emptyMap));
    }

    @Test
    public void commandLine_emptyString() throws Exception {
        checkLaunchUrl("");
    }

    @Test
    public void commandLine_whitespace() throws Exception {
        checkLaunchUrl("   ");
    }

    @Test
    public void generateLaunchUrl_userUpload() throws Exception {
        when(gpConfig.getGpPath()).thenReturn(gpContextPath);
        final String fileUrl=clientGpUrl+"/users/admin/all_aml_test.gct";
        substitutedValues.put("input.file", Arrays.asList(clientGpUrl+"/users/admin/all_aml_test.gct")); 
        assertEquals(
                // expected
                gpContextPath+"/tasklib/" + UrlUtil.encodeURIcomponent(lsid) + "/clsfilecreator.html?job.number="+jobNo+"&input.file="+ URLEncoder.encode(fileUrl, "UTF-8"),
                // actual
                JavascriptHandler.generateLaunchUrl(gpConfig, taskInfo, substitutedValues)
                );
    }

    @Test
    public void generateLaunchUrl_jobUpload() throws Exception {
        when(gpConfig.getGpPath()).thenReturn(gpContextPath);
        final String fileUrl=clientGpUrl+"users/test/tmp/run8743873107529768988.tmp/input.file/1/all_aml_train.gct";
        substitutedValues.put("input.file", Arrays.asList(fileUrl));
        assertEquals(
                // expected
                gpContextPath+"/tasklib/" + UrlUtil.encodeURIcomponent(lsid) + "/clsfilecreator.html?job.number="+jobNo+"&input.file="+ URLEncoder.encode(fileUrl, "UTF-8"),
                // actual
                JavascriptHandler.generateLaunchUrl(gpConfig, taskInfo, substitutedValues)
                );
    }

    @Test
    public void generateLaunchUrl_externalUrl() throws Exception  {
        when(gpConfig.getGpPath()).thenReturn(gpContextPath);
        final String fileUrl="http://www.broadinstitute.org/cancer/software/genepattern/data/all_aml/all_aml_train.gct";
        substitutedValues.put("input.file", Arrays.asList(fileUrl));
        assertEquals(
                // expected
                gpContextPath+"/tasklib/" + UrlUtil.encodeURIcomponent(lsid) + "/clsfilecreator.html?job.number="+jobNo+"&input.file="+URLEncoder.encode(fileUrl, "UTF-8"),
                // actual
                JavascriptHandler.generateLaunchUrl(gpConfig, taskInfo, substitutedValues)
                );
    }


}
