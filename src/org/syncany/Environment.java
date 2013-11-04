/*
 * Syncany, www.syncany.org
 * Copyright (C) 2011 Philipp C. Heckel <philipp.heckel@gmail.com> 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.syncany;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.syncany.config.ConfigNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Philipp C. Heckel
 * @author Guillermo Guerrero
 */
public class Environment {

    private static final Logger logger = Logger.getLogger(Environment.class.getSimpleName());
    private static Environment instance;

    public enum OperatingSystem {

        Windows, Linux, Mac
    };

    private OperatingSystem operatingSystem;
    private String architecture;

    private String defaultUserHome;
    private File defaultUserConfDir;
    private File defaultUserConfigFile;

    private File appDir;
    private File appBinDir;
    private File appResDir;
    private File appConfDir;
    private File appLibDir;

    /**
     * Local computer name / host name.
     */
    private static String machineName;

    /**
     * Local user name (login-name).
     */
    private static String userName;

    public synchronized static Environment getInstance() {
        if (instance == null) {
            instance = new Environment();
        }

        return instance;
    }

    private String getDefaultUserDir() {
        String path = "";
        if (System.getProperty("user.dir") != null) {
            path = System.getProperty("user.dir");

            File tryPath = new File(path);
            if (!tryPath.exists()) {
                throw new RuntimeException("Property 'user.dir' doesn't exist: " + tryPath);
            }
        } else {
            throw new RuntimeException("Property 'user.dir' must be set.");
        }

        return path;
    }

    private Environment() {
        String homePath;
        // Check must-haves
        if (System.getProperty("stacksync.home") == null) {
            homePath = getDefaultUserDir();
        } else {
            homePath = System.getProperty("stacksync.home");
            File tryPath = new File(homePath);
            if (!tryPath.exists()) {
                throw new RuntimeException("Property 'stacksync.home' must be set.");
            }
        }

        // Architecture
        if ("32".equals(System.getProperty("sun.arch.data.model"))) {
            architecture = "i386";
        } else if ("64".equals(System.getProperty("sun.arch.data.model"))) {
            architecture = "x84_64";
        } else {
            throw new RuntimeException("Syncany only supports 32bit and 64bit systems, not '" + System.getProperty("sun.arch.data.model") + "'.");
        }

        // Do initialization!
        defaultUserHome = System.getProperty("user.home") + File.separator;

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("linux")) {
            operatingSystem = OperatingSystem.Linux;
            defaultUserConfDir = new File(defaultUserHome + "." + Constants.APPLICATION_NAME.toLowerCase());
        } else if (osName.contains("windows")) {
            operatingSystem = OperatingSystem.Windows;
            if (osName.contains("xp")) {//windows xp
                defaultUserConfDir = new File(defaultUserHome + "Application Data" + File.separator + Constants.APPLICATION_NAME.toLowerCase());
            } else { //windows 7, 8
                defaultUserConfDir = new File(defaultUserHome + "AppData" + File.separator + "Roaming" + File.separator + Constants.APPLICATION_NAME.toLowerCase());
            }
        } else if (osName.contains("mac os x")) {
            operatingSystem = OperatingSystem.Mac;
            defaultUserConfDir = new File(defaultUserHome + "." + Constants.APPLICATION_NAME.toLowerCase());
        } else {
            throw new RuntimeException("Your system is not supported at the moment: " + System.getProperty("os.name"));
        }

        // Errors
        appDir = new File(homePath);
        if (!appDir.exists()) {
            throw new RuntimeException("Could not find application directory at " + appResDir);
        }

        appBinDir = new File(appDir.getAbsoluteFile() + File.separator + "bin");
        if (!appBinDir.exists()) {
            throw new RuntimeException("Could not find application binaries directory at " + appResDir);
        }

        appResDir = new File(appDir.getAbsoluteFile() + File.separator + "res");
        if (!appResDir.exists()) {
            throw new RuntimeException("Could not find application resources directory at " + appResDir);
        }

        appConfDir = new File(appDir.getAbsoluteFile() + File.separator + "conf");
        if (!appConfDir.exists()) {
            throw new RuntimeException("Could not find application config directory at " + appConfDir);
        }

