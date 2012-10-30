<%--
  The Broad Institute
 SOFTWARE COPYRIGHT NOTICE AGREEMENT
 This software and its documentation are copyright (2003-2012) by the
 Broad Institute. All rights are reserved.

 This software is supplied without any warranty or guaranteed support
 whatsoever. The Broad Institute cannot be responsible for its
 use, misuse, or functionality.
--%>
<%@ page import="org.genepattern.server.util.MessageUtils" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>GenePattern add/update module help</title>
<style>
.example { font-family: Courier, Courier New, serif; font-size: 10pt; }
.exampleLink { font-family: Courier, Courier New, serif; font-size: 10pt; color: blue; text-decoration: underline}
</style>
<%
MessageUtils messages = new MessageUtils();
%>
</head>

<body>
<a name="propertiesHelp"></a>
<h2>Module Properties</h2>
A person creates a module to share an algorithm or utility with other <%=messages.get("ApplicationName")%> users.
The module properties describe the program used to execute the algorithm or utility, its
parameters and other useful information. Following are brief descriptions of each
module property:
	<p>
		<a name="Name_brief"></a><strong>Name</strong><br>Name of the module.</p>
	<p>
		<a name="LSID_brief"></a><strong>LSID</strong><br>The Life Science Identifier (LSID) used to uniquely identify a GenePattern module.</p>
	<p>
		<a name="Description_brief"></a><strong>Description</strong><br>
        Brief description of the module.
    </p>
	<p>
		<a name="Author_brief"></a><strong>Author</strong><br>
    The author&#39;s name and affiliation (company or academic institution).
    </p>
    <p>
		<a name="License_brief"></a><strong>License</strong><br>
        The End-User license agreement for the module.
    </p>
	<p>
		<a name="Privacy_brief"></a><strong>Privacy</strong><br>
        Modules may be marked as public or private:
    </p>
		<ul>
			<li>
				Public modules may be accessed by anyone using the GenePattern server.</li>
			<li>
				Private modules may be accessed only by the person who installed or created the module (or by an administrator).</li>
		</ul>
	<p>
		<a name="Quality_brief"></a><strong>Quality level</strong><br>
One of three terms that indicates the author&rsquo;s confidence in the robustness of the module: development, preproduction, and production.</p>
	<p>
		<a name="Documentation_brief"></a><strong>Documentation</strong><br>
Help file that describes the module and provides instructions
for its use.</p>
	<p>
		<a name="Command_brief"></a><strong>Command line</strong><br>
Command line used to launch the module. Values enclosed in angle brackets are replaced by specific values before the command executes.</p>
	<p>
		<a name="TaskType_brief"></a><strong>Module category</strong><br>
Category under which to list this module. Categories are used to organize modules and pipelines on the GenePattern home page. Pipelines are always assigned to the category name <em>pipeline</em>.</p>
	<p>
		<a name="cpu_brief"></a><strong>CPU type</strong><br>
Indicates the type of CPU required to run the module, or <em>any</em> if the module runs on any type of CPU.</p>
	<p>
		<a name="os_brief"></a><strong>Operating system</strong><br>
Indicates the operating system required to run the module, or <em>any</em> if the module runs on any operating system.</p>
	<p>
		<a name="Language_brief"></a><strong>Language</strong><br>
 Indicates the programming language used to implement the module.</p>
	<p>
		<a name="MinLanguage_brief"></a><strong>min. language version</strong><br>
Indicates the version of the programming language used to implement the module.</p>
	<p>
		<a name="VersionComment_brief"></a><strong>Version comment</strong><br>
Describes changes made to the module in this version.</p>
	<p>
		<a name="FileFormat_brief"></a><strong>File format(s)</strong><br>
Lists the file formats of any output files generated by the module.</p>
	<p>
		<a name="Files_brief"></a><strong>Current files</strong><br>
Lists the support files packaged with the module, such as executable programs, documentation, and so on.</p>
	<p>
		<a name="Parameters_brief"></a><strong>Parameters</strong><br>
Lists the module parameters, including the file formats of any input files required by the module.</p>

<a name="editingPropertiesHelp"></a><h2>Creating and Editing Modules</h2>

<strong>Note:</strong> Only the <%=messages.get("ApplicationName")%> team can create, edit or install modules on the <%=messages.get("ApplicationName")%> public server.
Therefore, to create a module, you must have a local <%=messages.get("ApplicationName")%> server installed.<br><br>

