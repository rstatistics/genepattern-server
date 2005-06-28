package org.genepattern.server.webservice.server;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.soap.SOAPException;

import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.log4j.Category;
import org.genepattern.server.handler.AddNewJobHandler;
import org.genepattern.server.handler.GetJobStatusHandler;
import org.genepattern.server.webservice.GenericWebService;
import org.genepattern.webservice.FileWrapper;
import org.genepattern.webservice.JobInfo;
import org.genepattern.webservice.JobStatus;
import org.genepattern.webservice.OmnigeneException;
import org.genepattern.webservice.ParameterInfo;
import org.genepattern.webservice.TaskInfo;
import org.genepattern.webservice.WebServiceException;
import org.genepattern.webservice.AnalysisJob;

/**
 * Analysis Web Service.
 * 
 * @author David Turner, Hui Gong
 * @version 1.1
 */

public class Analysis extends GenericWebService {
	private MessageContext context = null;

	private static Category _cat = Category.getInstance(Analysis.class
			.getName());

	/**
	 * Default constructor. Constructs a <code>Analysis</code> web service
	 * object.
	 */
	public Analysis() {
		Thread.yield(); // JL: fixes BUG in which responses from AxisServlet are
						// sometimes empty
	}

	/**
	 * Gets the latest versions of all tasks
	 * 
	 * @return The latest tasks
	 * @exception WebServiceException
	 *                If an error occurs
	 */
	public TaskInfo[] getTasks() throws WebServiceException {
		Thread.yield(); // JL: fixes BUG in which responses from AxisServlet are
						// sometimes empty
		return new AdminService() {
			protected String getUserName() {
				return getUsernameFromContext();
			}
		}.getLatestTasks();
	}

    /**
	 * Saves a record of a job that was executed on the client into the database
	 * 
	 * @param taskID
	 *            the ID of the task to run.
	 * @param parmInfo
	 *            the parameters 
    * @exception WebServiceException
	 *                thrown if problems are encountered
	 * @return the job information
	 */
    
   public JobInfo recordClientJob(int taskID, ParameterInfo[] parameters) 	throws WebServiceException {
      try {
          org.genepattern.server.ejb.AnalysisJobDataSource ds = org.genepattern.server.util.BeanReference
                     .getAnalysisJobDataSourceEJB();   
         return ds.recordClientJob(taskID, getUsernameFromContext(), org.genepattern.webservice.ParameterFormatConverter.getJaxbString(parameters));
      } catch(Exception e) {
         throw new WebServiceException(e);  
      }
   }
   
   
   /**
	 * Submits an analysis job to be processed. The job is a child job of the supplied parent job.
	 * 
	 * @param taskID
	 *            the ID of the task to run.
	 * @param parmInfo
	 *            the parameters to process
	 * @param files
	 *            a HashMap of input files sent as attachments
    * @param the parent job number
	 * @return the job information for this process
	 * @exception is
	 *                thrown if problems are encountered
	 */
    
   public JobInfo submitJob(int taskID, ParameterInfo[] parameters, Map files, int parentJobId) throws WebServiceException {
      try {
         renameInputFiles(parameters, files);
         AddNewJobHandler req = new AddNewJobHandler(taskID, getUsernameFromContext(),
					parameters, parentJobId);
			return req.executeRequest();
      } catch(Exception e) {
         throw new WebServiceException(e);  
      }
   }
   
   // find any input files and concat axis name with original file name.
   private void renameInputFiles(ParameterInfo[] parameters, Map files) throws WebServiceException {
      if (parameters != null)
			for (int x = 0; x < parameters.length; x++) {
				if (parameters[x].isInputFile()) {
					String orgFilename = parameters[x].getValue();
					AttachmentPart ap = (AttachmentPart) files.get(orgFilename);
					DataHandler dataHandler = null;
					try {
						dataHandler = ap.getDataHandler();
					} catch (SOAPException se) {
						throw new WebServiceException(
								"Error while processing files");
					}
					String newFilename = dataHandler.getName() + "_"
							+ orgFilename;
					File f = new File(dataHandler.getName());
					File newFile = new File(newFilename);
					boolean renamed = f.renameTo(newFile);
					//reset parameter's value with new filename
					if (renamed) {
						parameters[x].setValue(newFilename);
					} else {
						try {
							parameters[x].setValue(f.getCanonicalPath());
						} catch (IOException ioe) {
							throw new WebServiceException(ioe);
						}
					}
				}
         }
   }
   
