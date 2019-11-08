package com.zhoug.plugin.android.utils;

import com.zhoug.plugin.android.beans.ResIdBean;

import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ActionUtils {
    private final static String[] HEADERS = {"Selected", "Element", "ID","FieldName"};

    public static DefaultTableModel getTableModel(List<ResIdBean> resIdBeans, TableModelListener tableModelListener) {
        DefaultTableModel tableModel = new DefaultTableModel();
        int size = resIdBeans.size();
        Object[][] cellData = new Object[size][5];
        for (int i = 0; i < size ; i++) {
            ResIdBean resIdBean = resIdBeans.get(i);
            cellData[i][0] = resIdBean.isSelect();
            cellData[i][1] = resIdBean.getName();
            cellData[i][2] = resIdBean.getId();
            cellData[i][3] = resIdBean.getFieldName();
        }

        tableModel = new DefaultTableModel(cellData, HEADERS) {
            final Class[] typeArray = {Boolean.class, Object.class, Object.class, Object.class};

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @SuppressWarnings("rawtypes")
            public Class getColumnClass(int column) {
                return typeArray[column];
            }


        };
        tableModel.addTableModelListener(tableModelListener);
        return tableModel;
    }
}
