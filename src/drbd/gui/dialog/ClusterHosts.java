/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009, LINBIT HA-Solutions GmbH.
 *
 * DRBD Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * DRBD Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with drbd; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */


package drbd.gui.dialog;

import drbd.data.Host;
import drbd.data.Hosts;
import drbd.data.Cluster;
import drbd.utilities.Tools;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Scrollable;

import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.LayoutManager;

/**
 * An implementation of a dialog where user can choose which hosts belong to
 * the cluster.
 *
 * @author Rasto Levrinc
 * @version $Id$
 *
 */
public class ClusterHosts extends DialogCluster {
    /** Serial Version UID. */
    private static final long serialVersionUID = 1L;
    /** Map from checkboxes to the host, which they choose. */
    private final Map<JCheckBox, Host> checkBoxToHost =
                                    new LinkedHashMap<JCheckBox, Host>();
    /** Host checked icon. */
    private static final ImageIcon HOST_CHECKED_ICON = Tools.createImageIcon(
                Tools.getDefault("Dialog.ClusterHosts.HostCheckedIcon"));
    /** Host not checked icon. */
    private static final ImageIcon HOST_UNCHECKED_ICON = Tools.createImageIcon(
                Tools.getDefault("Dialog.ClusterHosts.HostUncheckedIcon"));

    /**
     * Prepares a new <code>ClusterHosts</code> object.
     */
    public ClusterHosts(final WizardDialog previousDialog,
                        final Cluster cluster) {
        super(previousDialog, cluster);
    }

    /**
     * It is executed after the dialog is applied.
     */
    protected final void finishDialog() {
        getCluster().clearHosts();
        for (final JCheckBox button : checkBoxToHost.keySet()) {
            if (button.isSelected()) {
                final Host host = checkBoxToHost.get(button);
                host.setCluster(getCluster());
                getCluster().addHost(host);
            }
        }
        Tools.getGUIData().refreshClustersPanel();
        checkBoxToHost.clear();
    }

    /**
     * Returns the next dialog.
     */
    public final WizardDialog nextDialog() {
        boolean allConnected = true;
        for (final Host host : getCluster().getHosts()) {
            if (!host.isConnected()) {
                allConnected = false;
            }
        }
        if (allConnected) {
            return new ClusterCommStack(this, getCluster());
        } else {
            return new ClusterConnect(this, getCluster());
        }
    }

