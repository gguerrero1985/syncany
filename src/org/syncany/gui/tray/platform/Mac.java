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
package org.syncany.gui.tray.platform;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.syncany.Constants;
import org.syncany.config.Config;
import org.syncany.config.Folder;
import org.syncany.config.Profile;
import org.syncany.exceptions.ConfigException;
import org.syncany.exceptions.InitializationException;
import org.syncany.gui.linux.UpdateStatusTextRequest;
import org.syncany.gui.tray.Tray;
import org.syncany.gui.tray.TrayEvent;
import org.syncany.gui.tray.TrayEvent.EventType;
import org.syncany.gui.tray.TrayEventListener;
import org.syncany.gui.tray.TrayIconStatus;

/**
 *
 * @author Guillermo Guerrero
 */
public class Mac extends Tray {

    private SystemTray tray;
    private PopupMenu menu;
    private TrayIcon icon;
    private MenuItem itemStatus, itemPreferences, itemWebsite, itemDonate, itemQuit;

    private TrayIconStatus status;
    private JFrame frame;

    public Mac() {
        super();

        // cp. init
        this.menu = null;
        this.status = new TrayIconStatus(new TrayIconStatus.TrayIconStatusListener() {
            @Override
            public void trayIconUpdated(String filename) {
                if (config != null) {
                    setIcon(new File(config.getResDir() + File.separator
                            + Constants.TRAY_DIRNAME + File.separator + filename));
                }
            }
        });
    }

    @Override
    public void notify(String summary, String body, File imageFile) {

        frame = new JFrame();
        frame.setSize(300, 125);
        frame.setUndecorated(true);

        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();// size of the screen
        Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());// height of the task bar
        frame.setLocation(scrSize.width - frame.getWidth(), toolHeight.top + toolHeight.bottom);

        frame.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0f;
        constraints.weighty = 1.0f;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.BOTH;

        JLabel headingLabel = new JLabel(summary);
        if (imageFile != null) {
            Icon headingIcon = new ImageIcon(imageFile.getPath());
            headingLabel.setIcon(headingIcon); // --- use image icon you want to be as heading image.            
        }
        headingLabel.setOpaque(false);
        frame.add(headingLabel, constraints);

        constraints.gridx++;
        constraints.weightx = 0f;
        constraints.weighty = 0f;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTH;

