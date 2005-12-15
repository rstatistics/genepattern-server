<!-- /*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2006) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/ -->


<%@ page import="java.io.IOException,
		 java.util.StringTokenizer,
		 java.util.Enumeration,
		 java.util.HashMap,
		 java.io.File,
		 java.util.Date,
		 java.io.UnsupportedEncodingException,
		 java.net.InetAddress,
 		 java.net.URLEncoder, 
		 java.net.URLDecoder,
 		 java.text.SimpleDateFormat,
		 java.util.Date,
		 java.util.ArrayList,
		 java.util.Enumeration, 
		 java.util.GregorianCalendar,
		 java.text.ParseException,
		 java.text.DateFormat,
		
		 org.genepattern.webservice.TaskInfo,
		 org.genepattern.webservice.TaskInfoAttributes,
		 org.genepattern.webservice.ParameterFormatConverter,
		 org.genepattern.webservice.ParameterInfo,
		 org.genepattern.server.genepattern.GenePatternAnalysisTask,
		 org.genepattern.util.GPConstants,
		 org.genepattern.webservice.OmnigeneException,
		 org.genepattern.webservice.AnalysisWebServiceProxy,
		 org.genepattern.webservice.TaskInfo,
 		 org.genepattern.util.StringUtils,
		 org.genepattern.webservice.JobInfo,
		 com.jspsmart.upload.*,
		 org.genepattern.data.pipeline.PipelineModel"
	session="false" contentType="text/html" language="Java" %>
<%
response.setHeader("Cache-Control", "no-store"); // HTTP 1.1 cache control
response.setHeader("Pragma", "no-cache");		 // HTTP 1.0 cache control
response.setDateHeader("Expires", 0);
%>
<jsp:useBean id="mySmartUpload" scope="page" class="com.jspsmart.upload.SmartUpload" />

<html>
<head>
<link href="skin/stylesheet.css" rel="stylesheet" type="text/css">
	<link href="skin/favicon.ico" rel="shortcut icon">
	<title>Running Task</title>
</head>
<body>

<%
/**
 * To run a visualizer, we first upload the files here to get them a URL and then call
 * runVisualizer.jsp to actually launch it after we move the params out of the smart upload object into
 * a normal request
 */


com.jspsmart.upload.Request requestParameters = null;
String userID = null;