	/**
	 * Submits an analysis job to be processed.
	 * 
	 * @param taskID
	 *            the ID of the task to run.
	 * @param parmInfo
	 *            the parameters to process
	 * @param files
	 *            a HashMap of input files sent as attachments
	 * @return the job information for this process
	 * @exception is
	 *                thrown if problems are encountered
	 */
	public JobInfo submitJob(int taskID, ParameterInfo[] parameters, Map files)
			throws WebServiceException {
		Thread.yield(); // JL: fixes BUG in which responses from AxisServlet are
						// sometimes empty

		// get the username
		String username = getUsernameFromContext();

		JobInfo jobInfo = null;
		
		renameInputFiles(parameters, files);

		try {
			AddNewJobHandler req = new AddNewJobHandler(taskID, username,
					parameters);
			jobInfo = req.executeRequest();
		} catch (org.genepattern.webservice.OmnigeneException oe) {
			_cat.error(oe.getMessage());
			oe.printStackTrace();
			throw new WebServiceException(oe);
		} catch (Throwable t) {
			_cat.error(t.getMessage());
			t.printStackTrace();
			throw new WebServiceException(t);
		}

		return jobInfo;
	}

	/**
	 * Checks the status of a particular job.
	 * 
	 * @param jobID
	 *            the ID of the task to check
	 * @return the job information
	 * @exception is
	 *                thrown if problems are encountered
	 */
	public JobInfo checkStatus(int jobID) throws WebServiceException {
		Thread.yield(); // JL: fixes BUG in which responses from AxisServlet are
						// sometimes empty

		JobInfo jobInfo = null;

		try {
			GetJobStatusHandler req = new GetJobStatusHandler(jobID);
			jobInfo = req.executeRequest();
		} catch (org.genepattern.webservice.OmnigeneException oe) {
			_cat.error(oe.getMessage());
			throw new WebServiceException(oe);
		} catch (Throwable t) {
			_cat.error(t.getMessage());
			t.printStackTrace();
			throw new WebServiceException(t);
		}

		return jobInfo;
	}

	/**
	 * Returns the result files of a completed job.
	 * 
	 * @param jobID
	 *            the ID of the job that completed.
	 * @return the List of FileWrappers containing the results for this process
	 * @exception is
	 *                thrown if problems are encountered
	 */
	public List getResultFiles(int jobID) throws WebServiceException {
		Thread.yield(); // JL: fixes BUG in which responses from AxisServlet are
						// sometimes empty

		JobInfo jobInfo = null;
		ArrayList filenames = null;

		try {
			GetJobStatusHandler req = new GetJobStatusHandler(jobID);
			jobInfo = req.executeRequest();
		} catch (org.genepattern.webservice.OmnigeneException oe) {
			_cat.error(oe.getMessage());
			throw new WebServiceException(oe);
		} catch (Throwable t) {
			_cat.error(t.getMessage());
			throw new WebServiceException(t);
		}

		if (jobInfo != null) {
			ParameterInfo[] parameters = jobInfo.getParameterInfoArray();
			if (parameters != null) {
				for (int x = 0; x < parameters.length; x++) {
					if (parameters[x].isOutputFile()) {
						if (filenames == null)
							filenames = new ArrayList();
						filenames.add(System.getProperty("jobs") + "/"
								+ parameters[x].getValue());
					}
				}
			}
		}

		ArrayList list = null;
		if (filenames != null) {
			list = new ArrayList(filenames.size());

			for (Iterator iterator = filenames.iterator(); iterator.hasNext();) {
				String fn = (String) iterator.next();
				File f = new File(fn);
				DataHandler dataHandler = new DataHandler(
						new FileDataSource(fn));
				list.add(new FileWrapper(dataHandler.getName(), dataHandler, f
						.length(), f.lastModified()));
			}
		}

		return list;
	}
   
