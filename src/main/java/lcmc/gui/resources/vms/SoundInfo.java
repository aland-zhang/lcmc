/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009-2010, LINBIT HA-Solutions GmbH.
 * Copyright (C) 2009-2010, Rasto Levrinc
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
package lcmc.gui.resources.vms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import lcmc.data.Application;
import lcmc.data.Host;
import lcmc.data.StringValue;
import lcmc.data.VMSXML;
import lcmc.data.VMSXML.SoundData;
import lcmc.data.Value;
import lcmc.gui.Browser;
import lcmc.gui.widget.Widget;
import lcmc.utilities.MyButton;
import lcmc.utilities.Tools;
import org.w3c.dom.Node;

/**
 * This class holds info about virtual sound device.
 */
final class SoundInfo extends HardwareInfo {
    /** Parameters. */
    private static final String[] PARAMETERS = {SoundData.MODEL};

    /** Field type. */
    private static final Map<String, Widget.Type> FIELD_TYPES =
                                       new HashMap<String, Widget.Type>();
    /** Short name. */
    private static final Map<String, String> SHORTNAME_MAP =
                                                 new HashMap<String, String>();

    /** Whether the parameter is required. */
    private static final Collection<String> IS_REQUIRED =
        new HashSet<String>(Arrays.asList(new String[]{SoundData.MODEL}));

    /** Possible values. */
    private static final Map<String, Value[]> POSSIBLE_VALUES =
                                               new HashMap<String, Value[]>();
    static {
        FIELD_TYPES.put(SoundData.MODEL, Widget.Type.RADIOGROUP);
        SHORTNAME_MAP.put(SoundData.MODEL, "Model");
    }
    static {
        POSSIBLE_VALUES.put(SoundData.MODEL,
                            new Value[]{new StringValue("ac97"),
                                        new StringValue("es1370"),
                                        new StringValue("pcspk"),
                                        new StringValue("sb16")});
    }

