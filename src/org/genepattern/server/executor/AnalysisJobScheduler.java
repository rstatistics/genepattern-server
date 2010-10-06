/*
 The Broad Institute
 SOFTWARE COPYRIGHT NOTICE AGREEMENT
 This software and its documentation are copyright (2003-2009) by the
 Broad Institute/Massachusetts Institute of Technology. All rights are
 reserved.
 
 This software is supplied without any warranty or guaranteed support
 whatsoever. Neither the Broad Institute nor MIT can be responsible for its
 use, misuse, or functionality.
 */

package org.genepattern.server.executor;

import static org.genepattern.util.GPConstants.STDERR;
import static org.genepattern.util.GPConstants.STDOUT;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.genepattern.server.database.HibernateUtil;
import org.genepattern.server.domain.JobStatus;
import org.genepattern.server.genepattern.GenePatternAnalysisTask;
import org.genepattern.server.genepattern.GenePatternAnalysisTask.JOB_TYPE;
import org.genepattern.server.webservice.server.dao.AnalysisDAO;
import org.genepattern.webservice.JobInfo;
import org.hibernate.Query;
import org.hibernate.SQLQuery;

/**
 * Polls the db for new PENDING jobs and submits them to GenePatternAnalysisTask for execution.
 */
public class AnalysisJobScheduler implements Runnable {
    private static Logger log = Logger.getLogger(AnalysisJobScheduler.class);