Creating a <%=messages.get("ApplicationName")%> module is a multi-step process:
<ol>
<li>Find or write a program that executes the desired function.
Any program that can be executed from the command line can be run as
a <%=messages.get("ApplicationName")%> module.
If you are writing the program, you can use any programming language;
for example, you can use a compiled language, such as C, to create an
executable or use a scripting language, such as Perl, to create a script
that is run by an interpreter.</li>
<li>Use <%=messages.get("ApplicationName")%> to create a module that invokes
the program that you have written.
It takes just a few minutes to enter the necessary information in the module integrator.
You can decide which parameters from the algorithm to expose to the user and
can replace command line parameter names that are hard to remember with names
that are self-explanatory. You can also create drop-down list choices for parameters
to reduce the possibility of invoking the module with incorrect values.
</li>
<li>Run the module several times, testing it thoroughly before making it available to other <%=messages.get("ApplicationName")%> users.</li>
</ol>

When you save your changes, the module properties that you have entered are validated as follows:
<ul><li> Every parameter you have not marked as optional must be listed in the command line.</li>
<li>Every command line parameter must be either a parameter, environment variable, or system property.</li>
<li>The module name and parameter names must be legal - in general, you should avoid punctuation marks and other special characters. </li>
</ul>
If everything checks out, the uploaded files are saved in the
<%=messages.get("ApplicationName")%> module library and the module registered
in the module database. The module and its uploaded files are indexed in the background
so that they are available for searching. You can run the module immediately and can
share it with others.  <br><br>

The following sections describe each module property in detail:
<ul>
<li><a href="#taskLevelAttributes">Title Bar</a></li>
<li><a href="#details">Details</a></li>
<li><a href="#supportFiles">Support Files</a></li>
<li><a href="#Command">Command Line</a></li>
<li><a href="#inputParameters">Parameters</a></li>
</ul>
An example for each property is given based on the Consensus Clustering module, which may be
uploaded from the
<a href="<%=request.getContextPath()%>/pages/taskCatalog.jsf?taskType=Clustering&state=new&state=updated&state=up%20to%20date&name=ConsensusClustering">module repository</a> if you haven't already installed it.

<br><br><hr>
<a name="taskLevelAttributes"></a><h3>Title Bar</h3>

<a name="Name"></a><h4>Name</h4>
The name of the module will be used in the drop-down module catalog lists and as a directory name on the server with
this name.  It should be a short but descriptive name, without spaces or punctuation, and may be mixed
upper- and lower-case.<br><br>
ConsensusClustering example: <span class="example">ConsensusClustering</span>

<a name="Version"></a><h4>Version</h4>
Each time you update a module, you create a new version of the module.
Typically, you want to edit the most recent version of a module.
If you want to edit an earlier version, select that version from the drop-down list of versions.

<h4>Help</h4>
Click Help to display this text.
<h4>Save</h4>
Click Save to save your changes, creating a new version of the module, and remain in the module integrator.
<h4>Save and Run</h4>
Click Save and Run to save your changes, creating a new version of the module,
exit from the module integrator and run the module.

<br><br><hr>

<a name="details"></a><h3>Details</h3>

<a name="LSID"></a><h4>LSID</h4>
The Life Science Identifier (LSID) used to uniquely identify a GenePattern module.
You cannot create or edit LSIDS. They are created automatically by the GenePattern server when a module is saved.<br><br>

ConsensusClustering example: <span class="example">urn:lsid:broad.mit.edu:cancer.software.genepattern.module.analysis:00030:5 </span>

<a name="Description"></a><h4>Description</h4>
The description is where to explain what your module does, and why someone would want to use it.
It can be anywhere from a sentence to a short paragraph in length.
The description, sometimes in abridged form, is displayed in the pipeline designer module choice list,
in generated code when creating scripts from pipelines, and in the web client.
It's a very good way for you to document succinctly why your module exists.<br><br>

ConsensusClustering example: <span class="example">Resampling-based clustering method</span>

<a name="Author"></a><h4>Author</h4>
Enter the author&apos;s name.  If you share this module
with others, they will know how to give the author credit and whom to contact with questions, suggestions,
or enhancement ideas.<br><br>

ConsensusClustering example: <span class="example">Stefano Monti</span>

