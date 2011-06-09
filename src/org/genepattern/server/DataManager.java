package org.genepattern.server;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.genepattern.server.config.ServerConfiguration;
import org.genepattern.server.config.ServerConfiguration.Context;
import org.genepattern.server.database.HibernateUtil;
import org.genepattern.server.domain.UploadFile;
import org.genepattern.server.domain.UploadFileDAO;
import org.genepattern.server.webapp.jsf.AuthorizationHelper;
import org.genepattern.server.webapp.jsf.UIBeanHelper;

/**
 * Utility class for managing data files.
 * 
 * @author pcarr
 *
 */
public class DataManager {
    private static Logger log = Logger.getLogger(DataManager.class);

    /**
     * Delete the file from the server file system, checking permissions based on user context.
     * 
     * @param uploadedFile
     * 
     * @return true if the file was deleted
     */
    public static boolean deleteFile(UploadFile uploadedFile) {
        //if we are in a transaction, don't commit and close
        boolean inTransaction = HibernateUtil.isInTransaction();
        
        //this begins a new transaction
        UploadFileDAO dao = new UploadFileDAO();
        if (uploadedFile == null) {
            log.error("FileObj is null");
            return false;
        }

        File file = new File(uploadedFile.getPath());

        boolean deleted = false;
        boolean canDelete = canDelete(uploadedFile);
        if (!canDelete) {
            return false;
        }
            
        if (file.exists()) {
            deleted = file.delete(); 
            if (!deleted) {
                log.error("Error deleting file: "+file.getPath());
            }
        }
        // as currently implemented there is a small chance the db will not get updated
        // after deleting the file, therefore, we remove the record from the DB in all cases
        if (!file.exists()) {
            try {
                dao.delete(uploadedFile);
                if (!inTransaction) {
                    HibernateUtil.commitTransaction();
                }
            }
            catch  (Throwable t) {
                deleted = false;
                //possible error updating the DB
                log.error("Error deleting file record from db, path="+uploadedFile.getPath(), t);
            }
            finally {
                if (!inTransaction) {
                    HibernateUtil.rollbackTransaction();
                } 
            }
        } 
        return deleted;
    }
    
    public static boolean canDelete(UploadFile uf) {
        if (uf == null) {
            return false;
        }
        File toDel = new File(uf.getPath());
        if (toDel.isDirectory()) {
            //TODO: log.error("Deleting directories not implemented");
            return false;
        }
        String userid = UIBeanHelper.getUserId();
        if (userid == null) {
            //require a userid
            return false;
        }

        //TODO: come up with an improved policy for ACL for admin users
        boolean isAdmin = false;
        isAdmin = AuthorizationHelper.adminJobs(userid);
        if (isAdmin) {
            return true;
        }
        
        if (!toDel.canWrite()) {
            //TODO: server error
            return false;
        }
        
        return userid.equals(uf.getUserId());
    }
    
    private static void handleFileSync(UploadFileDAO dao, File file, String user) throws IOException {
        UploadFile uploadFile = new UploadFile();
        uploadFile.initFromFile(file, UploadFile.COMPLETE);
        uploadFile.setUserId(user);
        dao.saveOrUpdate(uploadFile);
        
        if (file.isDirectory()) {
            for (File i : file.listFiles()) {
                handleFileSync(dao, i, user);
            }
        }
    }
    
    public static void syncUploadFiles(String user) {
        try {
            UploadFileDAO dao = new UploadFileDAO();
            List<UploadFile> userFiles = dao.findByUserId(user);
            File uploadDir = getUserUploadDirectory(user);
            if (uploadDir == null) {
                log.error("Unable to get the user's upload directory in syncUploadFiles()");
                return;
            }
            
            // Remove all the old database entries
            for (UploadFile i : userFiles) {
                dao.delete(i);
            }
            HibernateUtil.commitTransaction();
            dao = new UploadFileDAO();
            // Add new entries to the database
            for (File i : uploadDir.listFiles()) {
                handleFileSync(dao, i, user);
            }
            
            // Commit
            HibernateUtil.commitTransaction();
        }
        catch (Exception e) {
            log.error("Error committing upload file sync to database");
            HibernateUtil.rollbackTransaction();
        }
    }

    public static File getUserUploadDirectory(String user) {
        return ServerConfiguration.instance().getUserUploadDir(Context.getContextForUser(user));
    }
}