    /** Returns "add new" button. */
    static MyButton getNewBtn(final DomainInfo vdi) {
        final MyButton newBtn = new MyButton("Add Sound Device");
        newBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        vdi.addSoundsPanel();
                    }
                });
                t.start();
            }
        });
        return newBtn;
    }
    /** Table panel. */
    private JComponent tablePanel = null;
    /** Creates the SoundInfo object. */
    SoundInfo(final String name, final Browser browser,
                 final DomainInfo vmsVirtualDomainInfo) {
        super(name, browser, vmsVirtualDomainInfo);
    }

    /** Adds disk table with only this disk to the main panel. */
    @Override
    protected void addHardwareTable(final JPanel mainPanel) {
        tablePanel = getTablePanel("Sound Devices",
                                   DomainInfo.SOUND_TABLE,
                                   getNewBtn(getVMSVirtualDomainInfo()));
        if (getResource().isNew()) {
            Tools.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tablePanel.setVisible(false);
                }
            });
        }
        mainPanel.add(tablePanel);
    }

    /** Returns long description of the specified parameter. */
    @Override
    protected String getParamLongDesc(final String param) {
        return getParamShortDesc(param);
    }

    /** Returns short description of the specified parameter. */
    @Override
    protected String getParamShortDesc(final String param) {
        final String name = SHORTNAME_MAP.get(param);
        if (name == null) {
            return param;
        }
        return name;
    }

    /** Returns preferred value for specified parameter. */
    @Override
    protected Value getParamPreferred(final String param) {
        return null;
    }

    /** Returns default value for specified parameter. */
    @Override
    protected Value getParamDefault(final String param) {
        return null;
    }

    /** Returns parameters. */
    @Override
    public String[] getParametersFromXML() {
        return PARAMETERS.clone();
    }

    /** Returns possible choices for drop down lists. */
    @Override
    protected Value[] getParamPossibleChoices(final String param) {
        return POSSIBLE_VALUES.get(param);
    }

    /** Returns section to which the specified parameter belongs. */
    @Override
    protected String getSection(final String param) {
        return "Sound Device Options";
    }

    /** Returns true if the specified parameter is required. */
    @Override
    protected boolean isRequired(final String param) {
        return IS_REQUIRED.contains(param);
    }

    /** Returns true if the specified parameter is integer. */
    @Override
    protected boolean isInteger(final String param) {
        return false;
    }

    /** Returns true if the specified parameter is label. */
    @Override
    protected boolean isLabel(final String param) {
        return false;
    }

    /** Returns true if the specified parameter is of time type. */
    @Override
    protected boolean isTimeType(final String param) {
        return false;
    }

    /** Returns whether parameter is checkbox. */
    @Override
    protected boolean isCheckBox(final String param) {
        return false;
    }

    /** Returns the type of the parameter. */
    @Override
    protected String getParamType(final String param) {
        return "undef"; // TODO:
    }

    /** Returns the regexp of the parameter. */
    @Override
    protected String getParamRegexp(final String param) {
        return null;
    }

    /** Returns type of the field. */
    @Override
    protected Widget.Type getFieldType(final String param) {
        return FIELD_TYPES.get(param);
    }

    /** Applies the changes. */
    @Override
    void apply(final Application.RunMode runMode) {
        if (Application.isTest(runMode)) {
            return;
        }
        Tools.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                getApplyButton().setEnabled(false);
                getRevertButton().setEnabled(false);
                getInfoPanel();
            }
        });
        waitForInfoPanel();
        final Map<String, String> parameters =
                                    getHWParameters(getResource().isNew());
        for (final Host h : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
            final VMSXML vmsxml = getBrowser().getVMSXML(h);
            if (vmsxml != null) {
                final Value model = getParamSaved(SoundData.MODEL);
                final String modelS;
                if (model == null) {
                    modelS = null;
                } else {
                    modelS = model.getValueForConfig();
                }
                parameters.put(SoundData.SAVED_MODEL, modelS);
                final String domainName =
                                getVMSVirtualDomainInfo().getDomainName();
                final Node domainNode = vmsxml.getDomainNode(domainName);
                modifyXML(vmsxml, domainNode, domainName, parameters);
                final String virshOptions =
                                   getVMSVirtualDomainInfo().getVirshOptions();
                vmsxml.saveAndDefine(domainNode, domainName, virshOptions);
            }
        }
        getResource().setNew(false);
        getBrowser().reload(getNode(), false);
        getBrowser().periodicalVMSUpdate(
                                getVMSVirtualDomainInfo().getDefinedOnHosts());
        Tools.invokeLater(new Runnable() {
            @Override
            public void run() {
                tablePanel.setVisible(true);
            }
        });
        final String[] params = getParametersFromXML();
        if (Application.isLive(runMode)) {
            storeComboBoxValues(params);
        }
        checkResourceFields(null, params);
    }

    /** Returns device parameters. */
    @Override
    protected Map<String, String> getHWParameters(final boolean allParams) {
        final Map<String, String> params = super.getHWParameters(allParams);
        setName(getParamSavedForConfig(SoundData.MODEL));
        return params;
    }

    /** Returns data for the table. */
    @Override
    protected Object[][] getTableData(final String tableName) {
        if (DomainInfo.HEADER_TABLE.equals(tableName)) {
            return getVMSVirtualDomainInfo().getMainTableData();
        } else if (DomainInfo.SOUND_TABLE.equals(tableName)) {
            if (getResource().isNew()) {
                return new Object[][]{};
            }
            return new Object[][]{getVMSVirtualDomainInfo().getSoundDataRow(
                                getName(),
                                null,
                                getVMSVirtualDomainInfo().getSounds(),
                                true)};
        }
        return new Object[][]{};
    }

    /** Returns whether this parameter is advanced. */
    @Override
    protected boolean isAdvanced(final String param) {
        return false;
    }

    /** Whether the parameter should be enabled. */
    @Override
    protected String isEnabled(final String param) {
        return null;
    }

    /** Whether the parameter should be enabled. */
    @Override
    protected boolean isEnabledOnlyInAdvancedMode(final String param) {
         return false;
    }

    /** Returns access type of this parameter. */
    @Override
    protected Application.AccessType getAccessType(final String param) {
        return Application.AccessType.ADMIN;
    }

    /** Returns true if the value of the parameter is ok. */
    @Override
    protected boolean checkParam(final String param, final Value newValue) {
        return !isRequired(param)
               || (newValue != null && !newValue.isNothingSelected());
    }

    /** Updates parameters. */
    @Override
    void updateParameters() {
        final Map<String, SoundData> sounds =
                              getVMSVirtualDomainInfo().getSounds();
        if (sounds != null) {
            final SoundData soundData = sounds.get(getName());
            if (soundData != null) {
                for (final String param : getParametersFromXML()) {
                    final Value oldValue = getParamSaved(param);
                    Value value = getParamSaved(param);
                    final Widget wi = getWidget(param, null);
                    for (final Host h
                            : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
                        final VMSXML vmsxml = getBrowser().getVMSXML(h);
                        if (vmsxml != null) {
                            final Value savedValue = soundData.getValue(param);
                            if (savedValue != null) {
                                value = savedValue;
                            }
                        }
                    }
                    if (!Tools.areEqual(value, oldValue)) {
                        getResource().setValue(param, value);
                        if (wi != null) {
                            /* only if it is not changed by user. */
                            wi.setValue(value);
                        }
                    }
                }
            }
        }
        updateTable(DomainInfo.HEADER_TABLE);
        updateTable(DomainInfo.SOUND_TABLE);
        checkResourceFields(null, getParametersFromXML());
    }

    /** Returns string representation. */
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder(30);
        final Value model = getParamSaved(SoundData.MODEL);
        if (model == null || model.isNothingSelected()) {
            s.append("new sound device...");
        } else {
            s.append(model);
        }
        return s.toString();
    }

    /** Removes this sound device without confirmation dialog. */
    @Override
    protected void removeMyselfNoConfirm(final Application.RunMode runMode) {
        if (Application.isTest(runMode)) {
            return;
        }
        final String virshOptions = getVMSVirtualDomainInfo().getVirshOptions();
        for (final Host h : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
            final VMSXML vmsxml = getBrowser().getVMSXML(h);
            if (vmsxml != null) {
                final Map<String, String> parameters =
                                                new HashMap<String, String>();
                parameters.put(SoundData.SAVED_MODEL,
                               getParamSaved(SoundData.MODEL).getValueForConfig());
                vmsxml.removeSoundXML(getVMSVirtualDomainInfo().getDomainName(),
                                      parameters,
                                      virshOptions);
            }
        }
        getBrowser().periodicalVMSUpdate(
                                getVMSVirtualDomainInfo().getDefinedOnHosts());
        removeNode();
    }

    /**
     * Returns whether this item is removeable (null), or string why it isn't.
     */
    @Override
    protected String isRemoveable() {
        return null;
    }

    /** Modify device xml. */
    @Override
    protected void modifyXML(final VMSXML vmsxml, final Node node, final String domainName, final Map<String, String> params) {
        if (vmsxml != null) {
            vmsxml.modifySoundXML(node, domainName, params);
        }
    }
}