<a name="Organization"></a><h4>Organization</h4>
Enter the author&apos;s affiliation (company or academic institution).  If you share this module
with others, they will know how to give the author credit and whom to contact with questions, suggestions,
or enhancement ideas.<br><br>

<a name="License"></a><h4>License</h4>
Upload a text file containing the End-User license agreement. Users will be prompted to accept this license
before running the module. <br>

<a name="VersionComment"></a><h4>Version Comment</h4>
Enter a brief description of the changes that you have made to the module. When GenePattern clients display a drop-down list of versions, the comments for each version are visible in the drop-down list.
<br><br>
ConsensusClustering example: <span class="example"><i>Added ability to create heatmap images of clusters</i></span>


<a name="TaskType"></a><h4>Module Category</h4>
On the GenePattern home page, modules and pipelines are organized by categories.
Pipelines are always assigned to the category name pipeline.
When you create/update a module, you can choose an existing category name or create a new category name.
If your module fits into an existing category, such as Preprocess & Utilities,
select that category from the drop-down list; otherwise, click the New button to add a new category.
GenePattern creates the drop-down list of categories dynamically based on the categories of the modules
installed on your GenePattern server. If you delete the last module in a given category,
that category is removed from the drop-down list.


ConsensusClustering example: <span class="example">Clustering</span>


<a name="Privacy"></a><h4>Privacy</h4>
Modules may be marked as either public or private.
When a module is first created, the default is to mark it private.
When a module is first created, the default is to mark it private.
<ul>
<li>Public modules are accessible to everyone who uses the server
on which it resides. </li>
<li>
Private modules may be accessed only by the module's owner, which is the username that the user logged in with.
Private modules are not visible to others building pipelines or running modules.
When a module is first created, the default is to mark it private.
</li></ul>
ConsensusClustering example: <span class="example">public</span>

<a name="Quality"></a><h4>Quality Level</h4>
The quality level is a simple three-level classification that lets the user know what level of confidence the
author has in the robustness of the module.  In increasing order of
quality expectations, they are: are &quot;development&quot;, &quot;preproduction&quot;, and &quot;production&quot;.
Although these terms have no strict definitions, they are useful for setting user expectations.
If you make this module public, set the quality level appropriately.
<br>
<br>
ConsensusClustering example: <span class="example">production</span>
<a name="cpu"></a><h4>CPU Type</h4>
If your module is compiled for a specific platform (Intel, Alpha, PowerPC, etc.), indicate that here.
CPU requirements are enforced when the module is run.<br><br>

ConsensusClustering example: <span class="example">any</span>

<a name="os"></a><h4>Operating System </h4>
If your module requires a specific operating system (Windows, Linux, MacOS, etc.), indicate that here.
Operating system requirements are enforced when the module is run.<br><br>

ConsensusClustering example: <span class="example">any</span>

<a name="Language"></a><h4>Language</h4>
There is no specific language support or requirement enforcement at this time.  However, by describing the
primary language that a module is implemented in, you give some hints to the prospective user about their
system requirements.<br><br>

ConsensusClustering example: <span class="example">Java</span>

<a name="MinLanguage"></a><h4>min. language level</h4>
If your module requires at least a certain revision of the language runtime environment(eg.
<span class="example">1.3.1_07</span>), indicate that here. This is not currently enforced, but provides useful information to the prospective module
user.<br><br>

ConsensusClustering example: <span class="example"><i>none specified</i></span>

<a name="OutputDescription"></a><h4>Output File Formats</h4>

Select the file formats of the output files generated by your module.
If your module generates an output file format not included in the list, click New to add that format to the list.
<br><br><hr>

<a name="supportFiles"></a><h3>Support Files</h3>
Any files required by your module, such as scripts, libraries, property files, DLLs, executable programs, etc.
must be uploaded to the server. These files may be referenced in the command line field
using the <span class="example">&lt;libdir&gt;<i>filename</i></span> nomenclature.
There is no upper limit on the number of files which may be uploaded, assuming there is enough space.

<ul>
<li>To add a file, click Add Files and select the file to add. When you save the module,
GenePattern copies the file to the server and adds it to the Current Files list.</li>

<li>To remove a file, select the check box next to the file in the Current Files list.
When you save the module, GenePattern removes the file from the server and the Current Files list.</li>
</ul>

Files that
have been uploaded appear as links in this section.  You may view or download them by clicking appropriately
in your browser.
<br><br>