    /**
     * Checks whether at least two hosts are selected for the cluster.
     */
    protected final void checkCheckBoxes() {
        Tools.getConfigData().getHosts().removeHostsFromCluster(getCluster());
        int selected = 0;
        for (final JCheckBox button : checkBoxToHost.keySet()) {
            if (button.isSelected()) {
                selected++;
            }
        }
        boolean enable = true;
        final List<String> ips = new ArrayList<String>();
        if (selected < 2) {
            enable = false;
        } else {
            /* check if some of the hosts are the same. It will not work all
             * the time if hops are used. */
            for (final JCheckBox button : checkBoxToHost.keySet()) {
                if (button.isSelected() && button.isEnabled()) {
                    final Host host = checkBoxToHost.get(button);
                    final String ip = host.getHostname();
                    if (ips.contains(ip)) {
                        enable = false;
                        break;
                    }
                    ips.add(ip);
                }
            }
        }
        final boolean enableButton = enable;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buttonClass(nextButton()).setEnabled(enableButton);
            }
        });
        if (!Tools.getConfigData().getAutoClusters().isEmpty()) {
            Tools.sleep(1000);
            pressNextButton();
        }
    }

    /**
     * Returns the title of the dialog.
     */
    protected final String getClusterDialogTitle() {
        return Tools.getString("Dialog.ClusterHosts.Title");
    }

    /**
     * Returns the description of the dialog.
     */
    protected final String getDescription() {
        return Tools.getString("Dialog.ClusterHosts.Description");
    }

    /**
     * Inits the dialog.
     */
    protected final void initDialog() {
        super.initDialog();
        enableComponentsLater(new JComponent[]{buttonClass(nextButton())});
        enableComponents();

        final Thread thread = new Thread(
            new Runnable() {
                public void run() {
                    checkCheckBoxes();
                }
            });
        thread.start();
    }

    /**
     * Returns the panel with hosts that can be selected.
     */
    protected final JComponent getInputPane() {
        /* Hosts */
        final ScrollableFlowPanel p1 =
            new ScrollableFlowPanel(new FlowLayout(FlowLayout.LEADING, 1, 1));
        final Hosts hosts = Tools.getConfigData().getHosts();

        final ItemListener chListener = new ItemListener() {
                public void itemStateChanged(final ItemEvent e) {
                    checkCheckBoxes();
                }
            };
        Host lastHost1 = null;
        Host lastHost2 = null;
        if (getCluster().getHosts().size() == 0) {
            /* mark last two available hosts */
            for (final Host host : hosts.getHostsArray()) {
                if (!getCluster().getHosts().contains(host)
                    && !host.isInCluster()) {
                    if (lastHost2 != null
                        && lastHost2.getIp() != null
                        && lastHost2.getIp().equals(host.getIp())) {
                        lastHost2 = host;
                    } else {
                        lastHost1 = lastHost2;
                        lastHost2 = host;
                    }
                }
            }
        }
        for (final Host host : hosts.getHostsArray()) {
            final JCheckBox button = new JCheckBox(host.getName(),
                                                   HOST_UNCHECKED_ICON);
            button.setSelectedIcon(HOST_CHECKED_ICON);
            if (getCluster().getBrowser() != null
                && getCluster() == host.getCluster()) {
                /* once we have browser the cluster members cannot be removed.
                 * TODO: make it possible
                 */
                button.setEnabled(false);
            } else if (host.isInCluster(getCluster())) {
                button.setEnabled(false);
            }
            checkBoxToHost.put(button, host);
            if (getCluster().getHosts().contains(host)) {
                button.setSelected(true);
            } else {
                if (host == lastHost1 || host == lastHost2) {
                    button.setSelected(true);
                } else {
                    button.setSelected(false);
                }
            }
            button.addItemListener(chListener);
            p1.add(button);
        }
        p1.setBackground(Color.WHITE);
        return new JScrollPane(p1,
                               JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                               JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    /**
     * Workaround so that flow layout scrolls right.
     */
    private class ScrollableFlowPanel extends JPanel
                                             implements Scrollable {
        private static final long serialVersionUID = 1L;
        public ScrollableFlowPanel(final LayoutManager layout) {
            super(layout);
        }

        public void setBounds(final int x,
                              final int y,
                              final int width,
                              final int height) {
            super.setBounds(x, y, getParent().getWidth(), height);
        }

        public Dimension getPreferredSize() {
            return new Dimension(getWidth(), getPreferredHeight());
        }

        public Dimension getPreferredScrollableViewportSize() {
            return super.getPreferredSize();
        }

        public int getScrollableUnitIncrement(final Rectangle visibleRect,
                                              final int orientation,
                                              final int direction) {
            final int hundredth = (orientation ==  SwingConstants.VERTICAL
                    ? getParent().getHeight() : getParent().getWidth()) / 100;
            return (hundredth == 0 ? 1 : hundredth);
        }

        public int getScrollableBlockIncrement(final Rectangle visibleRect,
                                               final int orientation,
                                               final int direction) {
            return orientation == SwingConstants.VERTICAL
                            ? getParent().getHeight() : getParent().getWidth();
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        private int getPreferredHeight() {
            int rv = 0;
            final int count = getComponentCount();
            for (int k = 0; k < count; k++) {
                final Component comp = getComponent(k);
                final Rectangle r = comp.getBounds();
                final int height = r.y + r.height;
                if (height > rv) {
                    rv = height;
                }
            }
            rv += ((FlowLayout) getLayout()).getVgap();
            return rv;
        }
    }
}