    public static ThreadGroup THREAD_GROUP = new ThreadGroup("GPAnalysisJob");
    public static ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new Thread(THREAD_GROUP, r);
        }
    };
    
    private final int BOUND = 20000;
    private final BlockingQueue<Integer> pendingJobQueue = new LinkedBlockingQueue<Integer>(BOUND);

    private Object jobQueueWaitObject = new Object();
    //the batch size, the max number of pending jobs to fetch from the db at a time
    private int batchSize = 20;
    private int numJobSubmissionThreads = 3;

    private Thread runner = null;
    private List<Thread> jobSubmissionThreads = null;

    public AnalysisJobScheduler() {
    }

    public void startQueue() {
        runner = new Thread(THREAD_GROUP, this);
        runner.setName("AnalysisTaskThread");
        runner.setDaemon(true);

        jobSubmissionThreads = new ArrayList<Thread>();
        for (int i=0; i<numJobSubmissionThreads; ++i) { 
            Thread jobSubmissionThread = new Thread(THREAD_GROUP, new ProcessingJobsHandler(jobQueueWaitObject, pendingJobQueue));
            jobSubmissionThread.setName("AnalysisTaskJobSubmissionThread-"+i);
            jobSubmissionThread.setDaemon(true);
            jobSubmissionThreads.add(jobSubmissionThread);
            jobSubmissionThread.start();
        }
        runner.start();
    }
    
    public void stopQueue() {
        if (runner != null) {
            runner.interrupt();
            runner = null;
        }
        for(Thread jobSubmissionThread : jobSubmissionThreads) {
            if (jobSubmissionThread != null) {
                //TODO: we could set the status back to PENDING for any jobs left on the queue
                jobSubmissionThread.interrupt();
                jobSubmissionThread = null;
            }
        }
        jobSubmissionThreads.clear();
    }

    /** Main AnalysisTask's thread method. */
    public void run() {
        log.debug("Starting AnalysisTask thread");
        try {
            while (true) {
                // Load input data to input queue
                List<Integer> waitingJobs = null;
                synchronized (jobQueueWaitObject) {
                    if (pendingJobQueue.isEmpty()) {
                        waitingJobs = AnalysisJobScheduler.getJobsWithStatusId(JobStatus.JOB_PENDING, batchSize);
                        if (waitingJobs != null && !waitingJobs.isEmpty()) {
                            waitingJobs = changeJobStatus(waitingJobs, JobStatus.JOB_PENDING, JobStatus.JOB_DISPATCHING);
                            if (waitingJobs != null) {
                                for(Integer jobId : waitingJobs) { 
                                    if (pendingJobQueue.contains(jobId)) {
                                        log.error("duplicate entry in pending jobs queue: "+jobId);
                                    }
                                    else {
                                        pendingJobQueue.put(jobId);
                                    }
                                }
                            }
                        }
                        else {
                            //insurance against deadlock, poll for new PENDING jobs every 60 seconds, regardless of whether notify has been called
                            final long timeout = 60*1000;
                            jobQueueWaitObject.wait(timeout);
                        }
                    }
                }
            }
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    static private List<Integer> getJobsWithStatusId(int statusId, int maxJobCount) {
        try {
            HibernateUtil.beginTransaction();
            String hql = "select jobNo from org.genepattern.server.domain.AnalysisJob where jobStatus.statusId = :statusId and :deleted = false order by submittedDate ";
            Query query = HibernateUtil.getSession().createQuery(hql);
            if (maxJobCount > 0) {
                query.setMaxResults(maxJobCount);
            }
            query.setInteger("statusId", statusId);
            query.setBoolean("deleted", false);
            List<Integer> jobIds = query.list();
            return jobIds;
        }
        catch (Throwable t) {
            log.error("Error getting list of pending jobs from queue", t);
            return new ArrayList<Integer>();
        }
        finally {
            HibernateUtil.closeCurrentSession();
        }
    }

    static private List<Integer> changeJobStatus(List<Integer> jobIds, int fromStatusId, int toStatusId) {
        List<Integer> updatedJobIds = new ArrayList<Integer>();
        HibernateUtil.beginTransaction();
        try {
            for(Integer jobId : jobIds) {
                AnalysisJobScheduler.changeJobStatus(jobId, fromStatusId, toStatusId);
                updatedJobIds.add(jobId);
            }
            HibernateUtil.commitTransaction();
        }
        catch (Throwable t) {
            // don't add it to updated jobs, record the failure and move on
            updatedJobIds.clear();
            log.error("Error updating job status to processing", t);
            HibernateUtil.rollbackTransaction();
        } 
        return updatedJobIds;
    }

    /**
     * Change the statusId for the given job, only if the job's current status id is the same as the fromStatusId.
     * This condition is helpful to guard against another thread which has already changed the job status.
     * 
     * @param jobNo
     * @param fromStatusId
     * @param toStatusId
     * @return number of rows successfully updated
     */
    static public int changeJobStatus(int jobNo, int fromStatusId, int toStatusId) {
        String sqlUpdate = "update ANALYSIS_JOB set status_id=:toStatusId where job_no=:jobNo and status_id=:fromStatusId";
        SQLQuery sqlQuery = HibernateUtil.getSession().createSQLQuery(sqlUpdate);
        sqlQuery.setInteger("toStatusId", toStatusId);
        sqlQuery.setInteger("jobNo", jobNo);
        sqlQuery.setInteger("fromStatusId", fromStatusId);

        int rval = sqlQuery.executeUpdate();
        if (rval != 1) {
            log.error("changeJobStatus(jobNo="+jobNo+", fromStatusId="+fromStatusId+", toStatusId="+toStatusId+") ignored, statusId for jobNo was already changed in another thread");
        }
        return rval;
    }
    
    static public int setJobStatus(int jobNo, int toStatusId) {
        String sqlUpdate = "update ANALYSIS_JOB set status_id=:toStatusId where job_no=:jobNo";
        SQLQuery sqlQuery = HibernateUtil.getSession().createSQLQuery(sqlUpdate);
        sqlQuery.setInteger("toStatusId", toStatusId);
        sqlQuery.setInteger("jobNo", jobNo);

        int rval = sqlQuery.executeUpdate();
        if (rval != 1) {
            log.error("setJobStatus(jobNo="+jobNo+", toStatusId="+toStatusId+") had no effect");
        }
        return rval;
    }

    /**
     * Wake up the job queue thread. The object is synchronized to obtain ownership of the monitor.
     */
    public void wakeupJobQueue() {
        synchronized (jobQueueWaitObject) {
            jobQueueWaitObject.notify();
        }
    }

    public static void terminateJob(Integer jobId) throws JobTerminationException {
        if (jobId == null) {
            throw new JobTerminationException("Invalid null arg");
        }
        JobInfo jobInfo = null;
        try {
            AnalysisDAO dao = new AnalysisDAO();
            jobInfo = dao.getJobInfo(jobId);
        }
        catch (Throwable t) {
            throw new JobTerminationException("Server error: Not able to load jobInfo for jobId: "+jobId, t);
        }
        finally {
            HibernateUtil.closeCurrentSession();
        }
        AnalysisJobScheduler.terminateJob(jobInfo);
    }
    
    public static void terminateJob(JobInfo jobInfo) throws JobTerminationException {
        if (jobInfo == null) {
            log.error("invalid null arg to terminateJob");
            return;
        }
    
        //note: don't terminate completed jobs
        boolean isFinished = isFinished(jobInfo); 
        if (isFinished) {
            log.debug("job "+jobInfo.getJobNumber()+"is already finished");
            return;
        }
    
        //terminate pending jobs immediately
        boolean isPending = isPending(jobInfo);
        if (isPending) {
            log.debug("Terminating PENDING job #"+jobInfo.getJobNumber());
            
            try { 
                AnalysisDAO ds = new AnalysisDAO();
                ds.updateJobStatus(jobInfo.getJobNumber(), JobStatus.JOB_ERROR);
                HibernateUtil.commitTransaction();
            }
            catch (Throwable t) {
                HibernateUtil.rollbackTransaction();
            }
            return;
        } 
        
        try {
            CommandExecutor cmdExec = CommandManagerFactory.getCommandManager().getCommandExecutor(jobInfo);
            cmdExec.terminateJob(jobInfo);
        }
        catch (Throwable t) {
            throw new JobTerminationException(t);
        }
    }

    public static boolean isPending(JobInfo jobInfo) {
        return isPending(jobInfo.getStatus());
    }

    private static boolean isPending(String jobStatus) {
        return JobStatus.PENDING.equals(jobStatus);
    }

    public static boolean isFinished(JobInfo jobInfo) {
        return isFinished(jobInfo.getStatus());
    }
    
    private static boolean isFinished(String jobStatus) {
        if ( JobStatus.FINISHED.equals(jobStatus) ||
                JobStatus.ERROR.equals(jobStatus) ) {
            return true;
        }
        return false;        
    }

    private static class ProcessingJobsHandler implements Runnable {
        private final Object jobQueueWaitObject;
        private final BlockingQueue<Integer> pendingJobQueue;
        private final GenePatternAnalysisTask genePattern = new GenePatternAnalysisTask();
        
        public ProcessingJobsHandler(Object jobQueueWaitObject, BlockingQueue<Integer> pendingJobQueue) {
            this.jobQueueWaitObject = jobQueueWaitObject;
            this.pendingJobQueue = pendingJobQueue;
        }
        
        public void run() {
            try {
                while (true) {
                    Integer jobId = pendingJobQueue.take();
                    submitJob(jobId);
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void submitJob(Integer jobId) {
            if (genePattern == null) {
                log.error("job not run, genePattern == null!");
                return;
            }
            synchronized(jobQueueWaitObject) {
                try {
                    genePattern.onJob(jobId);
                }
                catch (JobDispatchException e) {
                    handleJobDispatchException(jobId, e);
                }
            }
        }

        //handle errors during job dispatch (moved from GPAT.onJob)
        private void handleJobDispatchException(int jobId, Throwable t) {
            if (t.getCause() != null) {
              t = t.getCause();
            }
            String errorMessage = "Error submitting job #"+jobId;
            log.error(errorMessage, t);
            
            GenePatternAnalysisTask.getJobDir(""+jobId);
            
            String outDirName = GenePatternAnalysisTask.getJobDir(""+jobId);
            File outFile = GenePatternAnalysisTask.writeStringToFile(outDirName, STDERR, "GenePattern Server error preparing job for execution.\n"+t.getMessage() + "\n\n");
            int exitCode = -1;
            JOB_TYPE jobType = JOB_TYPE.JOB;
            
            
            int parentJobId = -1;
            try {
                AnalysisDAO dao = new AnalysisDAO();
                parentJobId = dao.getParentJobId(jobId);
            }
            catch (Throwable t1) {
                log.error("Error getting parentJobId for job #"+jobId);
            }
            finally {
                HibernateUtil.closeCurrentSession();
            }
            
            if (parentJobId >= 0) {
              jobType = JOB_TYPE.PIPELINE;
            }
            try {
                GenePatternAnalysisTask.handleJobCompletion(jobId, STDOUT, STDERR, exitCode, JobStatus.JOB_ERROR, jobType);
            }
            catch (Throwable t1) {
                log.error("Error handling job completion for job #"+jobId, t1);
            }
        }
    }

}

