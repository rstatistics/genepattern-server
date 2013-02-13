package org.genepattern.server.job.input;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

import org.genepattern.server.dm.GpFilePath;

/**
 * Interface for writing parameter list files to be used as input to modules.
 * 
 * @author pcarr
 *
 */
public interface ParamListWriter {
    void writeParamList(GpFilePath toFile, List<GpFilePath> values) throws Exception;
    
    /**
     * Default implementation of the ParamListWriter class, outputs a new text file,
     * where each line is the fully qualified path to a data file.
     * 
     * @author pcarr
     */
    public static class Default implements ParamListWriter {
        private final String COL_DELIM="\t";
        private boolean writeTimestamp=false;
        
        /**
         * Write a new parameter list file from the list of values.
         * 
         */
        @Override
        public void writeParamList(GpFilePath toFile, List<GpFilePath> values) throws Exception {
            FileWriter writer = null;
            BufferedWriter out = null;
            try {
                writer = new FileWriter(toFile.getServerFile());
                out = new BufferedWriter(writer);
                for(GpFilePath filePath : values) {
                    File file = filePath.getServerFile();
                    out.write(file.getAbsolutePath());
                    if (writeTimestamp) {
                        out.write(COL_DELIM); out.write("timestamp="+file.lastModified());
                        out.write(COL_DELIM); out.write(" date="+new Date(file.lastModified())+" ");
                    }
                    out.newLine();
                }
            }
            finally {
                if (out != null) {
                    out.close();
                }
            }
        }
        
    }

}