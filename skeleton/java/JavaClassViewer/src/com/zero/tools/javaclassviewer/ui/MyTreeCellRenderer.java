package com.zero.tools.javaclassviewer.ui;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Created by zero on 5/7/16.
 */
public class MyTreeCellRenderer extends DefaultTreeCellRenderer {
    //@Override
    //public Component getTreeCellRendererComponent(
    //    JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    //
    //    if (!(value instanceof DefaultMutableTreeNode)) {
    //        throw new IllegalArgumentException("DefaultMutableTreeNode expected");
    //    }
    //
    //    Object obj = ((DefaultMutableTreeNode) value).getUserObject();
    //
    //    if (obj instanceof ClassNode) {
    //        ClassNode node = (ClassNode) obj;
    //        setText("class: " + node.name);
    //    } else if (obj instanceof MethodNode) {
    //        MethodNode node = (MethodNode) obj;
    //        setText("method: " + node.name);
    //    } else {
    //        setText("unknown: " + value.toString());
    //    }
    //
    //    return this;
    //}
}
