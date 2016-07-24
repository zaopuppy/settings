package com.zero.tools.javaclassviewer.ui;


import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.zero.tools.javaclassviewer.TestContinuation;
import org.apache.commons.javaflow.Continuation;
import org.apache.commons.javaflow.ContinuationClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceClassVisitor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.URL;
import java.util.Set;

/**
 * Created by zero on 5/1/16.
 */
public class MainForm extends JFrame {
    private JTextField classPathText_;
    private JButton viewClassButton_;
    private JTextArea classInfoText_;
    private JPanel mainPanel_;
    private JTree classTree_;

    private final WindowListener windowListener_ = new WindowListener() {
        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
            dispose();
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }
    };

    private final DropTargetListener dropTargetListener_ = new DropTargetListener() {
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {

        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {

        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {

        }

        @Override
        public void dragExit(DropTargetEvent dte) {
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            if (!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return;
            }

            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            try {
                Object data = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if (data instanceof java.util.List) {
                    java.util.List list = (java.util.List) data;
                    if (list.size() > 0) {
                        Object o = list.get(0);
                        if (o instanceof File) {
                            File f = (File) o;
                            classPathText_.setText(f.getAbsolutePath());
                            viewClassButton_.doClick();
                        }
                    }
                }
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
        }
    };

    public MainForm(String title) {
        super(title);

        addWindowListener(windowListener_);

        setContentPane(mainPanel_);
        setPreferredSize(new Dimension(900, 500));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        setDragAndDrop();

        // --- FOR DEBUGGING ONLY ---
        ContinuationClassLoader classLoader = new ContinuationClassLoader(new URL[]{}, java.lang.ClassLoader.getSystemClassLoader());
        classLoader.setParentFirst(false);
        try {
            classLoader.loadClass("com.zero.tools.javaclassviewer.TestContinuation");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // --- FOR DEBUGGING ONLY ---
        viewClassButton_.addActionListener((ActionEvent e) -> {
            for (Continuation d = Continuation.startWith(new TestContinuation());
                 d != null; d = Continuation.continueWith(d)) {
                System.out.println("continue");
            }
            // displayClassInfo0("");
            // classInfoText_.setText("");
            // displayClassInfo(classPathText_.getText());
            // displayVMInfo();
        });
    }

    private void displayVMInfo() {
        try {
            for (VirtualMachineDescriptor desc: VirtualMachine.list()) {
                VirtualMachine vm = VirtualMachine.attach(desc.id());
                showMessage(desc.displayName());
                showMessage(vm.toString());
            }
        } catch (AttachNotSupportedException | IOException e) {
            e.printStackTrace();
        }

    }

    private void showMessage(String msg) {
        classInfoText_.setText(msg);
    }

    private void appendMessage(String msg) {
        classInfoText_.append(msg + '\n');
    }

    private void displayClassInfo0(String className) {
        String classFileName = className.replace('.', '/');
        InputStream inStream = ClassLoader.getSystemResourceAsStream(classFileName);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            TraceClassVisitor visitor = new TraceClassVisitor(new PrintWriter(out));
            ClassReader reader = new ClassReader(inStream);
            reader.accept(visitor, 0);
            showMessage(out.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayClassInfo(String classPath) {
        try {
            // Use `TraceClassVisitor` to print bytecode of specific class file
            {
                // MyClassVisitor visitor = new MyClassVisitor();
                ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                TraceClassVisitor visitor = new TraceClassVisitor(new PrintWriter(out));
                ClassReader reader =
                    new ClassReader(new BufferedInputStream(new FileInputStream(classPath)));
                reader.accept(visitor, 0);
                showMessage(out.toString());
            }

            // check frames for all methods
            {
                ClassNode classNode = new ClassNode();
                ClassReader reader =
                    new ClassReader(new BufferedInputStream(new FileInputStream(classPath)));
                reader.accept(classNode, 0);

                DefaultMutableTreeNode root = new DefaultMutableTreeNode(classNode);
                DefaultTreeModel model = new DefaultTreeModel(root);
                classTree_.setModel(model);
                classTree_.setCellRenderer(new MyTreeCellRenderer());
                classTree_.setShowsRootHandles(true);
                classTree_.addTreeSelectionListener(e -> {
                    JTree val = (JTree) e.getSource();
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) val.getLastSelectedPathComponent();
                    if (node != null) {
                        showMessage(node.getUserObject().toString());
                    }
                });
                for (MethodNode methodNode: (java.util.List<MethodNode>)classNode.methods) {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(methodNode);
                    model.insertNodeInto(node, root, root.getChildCount());
                    analyseMethod(classNode, methodNode);
                }
                classTree_.invalidate();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void analyseMethod(ClassNode classNode, MethodNode methodNode) {
        showMessage("==============================");
        showMessage("method: " + methodNode.name);
        Analyzer analyzer = new Analyzer(new SourceInterpreter());

        try {
            analyzer.analyze(classNode.name, methodNode);
        } catch (AnalyzerException e) {
            e.printStackTrace();
            return;
        }

        Frame[] frameList = analyzer.getFrames();
        int idx = 0;
        for (AbstractInsnNode node = methodNode.instructions.getFirst();
             node != null; node = node.getNext(), ++idx) {
            Frame frame = frameList[idx];
            showMessage(
                String.format("[%03d]: [%32s](%02d) --> [%s]",
                    idx, toString(node),
                    org.objectweb.asm.Type.getArgumentTypes(methodNode.desc).length,
                    frame == null ? "null" : toString(frame)));
        }
    }

    private String toString(AbstractInsnNode node) {
        String simpleName = node.getClass().getSimpleName();
        int opcode = node.getOpcode();
        if (opcode < 0) {
            return simpleName + "|<pseudo-insn>";
        }
        return simpleName + "|" + Printer.OPCODES[node.getOpcode()];
    }

    private String toString(Frame frame) {
        StringBuilder builder = new StringBuilder(512);
        int stackSize = frame.getStackSize();
        for (int i = 0; i < stackSize; ++i) {
            SourceValue value = (SourceValue) frame.getStack(i);
            Set<AbstractInsnNode> insns = value.insns;
            builder.append(toString(insns));
            builder.append(',');
        }
        return builder.toString();
    }

    private String toString(Set<AbstractInsnNode> insns) {
        StringBuilder builder = new StringBuilder(512);
        for (AbstractInsnNode node: insns) {
            builder.append(Printer.OPCODES[node.getOpcode()]);
            builder.append('|');
        }
        return builder.toString();
    }

    private void setDragAndDrop() {
        setDropTarget(new DropTarget(
            classPathText_, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetListener_));
        setDropTarget(new DropTarget(
            classInfoText_, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetListener_));
        setDropTarget(new DropTarget(
            classTree_, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetListener_));
    }
}
