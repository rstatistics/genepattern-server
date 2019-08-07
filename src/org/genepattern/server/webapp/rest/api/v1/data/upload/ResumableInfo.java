package org.genepattern.server.webapp.rest.api.v1.data.upload;

import java.io.File;
import java.util.HashSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ResumableInfo {
    public int      resumableChunkSize;
    public long     resumableTotalSize;
    public String   resumableIdentifier;
    public String   resumableFilename;
    public String   resumableRelativePath;

    public static class ResumableChunkNumber {
        public ResumableChunkNumber(int number) {
            this.number = number;
        }

        public int number;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ResumableChunkNumber
                    ? ((ResumableChunkNumber)obj).number == this.number : false;
        }

        @Override
        public int hashCode() {
            return number;
        }
    }

    //Chunks uploaded
    public HashSet<ResumableChunkNumber> uploadedChunks = new HashSet<ResumableChunkNumber>();

    public String resumableFilePath;

    public boolean vaild(){
        if (resumableChunkSize < 0 || resumableTotalSize < 0
                || ResumableHttpUtils.isEmpty(resumableIdentifier)
                || ResumableHttpUtils.isEmpty(resumableFilename)
                || ResumableHttpUtils.isEmpty(resumableRelativePath)) {
            return false;
        } else {
            return true;
        }
    }
    public boolean checkIfUploadFinished(File destination) {
        //check if upload finished
        int count = (int) Math.ceil(((double) resumableTotalSize) / ((double) resumableChunkSize));
        for(int i = 1; i < count; i ++) {
            if (!uploadedChunks.contains(new ResumableChunkNumber(i))) {
                return false;
            }
        }

        //Upload finished, change filename.
        File file = new File(resumableFilePath);

        System.out.println("UPLOAD FINISHED - " + file.getAbsolutePath());
        
        file.renameTo(destination);
        System.out.println("UPLOAD RENAMED - " + destination.getAbsolutePath());
        
        return true;
    }
}