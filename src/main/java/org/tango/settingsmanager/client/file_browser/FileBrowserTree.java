//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:	java source code for display JTree
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015
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
// $Revision: 1.2 $
//
// $Log:  $
//
//-======================================================================

package org.tango.settingsmanager.client.file_browser;


import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.settingsmanager.client.gui_utils.IconUtils;
import org.tango.settingsmanager.commons.ICommons;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class FileBrowserTree extends JTree implements TangoConst {
    private static ImageIcon directoryIcon;
    private static ImageIcon fileIcon;

    private Component parent;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;
    //private FileBrowserTreePopupMenu menu = new FileBrowserTreePopupMenu(this);
    private DeviceProxy managerProxy;
    private String rootPath;
    private String relativePath;

    private GenerateFilePopupMenu popupMenu;    //  used to create dir when generate
    //===============================================================
    //===============================================================
    public FileBrowserTree(Component parent, DeviceProxy managerProxy) throws DevFailed {
        super();
        this.parent = parent;
        this.managerProxy = managerProxy;
        this.relativePath = "";

        //  Get the full root relativePath
        DeviceAttribute attribute = managerProxy.read_attribute("SettingsPath");
        this.rootPath = attribute.extractString();
        //  Load icons
        directoryIcon = IconUtils.getInstance().getIcon("directory.gif");
        fileIcon = IconUtils.getInstance().getIcon("file.gif");
        //  Build the tree
        buildTree(relativePath);
        setSelectionPath(null);
        popupMenu = new GenerateFilePopupMenu(this);

    }
    //===============================================================
    //===============================================================
    private void buildTree(String path) throws DevFailed {
        //  Create the nodes.
        root = new DefaultMutableTreeNode(rootPath);
        createDirectoryNodes(path, root);

        //	Create the tree that allows one selection at a time.
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //	Create Tree and Tree model
        treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);

        //  Set the icon for leaf nodes.
        TangoRenderer renderer = new TangoRenderer();
        setCellRenderer(renderer);

        //	Listen for collapse tree
        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) { }
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
    //======================================================
    /**
     * Manage event on clicked mouse on JTree object.
     */
    //======================================================
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        //	Set selection at mouse position
        TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath==null)
            return;

        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount() - 1);
        Object userObject = node.getUserObject();
        int mask = evt.getModifiers();

        //  Check button clicked
        if ((mask & MouseEvent.BUTTON1_MASK)!=0) {
            if (userObject instanceof BrowsedFile) {
                if (evt.getClickCount()==2) {
                    if (parent instanceof FileBrowserDialog)
                        ((FileBrowserDialog)parent).manageSelection();
                    else
                        System.err.println("Parent unknown class !");
                }
            }
        }
        else if ((mask & MouseEvent.BUTTON3_MASK)!=0) {
            if (node==root)
                popupMenu.showMenu(evt, root.toString());
            if (userObject instanceof BrowsedDirectory)
                popupMenu.showMenu(evt, ((BrowsedDirectory)userObject).path);
        }
    }
    //===============================================================
    //===============================================================
    public void expandedPerformed(TreeExpansionEvent evt) {
        try {
            //	Get path
            TreePath treePath = evt.getPath();
            Object[] pathComponents = treePath.getPath();

            //	Get specified node
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) treePath.getPathComponent(pathComponents.length - 1);
            if (node!=root) {
                //  Build String path
                String path = "";
                for (Object obj : pathComponents) {
                    if (obj==root)
                        path += relativePath + "/";
                    else
                        path += obj.toString() + "/";
                }
                //  Create the sub directory nodes
                createDirectoryNodes(path, node);
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
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
    public void setSelectedFile(String fileName) throws DevFailed {
        if (fileName!=null) {
            //  Get path components
            StringTokenizer stk = new StringTokenizer(fileName, "/");
            List<String> items = new ArrayList<>();
            while (stk.hasMoreTokens())
                items.add(stk.nextToken());

            //	Set selection for specified path
            List<DefaultMutableTreeNode> nodeList = new ArrayList<>();
            nodeList.add(root);
            DefaultMutableTreeNode node = root;
            for (String item : items) {
                boolean found = false;
                DefaultMutableTreeNode childNode = null;
                for (int n = 0; !found && n < node.getChildCount(); n++) {
                    childNode = (DefaultMutableTreeNode) node.getChildAt(n);
                    if (childNode.toString().equalsIgnoreCase(item)) {
                        //  If directory -> expand to create tree under
                        if (!childNode.isLeaf()) {
                            expandNode(childNode);
                        }
                        nodeList.add(childNode);
                        found = true;
                    }
                }
                if (found)
                    node = childNode;
                else
                    break; //   not found --> stop search
            }

            DefaultMutableTreeNode[] nodes = nodeList.toArray(new DefaultMutableTreeNode[nodeList.size()]);
            TreePath selectedPath = new TreePath(nodes);
            setSelectionPath(selectedPath);

            setSelectedFileInfo(fileName);
        }
        else
            setSelectedFileInfo("");
    }
    //===============================================================
    //===============================================================
    private void setSelectedFileInfo(String fileName) {
        if (parent instanceof FileBrowserDialog)
            ((FileBrowserDialog)parent).setSelectedFileInfo(fileName);
    }
    //===============================================================
    //===============================================================
    private void expandNode(DefaultMutableTreeNode node) {
        if (node.getChildCount()>0) {
            node = (DefaultMutableTreeNode) node.getChildAt(0);
            List<DefaultMutableTreeNode> nodes = new ArrayList<>();
            nodes.add(node);
            while (node!=root) {
                node = (DefaultMutableTreeNode) node.getParent();
                nodes.add(0, node);
            }
            TreeNode[] treeNodes = new DefaultMutableTreeNode[nodes.size()];
            for (int i = 0 ; i<nodes.size() ; i++)
                treeNodes[i] = nodes.get(i);
            TreePath treePath = new TreePath(treeNodes);
            setSelectionPath(treePath);
            scrollPathToVisible(treePath);
        }
    }
    //===============================================================
    //===============================================================
    public void collapseNodes() {
        for (int i=1 ; i <= root.getChildCount() ; i++)
            collapseRow(i);

    }
    //===============================================================
    //===============================================================


    //===============================================================
    //===============================================================
    private void createDirectoryNodes(String path, DefaultMutableTreeNode node) throws DevFailed {
        //  Get directory contents
        DirectoryContent directoryContent = new DirectoryContent(path);
        //  Build directory nodes
        int i = 0;
        for (BrowsedDirectory directory : directoryContent.getDirectoryList()) {
            DefaultMutableTreeNode directoryNode =
                    new DefaultMutableTreeNode(directory);
            if (treeModel==null) {
                node.add(directoryNode);
                directoryNode.add(new DefaultMutableTreeNode(""));
            }
            else {
                treeModel.insertNodeInto(directoryNode, node, i++);
                treeModel.insertNodeInto(new DefaultMutableTreeNode(""), directoryNode, 0);
            }
        }
        //  build file nodes
        for (BrowsedFile file : directoryContent.getFileList()) {
            if (treeModel==null) {
                node.add(new DefaultMutableTreeNode(file));
            }
            else {
                treeModel.insertNodeInto(new DefaultMutableTreeNode(file), node, i++);
            }
        }
        //  remove previous nodes if any
        if (treeModel!=null)
            removePreviousNodes(node, i);
    }

    //======================================================
    //======================================================
    private DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) getLastSelectedPathComponent();
    }
    //======================================================
    //======================================================
    public String getSelectionType() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node==null)
            return "No selection";
        else
        if (node.getUserObject() instanceof BrowsedFile)
            return "File";
        else
        if (node.getUserObject() instanceof BrowsedDirectory)
            return "Selection is a directory";
        else
            return "Selection is unknown type";
    }
    //===============================================================
    //===============================================================
    private void createNewDirectory() {
        //  ToDo Get path to create the directory
        Object userObject = getSelectedNode().getUserObject();
        if (userObject instanceof BrowsedDirectory) {
            BrowsedDirectory browsedDirectory = (BrowsedDirectory) userObject;
            System.out.println(browsedDirectory.path);

            String newDirectory = JOptionPane.showInputDialog(this,
                    "Create directory:   "+browsedDirectory.path + "/",
                    "New directory", JOptionPane.QUESTION_MESSAGE);
            try {
                if (newDirectory != null) {
                    DeviceData argIn = new DeviceData();
                    argIn.insert(browsedDirectory.path+newDirectory);
                    managerProxy.command_inout("MakeDirectory", argIn);
                }
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
            }
        }
    }
    //===============================================================
    //===============================================================




    //===============================================================
	/*
	 *	file object definition
	 */
    //===============================================================
    private class BrowsedFile {
        String name;
        String fullName;
        //===========================================================
        private BrowsedFile(String path, String name) throws DevFailed {
            if (name.startsWith(ICommons.FILE_HEADER))
                this.name = name.substring(ICommons.FILE_HEADER.length());
            else
                Except.throw_exception("BadName", name + " is not a file name");
            //  Build full name
            if (!path.endsWith("/"))
                path += '/';
            fullName = path + this.name;
        }
        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }

    //===============================================================
	/*
	 *	Directory object definition
	 */
    //===============================================================
    private class BrowsedDirectory {
        String name;
        String path;
        //===========================================================
        private BrowsedDirectory(String path, String name) throws DevFailed {
            if (name.startsWith(ICommons.DIR_HEADER))
                this.name = name.substring(ICommons.DIR_HEADER.length());
            else
                Except.throw_exception("BadName", name + " is not a directory name");
            if (!path.endsWith("/"))
                path += '/';
            this.path = path + this.name+'/';
        }
        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }
    //===============================================================
    /*
     *  Directory content (list of directories and files)
     */
    //===============================================================
    private class DirectoryContent {
        private List<BrowsedDirectory> directoryList = new ArrayList<>();
        private List<BrowsedFile> fileList = new ArrayList<>();
        //===========================================================
        private DirectoryContent(String path) throws DevFailed {
            DeviceData argIn = new DeviceData();
            argIn.insert(path);
            DeviceData argOut = managerProxy.command_inout("GetFileList", argIn);
            String[] items = argOut.extractStringArray();
            for (String item : items) {
                if (item.startsWith(ICommons.DIR_HEADER))
                    directoryList.add(new BrowsedDirectory(path, item));
                else
                if (item.startsWith(ICommons.FILE_HEADER) && item.endsWith(ICommons.extension))
                    fileList.add(new BrowsedFile(path, item));
            }
        }
        //===========================================================
        public List<BrowsedDirectory> getDirectoryList() { return directoryList; }
        //===========================================================
        public List<BrowsedFile> getFileList() { return fileList; }
        //===========================================================
    }
    //===============================================================
    //===============================================================





    //  Border to add vertical padding between rows
    private static final Border border   = BorderFactory.createEmptyBorder ( 0, 0, 1, 0 );
    private static final Border noBorder = BorderFactory.createEmptyBorder ( 0, 0, 0, 0 );

    private static final Font rootFont      = new Font("Dialog", Font.BOLD, 18);
    private static final Font directoryFont = new Font("Dialog", Font.BOLD, 12);
    private static final Font fileFont      = new Font("Dialog", Font.PLAIN, 12);
    //===============================================================
    /**
     * Renderer Class
     */
    //===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        //===============================================================
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object obj,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, obj, selected,
                    expanded, leaf, row,
                    hasFocus);

            setBackgroundNonSelectionColor(Color.white);
            setForeground(Color.black);
            setBackgroundSelectionColor(Color.lightGray);
            if (row==0) {
                //	ROOT
                setFont(rootFont);
                setIcon(directoryIcon);
                setBorder(border);
                if (selected)
                    setSelectedFileInfo("");
            } else {
                 DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
                 if (node.getUserObject() instanceof BrowsedDirectory) {
                     setFont(directoryFont);
                     setIcon(directoryIcon);
                     setBorder(border);
                     if (selected)
                         setSelectedFileInfo(((BrowsedDirectory)node.getUserObject()).path);
                 } else
                 if (node.getUserObject() instanceof BrowsedFile) {
                     setFont(fileFont);
                     setIcon(fileIcon);
                     setBorder(noBorder);
                     if (selected)
                         setSelectedFileInfo(((BrowsedFile)node.getUserObject()).fullName);
                 }
            }
            return this;
        }
    }//	End of Renderer Class
    //==============================================================================
    //==============================================================================









    //==============================================================================
    //==============================================================================
    private static final int CREATE_DIRECTORY = 0;
    private static final int OFFSET = 2;    //	Label And separator

    private static String[] menuLabels = {
            "Create a directory",
    };
    private class GenerateFilePopupMenu extends JPopupMenu {
        private JLabel title;
        private JTree tree;
        //======================================================
        private GenerateFilePopupMenu(JTree tree) {
            this.tree = tree;
            title = new JLabel();
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 16));
            add(title);
            add(new JPopupMenu.Separator());

            for (String menuLabel : menuLabels) {
                if (menuLabel == null)
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
        private void showMenu(MouseEvent evt, String directoryName) {
            //	Set selection at mouse position
            TreePath selectedPath =
                    tree.getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath == null)
                return;
            tree.setSelectionPath(selectedPath);

            title.setText(directoryName);

            //	Reset all items
            for (int i = 0; i < menuLabels.length; i++)
                getComponent(OFFSET + i).setVisible(false);

            getComponent(OFFSET ).setVisible(true);
            show(tree, evt.getX(), evt.getY());
        }

        //======================================================
        private void hostActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int itemIndex = 0;
            for (int i = 0; i < menuLabels.length; i++)
                if (getComponent(OFFSET + i) == obj)
                    itemIndex = i;

            switch (itemIndex) {
                case CREATE_DIRECTORY:
                    createNewDirectory();
                    break;
            }
        }
    }
    //============================================================
    //============================================================
}