<strong>Help Files</strong>: Public modules should always include a help file that provides instructions
for using the module, a detailed description of each input parameter, a detailed description of each output
file (both its format and content), and either an explanation of the algorithm or a reference to the paper, journal or book that
explains it.
<ul><li>To add a help file to your module, include the appropriate text file as the <strong>first</strong> text file
in the list of Support Files.
</li></ul>
When a user selects your module, GenePattern displays a form that includes the
module parameters and a Help button. When the user clicks the Help button, GenePattern examines the list of
support files for the module and displays the first file that has a standard documentation file extension.
If no documentation file was provided, GenePattern displays a message indicating that no information is
available. (By default, the standard documentation file extensions are html, htm, xhtml, pdf, rtf, and txt.
You can modify this list of extensions by editing the files.doc property in the
GenePattern /resources/genepattern.properties file.)<br><br>

ConsensusClustering example: <span class="example">Current files: </span> <span class="exampleLink">Acme.jar</span> <span class="exampleLink">archiver.jar</span> <span class="exampleLink">common_cmdline.jar</span> <span class="exampleLink">ConsensusClustering.pdf</span> <span class="exampleLink">file_support.jar</span> <span class="exampleLink">geneweaver.jar</span> <span class="exampleLink">gp-common.jar</span> <span class="exampleLink">ineq_0.2-2.tar.gz</span> <span class="exampleLink">ineq_0.2-2.tgz</span> <span class="exampleLink">jaxb-rt-1.0-ea.jar</span> <span class="exampleLink">my.local.install.r</span> <span class="exampleLink">RunSomAlg.jar</span> <span class="exampleLink">trove.jar</span> <span class="exampleLink">version.txt</span>


<br><br><hr>
<a name="Command"></a><h3>Command Line</h3>
The crux of adding a module to the <%=messages.get("ApplicationName")%> server is to provide the command line that will be used to
launch the module, including substitutions for settings that will be specified differently for each invocation.
In the command line field, you will provide a combination of the fixed text and the dynamically-changed
text which together constitute the command line for an invocation of the module.<br><br>

Perhaps the trickiest thing about specifying a command line is making it truly platform-independent.  Sure, it works fine
for your computer, right now.  But if you zip it and send it to an associate, are they running a Mac?  Windows?  Unix?
You may not know, and you shouldn't need to care.  By carefully describing the command line using substitution variables,
you can pretty well ensure that your module will run anywhere.<br><br>

<strong>Parameters:</strong> Parameters that require substitution should be enclosed in brackets (ie. &lt;filename&gt;).
Every parameter listed in the parameters section must be mentioned in the command line
unless its optional field is checked.  A default value
may be provided and will be used if the user fails to specify a value when
invoking the module.<br><br>

Click the View Argument List button to display a list of the parameters mentioned in the command line.
You can change the order of the parameters by dragging them to a new position in the list or
by editing the text of the command line.<br><br>


<strong>Substitution properties:</strong> In addition to parameter names, you may also use environment variables,
<a href="http://java.sun.com/docs/books/tutorial/essential/system/properties.html" target="_blank" style="white-space: nowrap;">Java system properties</a>,
and any properties defined in the %<%=messages.get("ApplicationName")%>InstallDir%/resources/genepattern.properties file.
In particular, there are predefined values for &lt;java&gt;, &lt;perl&gt;, and
&lt;R&gt;, three languages that are used within various modules that may be downloaded from the module catalog at
the public <%=messages.get("ApplicationName")%> website.  Useful substitution properties include:<br><br>

