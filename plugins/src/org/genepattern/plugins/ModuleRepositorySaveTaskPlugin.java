/*
 The Broad Institute
 SOFTWARE COPYRIGHT NOTICE AGREEMENT
 This software and its documentation are copyright (2003-2006) by the
 Broad Institute/Massachusetts Institute of Technology. All rights are
 reserved.

 This software is supplied without any warranty or guaranteed support
 whatsoever. Neither the Broad Institute nor MIT can be responsible for its
 use, misuse, or functionality.
 */

package org.genepattern.plugins;

import edu.mit.genome.gp.util.ZipCatalogUpload;
import org.genepattern.server.webservice.server.SaveTaskPlugin;

import java.io.File;
import java.io.IOException;

/**
 * @author Joshua Gould
 */
public class ModuleRepositorySaveTaskPlugin implements SaveTaskPlugin {
    static String url = "http://wwwdev.broad.mit.edu/webservices/3rdpartymodulerepository/uploadForm.jsp";

    public void taskSaved(File zipFile) {
        ZipCatalogUpload zipCatalogUpload = new ZipCatalogUpload();
        try {
            zipCatalogUpload.upload(url, ZipCatalogUpload.MODULE, ZipCatalogUpload.DEV, zipFile.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