try {
	// mySmartUpload is from http://www.jspsmart.com/
	// Initialization
	mySmartUpload.initialize(pageContext);
	mySmartUpload.upload();
	requestParameters = mySmartUpload.getRequest();
	userID = requestParameters.getParameter(GPConstants.USERID);
	String RUN = "run";
	String CLONE = "clone";
	HashMap htFilenames = new HashMap();

	String lsid = requestParameters.getParameter("taskLSID");
	String taskName = requestParameters.getParameter("taskName");

	boolean DEBUG = false; // (requestParameters.getParameter("debug") != null);

	if (DEBUG) {
		System.out.println("\n\nPRERUN VISUALIZER Request parameters:<br>");
		for (java.util.Enumeration eNames = requestParameters.getParameterNames(); eNames.hasMoreElements(); ) {
			String n = (String)eNames.nextElement();
                        if (!("code".equals(n)))
			System.out.println(n + "='" + StringUtils.htmlEncode(requestParameters.getParameter(n)) + "'");
		}
	}
	String tmpDirName = null;
	if (mySmartUpload.getFiles().getCount() > 0) {

		String attachmentDir = null;
		File dir = null;
		String attachmentName = null;
		dir = new File(System.getProperty("java.io.tmpdir"));
		// create a bogus dir under this for the input files
		tmpDirName = taskName + "_" + userID + "_" + System.currentTimeMillis();			
		dir = new File(dir, tmpDirName );
		dir.mkdir();

		com.jspsmart.upload.File attachedFile = null;
		for (int i=0;i<mySmartUpload.getFiles().getCount();i++){
			attachedFile = mySmartUpload.getFiles().getFile(i);
			if (attachedFile.isMissing()) continue;
			try {
				attachmentName = attachedFile.getFileName();
				if (attachmentName.trim().length() == 0) continue;
				String fieldName = attachedFile.getFieldName();
				String fullName = attachedFile.getFilePathName();
				if (DEBUG) System.out.println("makePipeline: " + fieldName + " -> " + fullName);
				if (fullName.startsWith("http:") || fullName.startsWith("https:")|| fullName.startsWith("ftp:") || fullName.startsWith("file:")) {
				// don't bother trying to save a file that is a URL, retrieve it at execution time instead
					htFilenames.put(fieldName, fullName); // map between form field name and filesystem name
					continue;
				}
					
								attachmentName = dir.getPath() + File.separator + attachmentName;
				File attachment = new File(attachmentName);
				if (attachment.exists()) {
					attachment.delete();
				}
						
				attachedFile.saveAs(attachmentName);
				htFilenames.put(fieldName, tmpDirName + "/" + attachment.getName() ); // map between form field name and filesystem name
				
				//htFilenames.put(fieldName,attachedFile);
				
				if (DEBUG) System.out.println(fieldName + "=" + fullName + " (" + attachedFile.getSize() + " bytes) in " + htFilenames.get(fieldName) + "<br>");
			} catch (SmartUploadException sue) {
			    	throw new Exception("error saving " + attachmentName  + ": " + sue.getMessage());
			}
		}
		
	} // loop over files

	// set up the call to the runVisualizer.jsp by putting the params into the request
	// and then forwarding through a requestDispatcher
	TaskInfo task = GenePatternAnalysisTask.getTaskInfo(lsid, userID);
	ParameterInfo[] parmInfos = task.getParameterInfoArray();

	request.setAttribute("name", lsid);
	String server = request.getScheme()+"://"+ InetAddress.getLocalHost().getCanonicalHostName() + ":"
					+ System.getProperty("GENEPATTERN_PORT");
 	if (parmInfos == null){
 		parmInfos = new ParameterInfo[0];
 	}

	ArrayList missingReqParams = new ArrayList();
	 for (int i=0; i < parmInfos.length; i++){
		ParameterInfo pinfo = parmInfos[i];
		String value;	
		if (pinfo.isInputFile()){
			value = (String)htFilenames.get(pinfo.getName());
			if (value.startsWith("http:") || value.startsWith("https:")|| value.startsWith("ftp:") || value.startsWith("file:")) {
				value = value;
			} else {
				value = server + "/"+request.getContextPath()+"/getFile.jsp?task=&file="+ value;
			}
			HashMap pia = pinfo.getAttributes();
			pia.put(ParameterInfo.MODE, ParameterInfo.URL_INPUT_MODE);

		} else {
			value = requestParameters.getParameter(pinfo.getName());
		}
		request.setAttribute(pinfo.getName(), value);
	
		// look for missing required params
		if ((value == null) || (value.trim().length() == 0)){
			HashMap pia = pinfo.getAttributes();
			boolean isOptional = ((String)pia.get(GPConstants.PARAM_INFO_OPTIONAL[GPConstants.PARAM_INFO_NAME_OFFSET])).length() > 0;
		
			if (!isOptional){
				missingReqParams.add(pinfo);
			}
		}
		
		pinfo.setValue(value);

	}
	
	if (missingReqParams.size() > 0){
		System.out.println(""+missingReqParams);
%>
		<jsp:include page="navbar.jsp"></jsp:include>
<%

		request.setAttribute("missingReqParams", missingReqParams);
		(request.getRequestDispatcher("runTaskMissingParams.jsp")).include(request, response);
%>
		<jsp:include page="footer.jsp"></jsp:include>
		</body>
		</html>
<%
		return;
	}



	request.setAttribute("PipelineParameterInfo", parmInfos);

	RequestDispatcher rd = request.getRequestDispatcher("runPipeline.jsp");
	rd.include(request, response);

} catch (Exception e){
	e.printStackTrace();
}

%>


