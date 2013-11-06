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

import org.syncany.gui.error.ErrorDialog;
import org.syncany.config.Config;
import org.syncany.exceptions.ConfigException;
import org.syncany.exceptions.InitializationException;
import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.syncany.util.StringUtil;

/**
 * Main class for the Syncany client.
 *
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 * @author Guillermo Guerrero
 */
public class Syncany {

    private static final Config config = Config.getInstance();
    private static final Environment env = Environment.getInstance();
    private static final CommandLineParser parser = new PosixParser();

    private static String[] args;

    private static void showHelp(Options options) {
        HelpFormatter h = new HelpFormatter();
        h.printHelp("java -jar Syncany.jar", options);
        System.exit(0);
    }

    private static Options createOptions() {
        // create the Options
        Options options = new Options();

        options.addOption("d", "daemon", false, "To use Syncany with a daemon only.");
        options.addOption("c", "config", true, "Alternative path o config.xml file (Default: ~/.syncany)");
        options.addOption("h", "help", false, "Print this message.");

        return options;
    }

    /**
     * @param args Command line arguments for the Syncany client See '--help'
     * @throws org.syncany.exceptions.ConfigException
     * @throws org.syncany.exceptions.InitializationException
     */
    public static void main(String[] args) throws ConfigException, InitializationException {
        Syncany.args = args; // Required for restart
        Syncany.start();
    }

    public static void start() {
        Boolean startDemonOnly = false;

        try {
            // create the Options                        
            Options options = createOptions();

            // create the command line parser
            CommandLine line = parser.parse(options, args);

            // Help
            if (line.hasOption("help")) {
                showHelp(options);
            }

            startDemonOnly = line.hasOption("daemon");

            Application app = new Application(startDemonOnly);
            
            // Load config
            if (line.hasOption("config")) {
                File configFolder = new File(line.getOptionValue("config"));
                File configFile = new File(line.getOptionValue("config") + File.separator + "config.xml");

                if (configFolder.exists() && configFile.exists()) {
                    config.load(configFolder);
                } else {
                    if (!configFolder.exists()) {
                        throw new ConfigException("config folder " + configFolder + " doesn't exist.");
                    } else {
                        throw new ConfigException(configFile + " doesn't exist.");
                    }
                }
            } else {
                File configurationDir = env.getAppConfDir();
                if (configurationDir.exists()) {
                    config.load();
                    if (config.getProfiles().list().isEmpty()) {
                        File folder = new File(config.getConfDir() + File.separator + Constants.CONFIG_DATABASE_DIRNAME);
                        File configFile = new File(config.getConfDir() + File.separator + Constants.CONFIG_FILENAME);

                        folder.delete();
                        configFile.delete();
                        
                        app.initFirstTimeWizard();
                        config.load();
                    }
                } else { // new configuration
                    config.load();
                }
            }

            if (config.getProfiles().list().isEmpty()) {
                throw new ConfigException("Could not load a profile, check the configuration file.");
            }
            
            app.start();
        } catch (ConfigException e) {
            System.err.println("ERROR: Configuration exception: " + e.getMessage());
            System.err.println(StringUtil.getStackTrace(e));
            System.exit(1);
        } catch (ParseException e) {
            System.err.println("ERROR: Command line arguments invalid: " + e.getMessage());
            System.err.println(StringUtil.getStackTrace(e));
            System.exit(1);
        } catch (InitializationException e) {
            if (startDemonOnly) {
                System.err.println("ERROR: " + e.getMessage());
                System.err.println(StringUtil.getStackTrace(e));
                System.exit(1);
            } else {
                ErrorDialog.showDialog(e);
            }
        }
    }

}
