//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// $Revision: 28182 $
//
//-======================================================================


package org.tango.settingsmanager.client.config;

import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.settingsmanager.client.gui_utils.IconUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class AttributeTree extends JTree implements TangoConst {

    private DefaultTreeModel treeModel;
    private AttributeTreePopupMenu popupMenu = new AttributeTreePopupMenu(this);
    private JList<String> attributeJList;
    private boolean attributeListChanged = false;

    private static ImageIcon tangoIcon;
    private static ImageIcon serverIcon;
    private static ImageIcon deviceIcon;
    private static ImageIcon attributeIcon;
    private static ImageIcon classIcon;

    private static final int INSTANCE = 3;
    private static final int CLASS = 4;
    private static final int DEVICE = 5;
    private static final int ATTRIB = 6;

    private static final int DOMAIN = 3;
    private static final int FAMILY = 4;
    private static final int MEMBER = 5;
    //===============================================================
    //===============================================================
    public AttributeTree(JList<String> attributeJList) throws DevFailed {
        super();
        this.attributeJList = attributeJList;
        loadIcons();

        //	Get TANGO HOST as title
        String tango_host = ApiUtil.get_db_obj().get_tango_host();
        initComponent(tango_host);
    }
    //===============================================================
    boolean isAttributeListChanged() {
        return attributeListChanged;
    }
    //===============================================================
    void setAttributeListChanged(boolean attributeListChanged) {
        this.attributeListChanged = attributeListChanged;
    }
    //===============================================================
    private void loadIcons() throws DevFailed {
        if (deviceIcon==null) {
            tangoIcon = IconUtils.getInstance().getIcon("TangoLogo.gif", 0.15);
            serverIcon = IconUtils.getInstance().getIcon("server.gif");
            deviceIcon = IconUtils.getInstance().getIcon("device.gif");
            attributeIcon = IconUtils.getInstance().getIcon("leaf.gif");
            classIcon = IconUtils.getInstance().getIcon("class.gif");
        }
    }
    //===============================================================
    //===============================================================
    private void initComponent(String title) throws DevFailed {

        //  Create the nodes.
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(title);

        createServerNodes(rootNode);
        createDomainNodes(rootNode);
        createAliasNodes(rootNode);

        //	Create the tree that allows one selection at a time.
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        //	Create Tree and Tree model
        treeModel = new DefaultTreeModel(rootNode);
        setModel(treeModel);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);

        // Set the icon for leaf nodes.
        setCellRenderer(new TangoRenderer());

        //	Listen for collapse tree
        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
                //collapsedPerformed(e);
            }

            public void treeExpanded(TreeExpansionEvent e) {
                expandedPerformed(e);
            }
        });
        //	Add Action listener
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);
            }
        });
    }

    //===============================================================
    //===============================================================
    private boolean createChildNodes(DefaultMutableTreeNode node, String[] str) {
        boolean create = false;
        if (node.getChildCount() != str.length)
            create = true;
        else
            for (int i = 0; i < str.length; i++)
                if (!node.getChildAt(i).toString().equals(str[i]))
                    create = true;
        return create;
    }

    //===============================================================
    //===============================================================
    private void createAliasNodes(DefaultMutableTreeNode root) throws DevFailed {
        //	Create a node for Device
        DefaultMutableTreeNode c_node = new DefaultMutableTreeNode("Aliases");
        root.add(c_node);

        DefaultMutableTreeNode al_node;

        Database db = ApiUtil.get_db_obj();
        String[] aliases = db.get_device_alias_list("*");
        for (String alias : aliases) {
            //	Create a node for domain
            try {
                String deviceName = ApiUtil.get_db_obj().get_device_from_alias(alias);
                al_node = new DefaultMutableTreeNode(new BrowserDevice(deviceName, alias));
                al_node.add(new DefaultMutableTreeNode("dummy"));
                c_node.add(al_node);
            } catch (DevFailed e) {/** Do Nothing */}
        }
    }

    //===============================================================
    //===============================================================
    private void createServerNodes(DefaultMutableTreeNode root) throws DevFailed {
        //	Create a node for Device
        DefaultMutableTreeNode serversNode = new DefaultMutableTreeNode("Servers");
        root.add(serversNode);

        Database db = ApiUtil.get_db_obj();
        String[] servers = db.get_server_name_list();
        for (String server : servers) {
            //	Create a node each exe file
            DefaultMutableTreeNode exeNode = new DefaultMutableTreeNode(server);
            exeNode.add(new DefaultMutableTreeNode("dummy"));
            serversNode.add(exeNode);
        }
    }

    //===============================================================
    //===============================================================
    private void createInstanceNodes(DefaultMutableTreeNode node) {
        try {
            String exeFile = (String) node.getUserObject();
            String[] instances =
                    ApiUtil.get_db_obj().get_instance_name_list(exeFile);

            //  Check if something has changed.
            if (!createChildNodes(node, instances))
                return;

            for (int i = 0; i < instances.length; i++) {
                //	Create a node each instance
                DefaultMutableTreeNode instanceNode = new DefaultMutableTreeNode(new BrowserServer(exeFile, instances[i]));
                instanceNode.add(new DefaultMutableTreeNode("Dummy"));
                treeModel.insertNodeInto(instanceNode, node, i);
            }
            removePreviousNodes(node, instances.length);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }

    //===============================================================
    //===============================================================
    private void createClassNodes(DefaultMutableTreeNode node) {
        try {
            BrowserServer server = (BrowserServer) node.getUserObject();
            String[] classes = ApiUtil.get_db_obj().get_server_class_list(server.name);

            //  Check if something has changed.
            if (!createChildNodes(node, classes))
                return;

            for (int i = 0; i < classes.length; i++) {
                //	Create a node for each class
                DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(classes[i]);
                classNode.add(new DefaultMutableTreeNode("Dummy"));
                treeModel.insertNodeInto(classNode, node, i);
            }
            removePreviousNodes(node, classes.length);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }

    //===============================================================
    //===============================================================
    private void createDeviceNodesFromServer(DefaultMutableTreeNode node) {
        try {
            DefaultMutableTreeNode classNode = (DefaultMutableTreeNode) node.getParent();

            BrowserServer server = (BrowserServer) classNode.getUserObject();
            String serverName = server.name;
            String className = (String) node.getUserObject();
            String[] devices =
                    ApiUtil.get_db_obj().get_device_name(serverName, className);

            //  Check if something has changed.
            if (!createChildNodes(node, devices))
                return;

            for (int i=0 ; i<devices.length ; i++) {
                //	Create a node for device
                DefaultMutableTreeNode deviceNode =
                        new DefaultMutableTreeNode(new BrowserServDevice(devices[i]));
                deviceNode.add(new DefaultMutableTreeNode("dummy"));
                treeModel.insertNodeInto(deviceNode, node, i);
            }
            removePreviousNodes(node, devices.length);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
    //===============================================================
    //===============================================================


    //===============================================================
    //===============================================================
    private void createDomainNodes(DefaultMutableTreeNode root) throws DevFailed {
        //	Create a node for Device
        DefaultMutableTreeNode devicesNode = new DefaultMutableTreeNode("Devices");
        root.add(devicesNode);

        Database db = ApiUtil.get_db_obj();
        String[] domains = db.get_device_domain("*");
        for (String domain : domains) {
            //	Create a node for domain
            DefaultMutableTreeNode domainNode = new DefaultMutableTreeNode(domain);
            domainNode.add(new DefaultMutableTreeNode("dummy"));
            devicesNode.add(domainNode);
        }
    }
    //===============================================================
    //===============================================================
    private void createFamilyNodes(DefaultMutableTreeNode domainNode) {
        try {
            String domain = (String) domainNode.getUserObject();
            String[] families =
                    ApiUtil.get_db_obj().get_device_family(domain + "/*");

            //  Check if something has changed.
            if (!createChildNodes(domainNode, families))
                return;

            for (int f = 0; f < families.length; f++) {
                //	Create a node for family
                DefaultMutableTreeNode familyNode = new DefaultMutableTreeNode(families[f]);
                familyNode.add(new DefaultMutableTreeNode("Dummy"));
                treeModel.insertNodeInto(familyNode, domainNode, f);
            }
            removePreviousNodes(domainNode, families.length);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
    //===============================================================
    //===============================================================
    private void createMemberNodes(String tangoPath, DefaultMutableTreeNode familyNode) {
        try {
            tangoPath += (String) familyNode.getUserObject();
            String[] members =
                    ApiUtil.get_db_obj().get_device_member(tangoPath + "/*");

            //  Check if something has changed.
            if (!createChildNodes(familyNode, members))
                return;

            for (int m = 0; m < members.length; m++) {
                //	Create a node for member
                DefaultMutableTreeNode memberNode =
                        new DefaultMutableTreeNode(new BrowserDevice(tangoPath + "/" + members[m]));
                memberNode.add(new DefaultMutableTreeNode("Dummy"));
                treeModel.insertNodeInto(memberNode, familyNode, m);
            }
            removePreviousNodes(familyNode, members.length);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }

    //===============================================================
    //===============================================================
    private void createAttributeNodes(DefaultMutableTreeNode deviceNode) {
        try {
            BrowserDevice dev = (BrowserDevice) deviceNode.getUserObject();
            AttributeInfoEx[] info = dev.get_attribute_info_ex();
            int i=0;
            for (AttributeInfoEx attConfig : info) {
                if (isManagedBySettingsManager(attConfig)) {
                    //	Create a node for attribute
                    BrowserAttribute attr = new BrowserAttribute(attConfig.name, dev);
                    DefaultMutableTreeNode attributeNode = new DefaultMutableTreeNode(attr);
                    treeModel.insertNodeInto(attributeNode, deviceNode, i++);
                }
            }
            removePreviousNodes(deviceNode, i);
        } catch (DevFailed e) {
            removePreviousNodes(deviceNode, 0);
            ErrorPane.showErrorMessage(this, null, e);        }
    }

    //===============================================================
    //===============================================================
    private boolean isManagedBySettingsManager(AttributeInfoEx attConfig) {
        return attConfig!=null &&
                (attConfig.writable==AttrWriteType.WRITE || attConfig.writable==AttrWriteType.READ_WRITE);
    }
    //===============================================================
    //===============================================================
    private void removePreviousNodes(DefaultMutableTreeNode node, int offset) {
        while (node.getChildCount() > offset) {
            DefaultMutableTreeNode leaf =
                    (DefaultMutableTreeNode) node.getChildAt(offset);
            treeModel.removeNodeFromParent(leaf);
        }
    }

    //===============================================================
    //===============================================================
    private String tangoPath(TreePath path, int nb) {
        String p = "";
        for (int i = DOMAIN; i < DOMAIN + nb; i++) {
            p += path.getPathComponent(i - 1).toString();
            p += "/";
        }
        return p;
    }

    //===============================================================
    //===============================================================
    public void expandedPerformed(TreeExpansionEvent evt) {
        //	Get path
        TreePath tp = evt.getPath();
        Object[] path = tp.getPath();
        if (path.length < 2)
            return;
        //	Get concerned node
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) tp.getPathComponent(path.length - 1);

        //	and create new ones
        String tangoPath;
        if (path[1].toString().equals("Servers")) {
            switch (path.length) {
                case INSTANCE:
                    createInstanceNodes(node);
                    break;
                case CLASS:
                    createClassNodes(node);
                    break;
                case DEVICE:
                    createDeviceNodesFromServer(node);
                    break;
                case ATTRIB:
                    createAttributeNodes(node);
                    break;
            }
        } else if (path[1].toString().equals("Devices")) {
            switch (path.length) {
                case DOMAIN:
                    createFamilyNodes(node);
                    break;
                case FAMILY:
                    tangoPath = tangoPath(tp, 1);
                    createMemberNodes(tangoPath, node);
                    break;
                case MEMBER:
                    createAttributeNodes(node);
                    break;
            }
        } else if (path[1].toString().equals("Aliases")) {
            if (path.length == 3) {
                createAttributeNodes(node);
            }
        }
    }

    //======================================================
    /**
     * Manage event on clicked mouse on JTree object.
     */
    //======================================================
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        //	Check if click is on a node
        if (getRowForLocation(evt.getX(), evt.getY()) < 1)
            return;

        //	Set selection at mouse position
        TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath == null) return;

        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount() - 1);
        Object object = node.getUserObject();
        int mask = evt.getModifiers();

        //  Check button clicked
        if (evt.getClickCount() == 2 && node.isLeaf()) {
            //	if do not have attributes, retry to read attributes
            if (object instanceof BrowserDevice) {
                createAttributeNodes(node);
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode child =
                            (DefaultMutableTreeNode) node.getChildAt(i);
                    expandPath(new TreePath(child.getPath()));
                }
            }
        } else if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
            //	Check if selection is an attribute
            if (object instanceof BrowserAttribute) {
                //	Check if selection is an attribute
                popupMenu.showMenu(evt);
            }
        }
    }
    //===============================================================
    //===============================================================
    public List<String> getSelectedAttributes() {
        List<String>   attributes = new ArrayList<>();
        TreePath[]  paths = getSelectionPaths();
        if (paths!=null) {
            for (TreePath path : paths) {
                //  Check if attribute
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof BrowserAttribute) {
                    BrowserAttribute   attribute = (BrowserAttribute) node.getUserObject();
                    attributes.add(attribute.fullName);
                }
            }
        }
        return attributes;
    }
    //======================================================
    //======================================================
    private void addSelectedAttributes() {
        //  Get attribute and selection lists
        List<String> attributeList = new ArrayList<>();
        for (int i=0 ; i<attributeJList.getModel().getSize() ; i++)
            attributeList.add(attributeJList.getModel().getElementAt(i));
        List<String> selected = getSelectedAttributes();

        //  Check if selection already in JList
        for (String selection : selected) {
            boolean found = false;
            for (String attribute : attributeList) {
                if (attribute.equalsIgnoreCase(selection))
                    found = true;
            }
            //  If not add it
            if (!found) {
                attributeList.add(selection);
                attributeListChanged = true;
            }
        }
        //  Set to the JList object
        attributeJList.setListData(attributeList.toArray(new String[attributeList.size()]));
    }
    //======================================================
    //======================================================
    @SuppressWarnings("unused")
    private DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) getLastSelectedPathComponent();
    }
    //======================================================
    //======================================================
    @SuppressWarnings("unused")
    private BrowserAttribute getAttribute(Object o) {
        if (o == null)
            return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
        Object obj = node.getUserObject();
        if (obj instanceof BrowserAttribute)
            return (BrowserAttribute) obj;
        else
            return null;
    }
    //======================================================
    //======================================================





    //==========================================================
    //==========================================================
    private class BrowserServer {
        String name;
        String instance;
        BrowserServDevice dev;

        //==========================================================
        BrowserServer(String binfile, String instance) {
            this.name = binfile + "/" + instance;
            this.instance = instance;
            try {
                dev = new BrowserServDevice("dserver/" + name);
            } catch (DevFailed e) { /** Do nothing */}
        }

        //==========================================================
        //==========================================================
        public String toString() {
            return instance;
        }
        //==========================================================
    }

    //==========================================================
    //==========================================================
    private class BrowserDevice extends DeviceProxy {
        String name;
        String member;

        //==========================================================
        BrowserDevice(String name) throws DevFailed {
            super(name);
            this.name = name;
            int idx = name.lastIndexOf('/');
            if (idx < 0)
                this.member = name;
            else
                this.member = name.substring(idx + 1);
        }
        //==========================================================
        BrowserDevice(String name, String aliasname) throws DevFailed {
            super(name);
            this.name = name;
            member = aliasname;
        }
        //==========================================================
        public String toString() {
            return member;
        }
    }

    //==========================================================
    //==========================================================
    private class BrowserServDevice extends BrowserDevice {
        //==========================================================
        BrowserServDevice(String name) throws DevFailed {
            super(name);
        }

        //==========================================================
        public String toString() {
            return name;
        }
    }

    //==========================================================
    //==========================================================
    private class BrowserAttribute {
        BrowserDevice dev;
        String name;
        String fullName;
        //==========================================================
        BrowserAttribute(String name, BrowserDevice dev) {
            this.name = name;
            this.dev = dev;
            this.fullName = dev.name + "/" + name;
        }
        //==========================================================
        public String toString() {
            return name;
        }
    }



    //===============================================================
    /**
     * Renderer Class
     */
    //===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        private Font[] fonts;

        private final int TITLE = 0;
        private final int DEVICE = 1;
        private final int ATTR = 2;
        //===============================================================
        //===============================================================
        public TangoRenderer() {

            fonts = new Font[3];
            fonts[TITLE] = new Font("courrier", Font.BOLD, 18);
            //	width fixed font
            fonts[DEVICE] = new Font("Monospaced", Font.BOLD, 12);
            fonts[ATTR] = new Font("Monospaced", Font.PLAIN, 12);
        }
        //===============================================================
        //===============================================================
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object obj,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, obj, sel,
                    expanded, leaf, row,
                    hasFocus);

            if (row == 0) {
                //	ROOT
                setFont(fonts[TITLE]);
                setIcon(tangoIcon);
            } else {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;

                if (node.getUserObject() instanceof String) {
                    setFont(fonts[TITLE]);
                    if (obj.toString().equals("Servers"))
                        setIcon(serverIcon);
                    else if (obj.toString().equals("Devices"))
                        setIcon(deviceIcon);
                    else if (obj.toString().equals("Aliases"))
                        setIcon(deviceIcon);
                    else {
                        setFont(fonts[DEVICE]);
                        setIcon(classIcon);
                    }
                } else if (node.getUserObject() instanceof BrowserAttribute) {
                    setFont(fonts[ATTR]);
                    setIcon(attributeIcon);
                } else if (node.getUserObject() instanceof BrowserDevice) {
                    setFont(fonts[DEVICE]);
                    setIcon(deviceIcon);
                } else if (node.getUserObject() instanceof BrowserServer) {
                    setFont(fonts[DEVICE]);
                    setIcon(serverIcon);
                }
            }
            return this;
        }
    }//	End of Renderer Class
    //==========================================================
    //==========================================================





    //==========================================================
    //==========================================================
    private static final int ADD_ATTRIBUTES = 0;
    private static final int OFFSET = 2;    //	Label And separator

    private static String[] menuLabels = {
            "Add selected attributes",
    };

    private class AttributeTreePopupMenu extends JPopupMenu {
        private JTree tree;
        private JLabel title;
        //======================================================
        private AttributeTreePopupMenu(JTree tree) {
            this.tree = tree;
            title = new JLabel();
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 16));
            add(title);
            add(new JPopupMenu.Separator());

            for (String menuLabel : menuLabels) {
                if (menuLabel==null)
                    add(new Separator());
                else {
                    JMenuItem btn = new JMenuItem(menuLabel);
                    btn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            hostActionPerformed(evt);
                        }
                    });
                    add(btn);
                }
            }
        }
        //======================================================
        private void showMenu(MouseEvent evt) {
            // Check if attributes are selected
            List<String> selection = getSelectedAttributes();
            getComponent(OFFSET + ADD_ATTRIBUTES).setEnabled(!selection.isEmpty());

            show(tree, evt.getX(), evt.getY());
        }
        //======================================================
        private void hostActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int itemIndex = 0;
            for (int i = 0 ; i<menuLabels.length ; i++)
                if (getComponent(OFFSET + i)==obj)
                    itemIndex = i;

            switch (itemIndex) {
                case ADD_ATTRIBUTES:
                    addSelectedAttributes();
            }
        }
        //======================================================
    }
    //==========================================================
    //==========================================================
}