<table>
<tr><td valign="top"><span class="example">&lt;java&gt;</span></td><td>path to Java, the same one running the <%=messages.get("ApplicationName")%> server</td></tr>
<tr><td valign="top"><span class="example">&lt;perl&gt;</span></td><td>path to Perl, installed with <%=messages.get("ApplicationName")%> server on Windows, otherwise the one already installed on your system</td></tr>
<tr><td valign="top"><span class="example">&lt;R&gt;</span></td><td>path to a program that runs R and takes as input a script of R commands.  R is installed with <%=messages.get("ApplicationName")%>server on Windows and MacOS</td></tr>
<tr><td valign="top"><span class="example">&lt;java_flags&gt;</span></td><td>memory size and other Java JVM settings from the <%=messages.get("ApplicationName")%>/resources/genepattern.properties file</td></tr>
<tr><td valign="top"><span class="example">&lt;libdir&gt;</span></td><td>directory where the module's support files are stored</td></tr>
<tr><td valign="top"><span class="example">&lt;job_id&gt;</span></td><td>job number</td></tr>
<tr><td valign="top"><span class="example">&lt;name&gt;</span></td><td>name of the module being run</td></tr>
<tr><td valign="top"><span class="example">&lt;<i>filename</i>_basename&gt;</span></td><td>for each input file parameter, the filename without extension or directory</td></tr>
<tr><td valign="top"><span class="example">&lt;<i>filename</i>_extension&gt;</span></td><td>for each input file parameter, the extension without filename or directory</td></tr>
<tr><td valign="top"><span class="example">&lt;<i>filename</i>_file&gt;</span></td><td>for each input file parameter, the filename and extension without directory</td></tr>
<tr><td valign="top"><span class="example">&lt;path.separator&gt;</span></td><td>Java classpath delimiters (&#58; or &#59;), useful for specifying a classpath for Java-based modules</td></tr>
<tr><td valign="top"><span class="example">&lt;file.separator&gt;</span></td><td>/ or \ for directory delimiter</td></tr>
<tr><td valign="top"><span class="example">&lt;line.separator&gt;</span></td><td>newline, carriage return, or both for line endings</td></tr>
<tr><td valign="top"><span class="example">&lt;user.dir&gt;</span></td><td>current directory where the job is executing</td></tr>
<tr><td valign="top"><span class="example">&lt;user.home&gt;</span></td><td>user's home directory</td></tr>
</table>
<br>

Rather than having to customize your module's command line for the exact location of the language runtime
on each computer, you can use the substitution properties. For example,<br><br>
<span class="example">&lt;java&gt; -cp &lt;libdir&gt;mymodule.jar com.foo.MyModule &lt;arg1&gt;</span><br><br>
<%=messages.get("ApplicationName")%> will then take care of locating the Java runtime,
asking it to begin execution at the <span class="example">MyModule</span> class using code from the uploaded file
 <span class="example">mymodule.jar</span>.<br><br>

<strong>Standard input/output:</strong>
If your module is designed to accept a standard input stream and/or write to a
standard output stream, you can use redirection syntax when describing the command line.
To redirect a file to the input stream, enter the text \&lt; followed by the input file parameter.
To redirect the standard output or standard error streams to a named file, enter the
text \&gt; or \\&gt;&amp; followed by the name of the output file. In the following example,
the LogTransform module reads its input from the standard input stream and writes its output to
the standard output stream:
<br><br>
	<span class="brcode">&lt;perl&gt; &lt;libdir&gt;log_transform.pl \&lt; &lt;input.filename&gt; \&gt; &lt;output.file&gt;</span>

<br><br>

<strong>ConsensusClustering example</strong> (actually all on one line): <br>
<span class="example">&lt;java&gt; &lt;java_flags&gt; -DR_HOME=&lt;R_HOME&gt; -cp &lt;libdir&gt;geneweaver.jar edu.mit.wi.genome.geneweaver.clustering.ConsensusClustering &lt;input.filename&gt; &lt;kmax&gt; &lt;niter&gt; &lt;normalize.type&gt; -N &lt;norm.iter&gt; -S &lt;resample&gt; -t &lt;algo&gt; -L &lt;merge.type&gt; -i &lt;descent.iter&gt; -o &lt;out.stub&gt; -s -d &lt;create.heat.map&gt; -z &lt;heat.map.size&gt; -l1 -v</span>

<br><br><hr>
<a name="inputParameters"></a><h3>Parameters</h3>

The input parameters section of the form appears perhaps to be the most daunting.  And yet there is
little that is required to make a working module declaration.  Each parameter in the command line that comes from a
user input must have an entry in this section.  Otherwise the clients would know nothing about how to
prompt the user for input nor could they explain to the user what type of input is expected.<br><br>

To add one or more parameters, enter the number of parameters to add and click the Add Parameter button.

<a name="paramName"></a><h4>Name</h4>
Each parameter has a name, which can be whatever
you like, using letters, numbers, and period as a separator character between &quot;words&quot;.  It can be of mixed
upper- and lower-case.  The name is used inside &lt;brackets&gt; within the command line to indicate that the
value of that variable should be substituted at that position within the command line.  The name is also used as
a label within the web client to prompt the user for the value for that field.  And the name is used as
a way of identifying which parameter is which for the scripting clients.<br><br>


ConsensusClustering examples: <span class="example">kmax</span>, <span class="example">input.filename</span>

<a name="paramOptional"></a><h4>Optional</h4>

Some parameters are not required on the command line.  These parameters, when left blank by the user when the module
is invoked, result in nothing being added to the command line for that parameter.


<a name="paramDescription"></a><h4>Description</h4>
The description field is optional, but is very useful.  It allows the module author to provide a more detailed description
than the name itself.  What is the &quot;kmax&quot; parameter used for?  Does it interact with any other parameters?
Do you have any advice about what is a reasonable range of settings for it?  The description is displayed by the
<%=messages.get("ApplicationName")%> clients when they prompt for input for each field.<br><br>

ConsensusClustering example: <span class="example">Type of clustering algorithm</span>


<a name="paramDefault"></a><h4>Default Value</h4>

Some parameters should have a default value which will be supplied on the module's command line if no setting
is supplied by the user when invoking the module.  This is not the same as the defaults defined in the
program invoked by the module.
Instead, this allows the module author to create a default, even when none exists in the program being invoked by the module.<br><br>

The default value may use substitution variables, just like the rest of the command line.  So
a valid default for an output file might be <span class="example">&lt;input.filename_basename&gt;.foo</span>,
meaning that the output file will have the same stem as the input.filename parameter, but will have a .foo extension.
<br><br>
Default values for parameters that have a choice list must be either blank or one of the values from the choice list.
Any other setting will result in an error message.  If no default for a choice list is provided, the first entry
on the list will be the default.
<br><br>

ConsensusClustering examples: <span class="example">NMF</span>, <span class="example">5</span>, <span class="example">&lt;input.filename_basename&gt;</span>


<a name="paramFlag"></a><h4>Flag</h4>

Some parameters need to have extra text prefixing them on the command line when they are specified.
For example, you might need to write &quot;-F <i>filename</i>&quot; to pass in a filename.
The prefix text &quot;-F&quot; or &quot;-F &quot; would be specified here.
To insert a space between the flag and the parameter, add the space to the prefix text.<br><br>

example (with space): <span class="example">-F <i>inputfile</i></span>
<br>
example (without space): <span class="example">-F<i>inputfile</i></span>


<a name="paramType"></a><h4>Type</h4>

Declaration of the type of an input parameter allows the client to make a smarter presentation of the input to the
user.  (As of <%=messages.get("ApplicationName")%> 1.2, all parameters are being treated as either text or input file types).  Parameter type
choices are:

<ul>
<li>Text</li>
<li>Input File
<p>
When you select a parameter type of input file, a drop-down list of file formats appears in the file format column. Select the valid file format(s) for this parameter. If your module requires an input file format not included in the list, scroll back to the Output Description field and click New to add that format to the list.
For this type of parameter, when the user enters the name of the file, the
<%=messages.get("ApplicationName")%> clients pass along the entire
file rather than just the file name.<br><br>

</p>
</li>
<li>Choice
<p>
Some parameters are best represented as a drop-down list of choices.  By constraining input to those from the list,
the user is saved typing and cannot make a mistake by choosing an invalid setting (unless there is a dependency
on some other parameter). To enter the choices, click the Edit Choices link and enter the
choices in the Edit Choice List window.
<br><br>
For each choice enter the value required by the program (Value) and, optionally, a more
human-readable value (Display Value). When you exit from the Edit Choice List window,
the choices you entered are displayed as a semi-colon delimited set of choices. For example:
<br><br>

<span class="example"><font size="-1">hierarchical=Hierarchical clustering;SOM=Self-organizing map;NMF=Non-negative Matrix Factorization;3.14159265=pi</font></span>
<br>
<form>
<table>
<tr>
<td valign="top">
would create a drop-down list that looks like this:</td>
<td>
<select size="4"><option>Hierarchical clustering</option><option>Self-organizing map</option><option>Non-negative Matrix Factorization</option><option>pi</option></select>
</td></tr></table>
</form>
</li>
<li>Integer</li>
<li>Floating Point</li>
<li>Directory</li>
<li>Password</li>
</ul>





<br><br>

<br>
</body>
</html>
