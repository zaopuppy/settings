package com.zero.tools.javaclassviewer.ui;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceClassVisitor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by zero on 5/1/16.
 */
public class MainForm extends JFrame {
    private JTextField classPathText_;
    private JButton viewClassButton_;
    private JTextArea classInfoText_;
    private JPanel mainPanel_;

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

        viewClassButton_.addActionListener((ActionEvent e) -> {
            classInfoText_.setText("");
            displayClassInfo(classPathText_.getText());
        });
    }

    private void showMessage(String msg) {
        classInfoText_.append(msg + '\n');
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
                SwingUtilities.invokeLater(() -> {
                    showMessage(out.toString());
                });
            }

            // check frames for all methods
            {
                ClassNode classNode = new ClassNode();
                ClassReader reader =
                    new ClassReader(new BufferedInputStream(new FileInputStream(classPath)));
                reader.accept(classNode, 0);

                for (MethodNode methodNode: (java.util.List<MethodNode>)classNode.methods) {
                    analyseMethod(classNode, methodNode);
                }
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
    }
}