        JButton cloesButton = new JButton(new AbstractAction("X") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                frame.dispose();
            }
        });

        cloesButton.setMargin(new Insets(1, 4, 1, 4));
        cloesButton.setFocusable(false);

        frame.add(cloesButton, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weightx = 1.0f;
        constraints.weighty = 1.0f;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.BOTH;
        JLabel messageLabel = new JLabel("<HtMl>" + body);
        frame.add(messageLabel, constraints);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);

        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3500); // time after which pop up will be disappeared.
                    frame.dispose();
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        ;
    }

    .start();  
    }

    @Override
    public void updateUI() {
        //initMenu();
    }

    @Override
    public void updateStatusText() {

        UpdateStatusTextRequest menu = new UpdateStatusTextRequest(processesText);
        synchronized (itemStatus) {
            itemStatus.setLabel(menu.getStatusText());
        }
    }

    @Override
    public void setStatusIconPlatform(StatusIcon s) {
        status.setIcon(s);
    }

    private void setIcon(File file) {
        icon.setImage(Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath()));
    }

    @Override
    public void init(String initialMessage) throws InitializationException {
        initMenu(initialMessage);
        initIcon();
    }

    @Override
    public void destroy() {
        // Nothing.
    }

    private void initMenu(String initialMessage) {
        // Create
        menu = new PopupMenu();

        // Status
        itemStatus = new MenuItem("Everything is up to date");
        itemStatus.setEnabled(false);

        menu.add(itemStatus);

        // Profiles and folders
        List<Profile> profiles = config.getProfiles().list();

        menu.addSeparator();

        if (profiles.size() == 1) {
            Profile profile = profiles.get(0);

            for (final Folder folder : profile.getFolders().list()) {
                if (!folder.isActive() || folder.getLocalFile() == null) {
                    continue;
                }

                MenuItem itemFolder = new MenuItem(folder.getLocalFile().getName());

                itemFolder.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        fireTrayEvent(new TrayEvent(EventType.OPEN_FOLDER, folder.getLocalFile().getAbsolutePath()));
                    }
                });

                menu.add(itemFolder);
            }

            menu.addSeparator();
        } else if (profiles.size() > 1) {
            for (Profile profile : profiles) {
                Menu itemProfile = new Menu(profile.getName());

                for (final Folder folder : profile.getFolders().list()) {
                    if (!folder.isActive() || folder.getLocalFile() == null) {
                        continue;
                    }

                    MenuItem itemFolder = new MenuItem(folder.getLocalFile().getName());

                    itemFolder.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            fireTrayEvent(new TrayEvent(EventType.OPEN_FOLDER, folder.getLocalFile().getAbsolutePath()));
                        }
                    });

                    itemProfile.add(itemFolder);
                }

                menu.add(itemProfile);
            }

            menu.addSeparator();
        }

        // Preferences
        itemPreferences = new MenuItem("Preferences ...");
        itemPreferences.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                fireTrayEvent(new TrayEvent(EventType.PREFERENCES));
            }
        });

        menu.add(itemPreferences);

        // Donate!
        itemDonate = new MenuItem("Donate ...");
        itemDonate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                fireTrayEvent(new TrayEvent(EventType.DONATE));
            }
        });

        menu.add(itemDonate);

        // Quit
        menu.addSeparator();

        itemQuit = new MenuItem("Quit");
        itemQuit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                fireTrayEvent(new TrayEvent(EventType.QUIT));
            }
        });

        menu.add(itemQuit);
    }

    private void initIcon() throws InitializationException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            throw new InitializationException("Unable to set look and feel for tray icon", e);
        }

        tray = SystemTray.getSystemTray();
        
        File defaultIconFile = new File(Config.getInstance().getResDir()+File.separator+
                Constants.TRAY_DIRNAME+File.separator+Constants.TRAY_FILENAME_DEFAULT);

        Image image = Toolkit.getDefaultToolkit().getImage(defaultIconFile.getAbsolutePath());

        icon = new TrayIcon(image, "syncany", menu);
        icon.setImageAutoSize(true);
        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Profile profile = config.getProfiles().list().get(0);
                    Folder folder = profile.getFolders().list().get(0);
                    fireTrayEvent(new TrayEvent(TrayEvent.EventType.OPEN_FOLDER, folder.getLocalFile().getAbsolutePath()));
                }
            }
        });
               
        try {
            tray.add(icon);
        } catch (AWTException e) {
            throw new InitializationException("Unable to add tray icon.", e);
        }      
    }

    public static void main(String[] args) throws ConfigException, InitializationException, InterruptedException {
        System.out.println("STARTED");

        config.load();
        Tray tray = Tray.getInstance();
        tray.registerProcess(tray.getInstance().getClass().getSimpleName());
        tray.init("Everything is up to date.");

        //File imageFile = null;
        File imageFile = new File(config.getResDir() + File.separator + "logo48.png");
        tray.notify("hello asdas dasd dasd asd ", "test asdsad sd asd sa", imageFile);
        //tray.setStatus(Status.UPDATING);
        tray.addTrayEventListener(new TrayEventListener() {

            @Override
            public void trayEventOccurred(TrayEvent event) {
                System.out.println(event);
            }
        });
        tray.setStatusIcon(tray.getInstance().getClass().getSimpleName(), StatusIcon.UPDATING);
        //System.out.println(FileUtil.showBrowseDirectoryDialog());

        Thread.sleep(5000);
    }
}
