package org.genepattern.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

/**
 * Utility class for managing the loading of properties from the genepattern.properties file, custom.properties file, et cetera.
 * This class should be used to replace all calls to System.getProperty() from the source code.
 * Once that has occurred it is fairly straightforward to allow reloading the properties without having to do a server restart.
 * 
 * The following properties files are loaded, in order, after reading System.properties:
 *     genepattern.properties, custom.properties, and build.properties.
 * 
 * 
 * @author pcarr
 */
public class GpServerProperties {
    private static Logger log = Logger.getLogger(GpServerProperties.class);

    private static class Record {
        private final File propFile;
        private long dateLoaded = System.currentTimeMillis();
        private final Properties props=new Properties();
        
        public Record(final Properties from) {
            this.propFile=null;
            this.dateLoaded=System.currentTimeMillis();
            this.props.putAll(from);
        }

        public Record(final File propFile) {
            if (propFile==null) {
                throw new IllegalArgumentException("propFile==null");
            }
            this.propFile=propFile;
            reloadProps();
        }
        
        public Properties getProperties() {
            return props;
        }
        
        public long getDateLoaded() {
            return dateLoaded;
        }
        
        public void reloadProps() {
            props.clear();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(propFile);
                props.load(fis);
                dateLoaded = System.currentTimeMillis();
            }
            catch (FileNotFoundException e) {
                log.error("FileNotFound: "+propFile.getAbsolutePath(), e);
                return;
            }
            catch (IOException e) {
                log.error("IOException reading file="+propFile.getAbsolutePath(), e);
                return;
            }
            catch (Throwable t) {
                log.error("unexpected error reading file="+propFile.getAbsolutePath(), t);
                return;
            }
            finally {
                if (fis != null) {
                    try {
                        fis.close();
                    }
                    catch (IOException e) {
                        log.error(e);
                    }
                }
            }
        }
    }

    private Map<String,Record> propertiesList = new LinkedHashMap<String,Record>();

    private final boolean useSystemProperties; // default = true;
    private final boolean usePropertiesFiles;  // default = true;
    private final File resourcesDir; // can be null
    
    private final Map<String,String> serverProps;
    
    private GpServerProperties(final Builder in) {
        log.debug("initializing GpServerProperties...");
        this.useSystemProperties=in.useSystemProperties;
        this.usePropertiesFiles=in.usePropertiesFiles;
        this.resourcesDir=in.resourcesDir;
        
        final Properties flattened=new Properties();
        if (in.initFromSystemProperties) {
            log.debug("loading system properties...");
            Record systemProps=new Record(System.getProperties());
            if (log.isDebugEnabled()) {
                log.debug("system.properties loaded at "+new Date(systemProps.getDateLoaded()));
            }
            propertiesList.put("system.properties", systemProps);
            flattened.putAll(systemProps.getProperties());
        }
        if (in.gpPropertiesFile != null) {
            log.debug("loading genepattern.properties from file="+in.gpPropertiesFile);
            Record gpProps=new Record(in.gpPropertiesFile);
            propertiesList.put("genepattern.properties", gpProps);
            flattened.putAll(gpProps.getProperties());
        }
        if (in.customPropertiesFile != null) {
            log.debug("loading custom.properties from file="+in.customPropertiesFile);
            Record customProps=new Record(in.customPropertiesFile);
            propertiesList.put("custom.properties", customProps);
            flattened.putAll(customProps.getProperties());
        }
        if (in.customProperties != null) {
            flattened.putAll(in.customProperties);
        }
        if (in.buildPropertiesFile != null) {
            log.debug("loading build.properties from file="+in.buildPropertiesFile);
            Record buildProps=new Record(in.buildPropertiesFile);
            propertiesList.put("build.properties", buildProps);
            flattened.putAll(buildProps.getProperties());
        }
        this.serverProps=Maps.fromProperties(flattened);
    }
    
    public File getResourcesDir() {
        return resourcesDir;
    }
    
    public String getProperty(final String key) {
        return getProperty(key, this.usePropertiesFiles, this.useSystemProperties);
    }
    
    public String getProperty(final String key, final String defaultValue) {
        String rval=getProperty(key);
        if (rval==null) {
            return defaultValue;
        }
        return rval;
    }
    
    private String getProperty(final String key, final boolean usePropertiesFiles, final boolean useSystemProperties) {
        if (usePropertiesFiles) {
            if (serverProps.containsKey(key)) {
                return serverProps.get(key);
            }
        }
        if (useSystemProperties) {
            return System.getProperty(key);
        }
        return null;
    }
    
    public Value getValue(GpContext context, final String key) {
        if (context==null) {
            context=GpContext.getServerContext();
        }
        final String prop=getProperty(key, context.getCheckPropertiesFiles(), context.getCheckSystemProperties());
        if (prop==null) {
            return null;
        }
        return new Value(prop);
    }
    
    public static final class Builder {
        private boolean initFromSystemProperties = true;
        private boolean useSystemProperties = true;
        private boolean usePropertiesFiles = true;
        private File resourcesDir=null;
        private File gpPropertiesFile=null;
        private File customPropertiesFile=null;
        private File buildPropertiesFile=null;
        private Properties customProperties=null;

        public Builder initFromSystemProperties(final boolean initFromSystemProperties) {
            this.initFromSystemProperties=initFromSystemProperties;
            return this;
        }
        public Builder useSystemProperties(final boolean useSystemProperties) {
            this.useSystemProperties=useSystemProperties;
            return this;
        }
        public Builder usePropertiesFiles(final boolean usePropertiesFiles) {
            this.usePropertiesFiles=usePropertiesFiles;
            return this;
        }
        public Builder resourcesDir(final File resourcesDir) {
            this.resourcesDir=resourcesDir;
            return this;
        }
        public Builder gpProperties(final File gpProperties) {
            this.gpPropertiesFile=gpProperties;
            return this;
        }
        
        public Builder customProperties(final File customProperties) {
            this.customPropertiesFile=customProperties;
            return this;
        }
        
        public Builder buildProperties(final File buildProperties) {
            this.buildPropertiesFile=buildProperties;
            return this;
        }
        
        public Builder addCustomProperty(final String key, final String value) {
            if (customProperties==null) {
                customProperties=new Properties();
            }
            customProperties.setProperty(key, value);
            return this;
        }
        
        public Builder addCustomProperties(final Properties props) {
            if (customProperties==null) {
                customProperties=new Properties();
            }
            for (Enumeration<?> propertyNames = props.propertyNames(); propertyNames.hasMoreElements(); ) {
                Object key = propertyNames.nextElement();
                customProperties.put(key, props.get(key));
            }
            return this;
        }
        
        public GpServerProperties build() throws IllegalArgumentException {
            if (gpPropertiesFile==null && resourcesDir != null) {
                gpPropertiesFile=new File(resourcesDir, "genepattern.properties");
            }
            if (customPropertiesFile==null && resourcesDir != null) {
                customPropertiesFile=new File(resourcesDir, "custom.properties");
            }
            if (buildPropertiesFile==null && resourcesDir != null) {
                buildPropertiesFile=new File(resourcesDir, "build.properties");
            }
            return new GpServerProperties(this);
        }
    }

}
