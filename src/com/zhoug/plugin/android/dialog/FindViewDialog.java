package com.zhoug.plugin.android.dialog;

import com.zhoug.plugin.android.ClassWriter;
import com.zhoug.plugin.android.beans.ResIdBean;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.List;

public class FindViewDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable tableView;
    private JButton SelectAll;
    private JCheckBox cbapi26;
    private JCheckBox cbM;

    private OnListener onListener;
    private List<ResIdBean> resIdBeanList;
    private boolean allSelected=true;
    private static boolean api26=true;
    private static boolean m=true;

    public FindViewDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        tableView.setRowHeight(30);

        SelectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(allSelected){
                    allSelected=false;
                }else{
                    allSelected=true;
                }
                onListener.onSelectAll(allSelected);

            }
        });
        cbapi26.setSelected(ClassWriter.API26);
        cbapi26.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ClassWriter.API26=cbapi26.isSelected();
            }
        });

        cbM.setSelected(ClassWriter.prefixM);
        cbM.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ClassWriter.prefixM=cbM.isSelected();
                onListener.onPrefixM(cbM.isSelected());
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onListener.onOk();
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onListener.onCancel();
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public void setTableModel(DefaultTableModel model) {
        tableView.setModel(model);
        tableView.getColumnModel().getColumn(0).setPreferredWidth(20);
    }


  /*  public static void main(String[] args) {
        FindViewDialog dialog = new FindViewDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }*/



    public void setOnListener(OnListener onListener) {
        this.onListener = onListener;
    }

    public interface OnListener{
         void onSelectAll(boolean selectAll);
         void onPrefixM(boolean addPrefixM);
         void onOk();
         void onCancel();


    }
}