   /**
   * Terminates the execution of the given job
   *
   * @param jobId the job id
   */
   public void terminateJob(int jobId) throws WebServiceException {
      try {
         Process p = org.genepattern.server.genepattern.GenePatternAnalysisTask.terminatePipeline("" + jobId);
         if (p != null) {
            setJobStatus(jobId, JobStatus.ERROR);
         }
         org.genepattern.server.ejb.AnalysisJobDataSource ds = org.genepattern.server.util.BeanReference
			.getAnalysisJobDataSourceEJB();
         JobInfo[] children = ds.getChildren(jobId);
         for(int i = 0; i < children.length; i++) { // terminate all child jobs
             terminateJob(children[i].getJobNumber());  
         }
      } catch(Exception e) {
         throw new WebServiceException(e);  
      }
   }
   
   /**
	 * Purges the all the input and output files for the given job and expunges
	 * the job from the stored history. If the job is running if will be terminated.
	 * 
	 * @param jobId
	 *            the job id
	 */
	public void purgeJob(int jobId) throws WebServiceException {
		try {
         deleteJob(jobId);
         org.genepattern.server.ejb.AnalysisJobDataSource ds = org.genepattern.server.util.BeanReference
					.getAnalysisJobDataSourceEJB();
         org.genepattern.server.indexer.Indexer.deleteJob(jobId);  
         JobInfo[] children = ds.getChildren(jobId);
         ds.deleteJob(jobId);
         
         for(int i = 0; i < children.length; i++) {
            purgeJob(children[i].getJobNumber());  
         }
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
	}
   

	/**
	 * Deletes the all the input and output files for the given job and removes
	 * the job from the stored history. If the job is running if will be terminated.
	 * 
	 * @param jobId
	 *            the job id
	 */
	public void deleteJob(int jobId) throws WebServiceException {
		try {
         terminateJob(jobId);
			File jobDir = new File(
					org.genepattern.server.genepattern.GenePatternAnalysisTask
							.getJobDir(String.valueOf(jobId)));
			File[] files = jobDir.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					files[i].delete();
					org.genepattern.server.indexer.Indexer.deleteJobFile(jobId,
							files[i].getName());
				}
			}
			jobDir.delete();
			org.genepattern.server.ejb.AnalysisJobDataSource ds = org.genepattern.server.util.BeanReference
					.getAnalysisJobDataSourceEJB();
			ds.setJobDeleted(jobId, true);
         JobInfo[] children = ds.getChildren(jobId);
         for(int i = 0; i < children.length; i++) {
            deleteJob(children[i].getJobNumber());  
         }
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
	}

   
   private ParameterInfo[] removeOutputFile(JobInfo jobInfo, String value) {
      ParameterInfo[] params = jobInfo.getParameterInfoArray();
      if(params==null) {
         return new ParameterInfo[0];
      }
      List newParams = new ArrayList();
      for(int i = 0; i < params.length; i++) {
         if(!params[i].isOutputFile() || !params[i].getValue().equals(value)) {
            newParams.add(params[i]);
         }
      }
      return (ParameterInfo[]) newParams.toArray(new ParameterInfo[0]);
   }
   
