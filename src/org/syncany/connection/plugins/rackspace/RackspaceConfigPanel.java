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
package org.syncany.connection.plugins.rackspace;

import java.util.ResourceBundle;
import org.jdesktop.application.Action;

import org.syncany.config.Config;
import org.syncany.connection.plugins.ConfigPanel;

/**
 *
 * @author Philipp C. Heckel <philipp.heckel@gmail.com>
 */
public class RackspaceConfigPanel extends ConfigPanel {
	
	private ResourceBundle resourceBundle;
	
    public RackspaceConfigPanel(RackspaceConnection connection) {
        super(connection);
        resourceBundle = Config.getInstance().getResourceBundle();
        initComponents();	
    }

    @Override
    public void load() {
        txtUsername.setText(getConnection().getUsername());
        txtApiKey.setText(getConnection().getApiKey());
        txtContainerName.setText(getConnection().getContainer());
        if(getConnection().getAuthServer().equals("US"))
            txtAuthServer.setSelected(authUs.getModel(), true);
        else
            txtAuthServer.setSelected(authUk.getModel(), true);
    }

    @Override
    public void save() {
        getConnection().setUsername(txtUsername.getText());
        getConnection().setApiKey(new String(txtApiKey.getPassword()));
        getConnection().setContainer(txtContainerName.getText());
        
        String authServer;
        if(txtAuthServer.getSelection() == authUs.getModel())
            authServer = "US";
        else
            authServer = "UK";
        getConnection().setAuthServer(authServer);
    }

    @Override
    public RackspaceConnection getConnection() {
        return (RackspaceConnection) super.getConnection();
    }       
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtAuthServer = new javax.swing.ButtonGroup();
        txtUsername = new javax.swing.JTextField();
        txtApiKey = new javax.swing.JPasswordField();
        txtContainerName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        authUs = new javax.swing.JRadioButton();
        authUk = new javax.swing.JRadioButton();

        txtUsername.setName("txtUsername"); // NOI18N

        txtApiKey.setName("txtApiKey"); // NOI18N

        txtContainerName.setName("txtContainerName"); // NOI18N

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText("Container Name:");
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Username:");
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("API Key:");
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText("Auth Server:");
        jLabel4.setName("jLabel4"); // NOI18N

        txtAuthServer.add(authUs);
        authUs.setSelected(true);
        authUs.setText("US");
        authUs.setName("authUs"); // NOI18N

        txtAuthServer.add(authUk);
        authUk.setText("UK");
        authUk.setName("authUk"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .addComponent(txtApiKey, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .addComponent(txtContainerName, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(authUs)
                        .addGap(18, 18, 18)
                        .addComponent(authUk)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtApiKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtContainerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(authUs)
                    .addComponent(authUk))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton authUk;
    private javax.swing.JRadioButton authUs;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPasswordField txtApiKey;
    private javax.swing.ButtonGroup txtAuthServer;
    private javax.swing.JTextField txtContainerName;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables

}
