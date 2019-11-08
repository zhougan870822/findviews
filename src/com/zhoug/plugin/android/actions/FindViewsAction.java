package com.zhoug.plugin.android.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.zhoug.plugin.android.ClassWriter;
import com.zhoug.plugin.android.beans.ResIdBean;
import com.zhoug.plugin.android.dialog.FindViewDialog;
import com.zhoug.plugin.android.utils.ActionUtils;
import com.zhoug.plugin.android.utils.ToastUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class FindViewsAction extends AnAction {
    private static final String TAG = "FindViewsAction";
    private List<ResIdBean> resIdBeanList = new ArrayList<>();
    private DefaultTableModel tableModel;

    //编辑器
    private Editor editor;
    //事件发生所在的文件
    private PsiFile psiFile;
    //项目
    private Project project;
    //Module
    private Module module;

    private FindViewDialog findViewDialog;
    private ClassWriter classWriter;



    @Override
    public void update(AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getData(PlatformDataKeys.EDITOR);
        if (editor != null) {
//            e.getPresentation().setEnabled(true);
            PsiFile  psiFile = anActionEvent.getData(PlatformDataKeys.PSI_FILE);
            if(psiFile!=null && psiFile.getName().endsWith(".java")){
                anActionEvent.getPresentation().setVisible(true);
            }else{
                System.out.println("非java文件不显示插件按钮");
                anActionEvent.getPresentation().setVisible(false);
            }
        } else {
//            e.getPresentation().setEnabled(false);//回收,不可点击
            anActionEvent.getPresentation().setVisible(false);
        }

    }


    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        editor = anActionEvent.getData(PlatformDataKeys.EDITOR);
        psiFile = anActionEvent.getData(PlatformDataKeys.PSI_FILE);
        project = anActionEvent.getData(PlatformDataKeys.PROJECT);
        module = ModuleUtil.findModuleForFile(psiFile);

        //光标下的元素
        PsiElement psiElement = getPsiElement();
        if (psiElement == null) {
            ToastUtils.toastShort(editor, "psiElement=null");
            return;
        }
        //文件名,记住要拼接上后缀
        String xMlFileName = String.format("%s.xml", psiElement.getText());
        //查找文件
        PsiFile xmlFile = getFileByName(xMlFileName);
        if (xmlFile == null) {
            ToastUtils.toastShort(editor, "没有找到文件:" + xMlFileName);
            return;
        }
        //遍历文件元素,找到有id的组件
        resIdBeanList.clear();
        getResIdBean(xmlFile, resIdBeanList);

        if (resIdBeanList.size() == 0) {
            ToastUtils.toastShort(editor, "没有带id的组件:" + xMlFileName);
            return;
        }
        classWriter= new ClassWriter(project,psiFile,resIdBeanList);
        classWriter.modifyResIdBean();

        if (findViewDialog == null) {
            findViewDialog = new FindViewDialog();
        }
        findViewDialog.setOnListener(onDialogListener);
        updateTable();
        findViewDialog.setTitle("FindViewById");
        findViewDialog.pack();
        findViewDialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(anActionEvent.getProject()));
        findViewDialog.setVisible(true);


    }

    private void updateTable() {
        tableModel = ActionUtils.getTableModel(resIdBeanList, tableModelListener);
        findViewDialog.setTableModel(tableModel);
    }




    /**
     * 遍历文件元素 获取有id的元素集合
     *
     * @param xmlFile
     * @param resIdBeanList
     */
    private void getResIdBean(PsiFile xmlFile, List<ResIdBean> resIdBeanList) {
        if (xmlFile == null) return;

        xmlFile.accept(new XmlRecursiveElementVisitor(true) {
            @Override
            public void visitXmlTag(XmlTag tag) {
                super.visitXmlTag(tag);
                String className = tag.getName();
                if (className.contains(".")) {
                    className = className.substring(className.lastIndexOf(".") + 1);

                }
                if (className.equals("include")) {
                    XmlAttribute layout = tag.getAttribute("layout");
                    if (layout != null) {
                        String value = layout.getValue();
                        if (value != null) {
                            String fileName = value.split("/")[1] + ".xml";
                            PsiFile fileByName = getFileByName(fileName);
                            getResIdBean(fileByName, resIdBeanList);
                        }
                    }
                } else {
                    XmlAttribute attribute = tag.getAttribute("android:id");
                    if (attribute != null) {
                        String value = attribute.getValue();
                        if (value != null) {
                            String[] split = value.split("/");
                            ResIdBean resIdBean = new ResIdBean(className, split[1]);
                            resIdBeanList.add(resIdBean);
                        }
                    }
                }
            }
        });

    }

    /**
     * 根据文件名查找文件
     *
     * @param xmlFileName
     * @return
     */
    private PsiFile getFileByName(String xmlFileName) {
        //获取文件
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, xmlFileName, GlobalSearchScope.moduleScope(module));
        String msg = "文件名:" + xmlFileName;
        msg += "---module:" + (module != null ? module.getName() : "module==null");
        msg += "---文件:" + (filesByName.length > 0 ? filesByName[0] : "filesByName.length=0");
        System.out.println(msg);
//        showDialog(editor,msg,3);
        if (filesByName.length == 0) {
            return null;
        }
        return filesByName[0];
    }

    /**
     * 获取光标下面的元素
     *
     * @return
     */
    private PsiElement getPsiElement() {
        PsiElement psiElement = null;
        if (editor != null) {
            //得到编辑器的光标类
            CaretModel caretModel = editor.getCaretModel();
            //程序结构接口
            //光标开始位置
            int offset = caretModel.getOffset();
            if (psiFile != null) {
                //光标下面的元素(单词)
                PsiElement element = psiFile.findElementAt(offset);
                PsiElement element1 = psiFile.findElementAt(offset - 1);
                //注意光标在R.layout.activity_main的后面时选择的是")" 所以需要判断
                String text = element.getText();
                String text1 = element1.getText();
                //文件名

                if (!".".equals(text) && !")".equals(text) && !",".equals(text)) {
                    psiElement = element;
                } else {
                    psiElement = element1;

                }
            }
        }
        return psiElement;
    }


    private FindViewDialog.OnListener onDialogListener = new FindViewDialog.OnListener() {
        @Override
        public void onSelectAll(boolean selectAll) {
            for (ResIdBean resIdBean : resIdBeanList) {
                resIdBean.setSelect(selectAll);
            }
            updateTable();

        }

        @Override
        public void onPrefixM(boolean addPrefixM) {
            for (ResIdBean resIdBean : resIdBeanList) {
                resIdBean.reFieldName();
            }
            updateTable();
        }

        @Override
        public void onOk() {
            classWriter.generateCode();
        }

        @Override
        public void onCancel() {

        }

    };

    private TableModelListener tableModelListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent event) {
            if (tableModel == null) {
                return;
            }
            int row = event.getFirstRow();
            int column = event.getColumn();
            //第一列是否勾选
            try {
                if (column == 0) {
                    Boolean isSelected = (Boolean) tableModel.getValueAt(row, column);
                    resIdBeanList.get(row).setSelect(isSelected);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