	/**
	 * 
	 * Deletes the given output file for the given job and removes the output file from the parameter info array for the job. If <tt>jobId</tt> is a parent job and value was created by it's child, the child will be updated as well. Additionall, if <tt>jobId</tt> is a child job, the parent will be updated too.
	 * 
	 * @param jobId
	 *            the job id
	 * @param value
	 *            the value of the parameter info object for the output file to delete
	 */
	public void deleteJobResultFile(int jobId, String value) throws WebServiceException {
      try {
         org.genepattern.server.ejb.AnalysisJobDataSource ds = org.genepattern.server.util.BeanReference
                     .getAnalysisJobDataSourceEJB();
         JobInfo jobInfo = ds.getJobInfo(jobId);
         int beforeDeletionLength = 0;
         ParameterInfo[] params = jobInfo.getParameterInfoArray();
         if(params!=null) {
             beforeDeletionLength = params.length;
         }
         jobInfo.setParameterInfoArray(removeOutputFile(jobInfo, value));
         
         if(jobInfo.getParameterInfoArray().length==beforeDeletionLength) {
            throw new WebServiceException(new java.io.FileNotFoundException());  
         }
         
         int fileCreationJobNumber = jobInfo.getJobNumber();
        
         String fileName = value;
         int index1 = fileName.lastIndexOf('/');
         int index2 = fileName.lastIndexOf('\\');
         int index = (index1 > index2 ? index1 : index2);
         if (index != -1) {
            fileCreationJobNumber = Integer.parseInt(fileName.substring(0, index));
            fileName = fileName.substring(index + 1, fileName
                           .length());
            
         }		
         String jobDir = org.genepattern.server.genepattern.GenePatternAnalysisTask
               .getJobDir(String.valueOf(fileCreationJobNumber));
         File file = new File(jobDir, fileName);
         if(file.exists()) {
            file.delete();
         }
         
         ds.updateJob(jobInfo.getJobNumber(), jobInfo.getParameterInfo(), ((Integer)JobStatus.STATUS_MAP.get(jobInfo.getStatus())).intValue());
            
         if(fileCreationJobNumber!=jobId) { // jobId is a parent job
            JobInfo childJob = ds.getJobInfo(fileCreationJobNumber);
            childJob.setParameterInfoArray(removeOutputFile(childJob, value));
            ds.updateJob(childJob.getJobNumber(), childJob.getParameterInfo(), ((Integer)JobStatus.STATUS_MAP.get(childJob.getStatus())).intValue());
         } else {
            JobInfo parent = ds.getParent(jobId);
            if(parent!=null) { // jobId is a child job
               parent.setParameterInfoArray(removeOutputFile(parent, value));
            }
         }
          try {
             org.genepattern.server.indexer.Indexer.deleteJobFile(fileCreationJobNumber,
               fileName);
          } catch (IOException ioe) {
         // ignore Lucene Lock obtain timed out exceptions
            _cat.debug(ioe + " while deleting search indices for job "
               + jobId);
          }
      } catch(org.genepattern.server.JobIDNotFoundException jnfe) {
         // file and job has already been deleted-ignore
      } catch(org.genepattern.webservice.OmnigeneException oe) {
         throw new WebServiceException(oe);  
      } catch(java.rmi.RemoteException re) {
         throw new WebServiceException(re); 
      }
          
     
	}
  
   
   /**
   * Sets the status of the given job
   * @param jobId the job id
   * @param status the job status. One of "Pending", "Processing", "Finished, or "Error"
   */
   public void setJobStatus(int jobId, String status) throws WebServiceException {
      try {
         org.genepattern.server.ejb.AnalysisJobDataSource ds = org.genepattern.server.util.BeanReference
                  .getAnalysisJobDataSourceEJB();
         JobInfo jobInfo = ds.getJobInfo(jobId);
         Integer intStatus = (Integer) JobStatus.STATUS_MAP.get(status);
         if(intStatus==null) {
            throw new WebServiceException("Unknown status: " + status);  
         }
         ds.updateJob(jobId, intStatus.intValue());
      } catch (Exception e) {
			throw new WebServiceException(e);
		}
   }
   
   
   
	/**
	 * 
	 * Gets the jobs for the current user
    * @param username the username to retrieve jobs for. If <tt>null</tt> all available jobs are returned.
    * @param maxJobNumber the maximum job number to include in the returned jobs or -1, to start at the current maximum job number in the database
    * @param maxEntries the maximum number of jobs to return
    * @param allJobs if <tt>true</tt> return all jobs that the given user has run, otherwise return jobs that have not been deleted 
	 * 
	 * @return the jobs
	 */
	public JobInfo[] getJobs(String username, int maxJobNumber, int maxEntries, boolean allJobs) throws WebServiceException {
		try {
			org.genepattern.server.ejb.AnalysisJobDataSource ds = org.genepattern.server.util.BeanReference
					.getAnalysisJobDataSourceEJB();
			return ds.getJobs(username, maxJobNumber, maxEntries, allJobs);
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
	}

	/**
	 * Returns the username trying to access this service. The username is
	 * retrieved from the incoming soap header.
	 * 
	 * @return a String containing the username or an empty string if one not
	 *         found.
	 */
	protected String getUsernameFromContext() {
		// get the context then the username from the soap header
		context = MessageContext.getCurrentContext();
		String username = context.getUsername();
		if (username == null)
			username = "";
		return username;
	}

}