        appLibDir = new File(appDir.getAbsoluteFile() + File.separator + "lib");
        if (!appLibDir.exists()) {
            throw new RuntimeException("Could not find application library directory at " + appLibDir);
        }

        // Machine stuff        
        java.util.Date date = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmm");

        String defaultMachineName;
        try {
            defaultMachineName = InetAddress.getLocalHost().getHostName();

            if (defaultMachineName.length() > 10) {
                defaultMachineName = InetAddress.getLocalHost().getHostName().substring(0, 9);
            }
            defaultMachineName += sdf.format(date);
        } catch (UnknownHostException ex) {
            logger.log(Level.SEVERE, "Cannot find host {0}", ex);
            defaultMachineName = "(unknown)" + sdf.format(date);
        }

        // Common values
        defaultUserConfigFile = new File(defaultUserConfDir.getAbsoluteFile() + File.separator + Constants.CONFIG_FILENAME);
        if (defaultUserConfigFile.exists()) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                Document doc = dBuilder.parse(defaultUserConfigFile);
                ConfigNode self = new ConfigNode(doc.getDocumentElement());
                machineName = self.getProperty("machinename", defaultMachineName);

                if (machineName.isEmpty()) {
                    machineName = defaultMachineName;
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "aplicationstarter#ERROR: cant set machineName {0}", ex);
                machineName = defaultMachineName;
            } catch (ParserConfigurationException ex) {
                logger.log(Level.SEVERE, "aplicationstarter#ERROR: cant set machineName {0}", ex);
                machineName = defaultMachineName;
            } catch (SAXException ex) {
                logger.log(Level.SEVERE, "aplicationstarter#ERROR: cant set machineName {0}", ex);
                machineName = defaultMachineName;
            }
        } else {
            machineName = defaultMachineName;
        }

        machineName = machineName.replace("-", "_");
        userName = System.getProperty("user.name");

        // GUI 
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //java.util.Enumeration keys = UIManager.getDefaults().keys();

            /*while (keys.hasMoreElements()) {
             Object key = keys.nextElement();
             Object value = UIManager.get (key);
                
             if (value instanceof FontUIResource) {
             FontUIResource f = (FontUIResource) value;
             f = new FontUIResource(f.getFamily(), f.getStyle(), f.getSize()-2);
             System.out.println(key +" = "+value);
             UIManager.put (key, f);
              
             }
             }*/
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Couldn't set native look and feel.", ex);
        } catch (IllegalAccessException ex) {
            logger.log(Level.SEVERE, "Couldn't set native look and feel.", ex);
        } catch (InstantiationException ex) {
            logger.log(Level.SEVERE, "Couldn't set native look and feel.", ex);
        } catch (UnsupportedLookAndFeelException ex) {
            logger.log(Level.SEVERE, "Couldn't set native look and feel.", ex);
        }
    }

    public File getAppConfDir() {
        return appConfDir;
    }

    public File getAppDir() {
        return appDir;
    }

    public File getAppBinDir() {
        return appBinDir;
    }

    public File getAppResDir() {
        return appResDir;
    }

    public File getAppLibDir() {
        return appLibDir;
    }

    public File getDefaultUserConfigFile() {
        return defaultUserConfigFile;
    }

    public File getDefaultUserConfigDir() {
        return defaultUserConfDir;
    }

    public String getMachineName() {
        return machineName.replace("-", "_");
    }

    public String getUserName() {
        return userName;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public String getArchitecture() {
        return architecture;
    }

    public HashMap<String, String> getProperties() {
        HashMap<String, String> properties = new HashMap<String, String>();

        properties.put("operatingSystem", operatingSystem.toString());
        properties.put("architecture", architecture);
        properties.put("defaultUserHome", defaultUserHome);
        properties.put("defaultUserConfDir", defaultUserConfDir.getAbsolutePath());
        properties.put("defaultUserConfigFile", defaultUserConfigFile.getAbsolutePath());

        properties.put("appDir", appDir.getAbsolutePath());
        properties.put("appBinDir", appBinDir.getAbsolutePath());
        properties.put("appResDir", appResDir.getAbsolutePath());
        properties.put("appConfDir", appConfDir.getAbsolutePath());
        properties.put("appLibDir", appLibDir.getAbsolutePath());

        properties.put("machineName", machineName);
        properties.put("userName", userName);

        return properties;
    }
}
