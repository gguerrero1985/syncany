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
package org.syncany.gui.tray;

import org.syncany.config.Config;
import org.syncany.Environment;
import org.syncany.exceptions.InitializationException;
import org.syncany.gui.tray.platform.Linux;
import org.syncany.gui.tray.platform.Windows;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.syncany.gui.tray.platform.Mac;

/**
 *
 * @author Philipp C. Heckel
 * @author Guillermo Guerrero
 */
public abstract class Tray {

    public enum StatusIcon {

        DISCONNECTED, UPDATING, UPTODATE
    };

    protected static Tray instance = null;
    protected static final Logger logger = Logger.getLogger(Tray.class.getSimpleName());
    protected static final Config config = Config.getInstance();
    protected static final Environment env = Environment.getInstance();

    protected List<TrayEventListener> listeners;
    protected HashMap<String, StatusIcon> processesIcon;
    protected HashMap<String, String> processesText;
    private StatusIcon cachedStatus;

    private boolean startDemonOnly;

    protected Tray() {
        logger.log(Level.INFO, "{0}#Creating tray ...", config.getMachineName());

        cachedStatus = StatusIcon.DISCONNECTED;
        listeners = new ArrayList<TrayEventListener>();

        processesIcon = new HashMap<String, StatusIcon>();
        processesText = new HashMap<String, String>();

        this.startDemonOnly = false;
    }

    public void addTrayEventListener(TrayEventListener listener) {
        listeners.add(listener);
    }

    public void removeTrayEventListener(TrayEventListener listener) {
        listeners.remove(listener);
    }

    public static Tray getInstance() {
        if (instance != null) {
            return instance;
        }

        if (env.getOperatingSystem() == Environment.OperatingSystem.Linux) {
            instance = new Linux();
            return instance;
        } else if (env.getOperatingSystem() == Environment.OperatingSystem.Windows) {
            instance = new Windows();
            return instance;
        } else if (env.getOperatingSystem() == Environment.OperatingSystem.Mac) {
            instance = new Mac();
            return instance;
        }

        throw new RuntimeException("Your OS is currently not supported: " + System.getProperty("os.name"));
    }

    protected void fireTrayEvent(TrayEvent event) {
        for (TrayEventListener l : listeners) {
            l.trayEventOccurred(event);
        }
    }

    public void registerProcess(String processName) {
        if (!processesIcon.containsKey(processName)) {
            processesIcon.put(processName, StatusIcon.UPTODATE);
        }

        if (!processesText.containsKey(processName)) {
            processesText.put(processName, "");
        }
    }
    
    public String setStatusText(String processName, String message){
        if(!startDemonOnly){
            if(processesText.containsKey(processName)){
                processesText.put(processName, message);
            }

            //updateUI();
            updateStatusText();
        }
        
        return message;
    }

    public synchronized StatusIcon setStatusIcon(String processName, StatusIcon status) {                
        if(!startDemonOnly){        
            if(processesIcon.containsKey(processName)){
                processesIcon.put(processName, status);
            }

            StatusIcon finalStatus = StatusIcon.UPTODATE;        
            for(Map.Entry<String, StatusIcon> entry: processesIcon.entrySet()){

                if( entry.getValue() == StatusIcon.UPDATING){
                    finalStatus = StatusIcon.UPDATING;
                } else if( entry.getValue() == StatusIcon.DISCONNECTED){
                    finalStatus = StatusIcon.DISCONNECTED;
                    break;
                }            
            }        

            if (cachedStatus != null && cachedStatus == finalStatus) {
                // Nothing to send!
                return cachedStatus;
            }

            cachedStatus = status;
            setStatusIconPlatform(cachedStatus);
            return cachedStatus;
        } else{
            return status;
        }
    }

    public abstract void updateUI();
    public abstract void updateStatusText();
    public abstract void setStatusIconPlatform(StatusIcon status);    
    public abstract void init(String initialMessage) throws InitializationException;
    public abstract void destroy();
    public abstract void notify(String summary, String body, File imageFile);
}
