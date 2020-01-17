package com.zhoug.plugin.android;

import com.intellij.lang.jvm.JvmClassKind;
import com.intellij.lang.jvm.types.JvmReferenceType;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.zhoug.plugin.android.beans.ResIdBean;
import com.zhoug.plugin.android.utils.PsiClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClassWriter {
    //项目
    private Project project;
    //目标类
    private PsiClass _psiClass;
    //目标类文件eg:xxx.java
    private PsiFile psiFile;
    //
    private List<ResIdBean> resIdBeanList;
    //PsiElement工厂
    private PsiElementFactory _psiElementFactory;

    public static boolean API26 = true;
    public static boolean prefixM = true;

    private PsiClassUtils mPsiClassUtils;


    public ClassWriter(Project project, PsiFile psiFile) {
        this(project, psiFile, null);
    }

    public ClassWriter(Project project, PsiFile psiFile, List<ResIdBean> resIdBeanList) {
        this.project = project;
        this.psiFile = psiFile;
        this.resIdBeanList = resIdBeanList;
        init();
    }

    private void init() {
        _psiElementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
        if (psiFile != null) {
            GlobalSearchScope scope = GlobalSearchScope.fileScope(psiFile);
            String name = psiFile.getName();
            System.out.println("psiFile=" + name);
            name = name.split("\\.")[0];
            _psiClass = PsiShortNamesCache.getInstance(project).getClassesByName(name, scope)[0];
            System.out.println("psiClass=" + _psiClass);
        }
    }


    public ClassWriter setResIdBeanList(List<ResIdBean> resIdBeanList) {
        this.resIdBeanList = resIdBeanList;
        return this;
    }

    /**
     * 生成代码
     */
    public void generateCode() {
        System.out.println("开始生成代码>>>>>>>>>>>>");
        //在类中写入全局变量和初始化方法
        new WriteCommandAction(project, psiFile) {
            @Override
            protected void run(@NotNull Result result) throws Throwable {
                addFields();
                addFindViewsMethod();
            }
        }.execute();
    }

    /**
     * 向类中添加全局变量
     */
    private void addFields() {
        for (ResIdBean resIdBean : resIdBeanList) {
            PsiField psiField = _psiClass.findFieldByName(resIdBean.getFieldName(), false);
            if (!resIdBean.isSelect()) {
                //不需要创建,删除已经创建了的
                if (psiField != null) {
                    psiField.delete();
                }

            } else {
                //判断一下全局变量中是否已经创建了某个id，如果创建了，则不重复创建
                if (psiField == null) {
                    //没创建过
                    String s = "private" + " " +
                            resIdBean.getName() + " " +
                            resIdBean.getFieldName() + ";";
                    System.out.println(s);
                    PsiField fieldFromText = _psiElementFactory.createFieldFromText(s, _psiClass);

                    _psiClass.add(fieldFromText);
                }
            }

        }
    }

    /**
     * 如果已经创建了部分field,那么没创建的设置为false
     */
    public void modifyResIdBean() {
        boolean init = false;
        for (ResIdBean resIdBean : resIdBeanList) {
            //判断一下全局变量中是否已经创建了某个id
            if (_psiClass.findFieldByName(resIdBean.getFieldName(), false) != null) {
                init = true;
                break;
            }
        }
        if (!init) {
            return;
        }
        for (ResIdBean resIdBean : resIdBeanList) {
            if (_psiClass.findFieldByName(resIdBean.getFieldName(), false) == null) {
                resIdBean.setSelect(false);
            }
        }

    }

    /**
     * 向类中添加方法findViews
     */
    private void addFindViewsMethod() {
        PsiMethod[] findViews = _psiClass.findMethodsByName("findViews", false);
        PsiMethod method = findViews.length > 0 ? findViews[0] : null;
        boolean addParams = methodShouldParams();
        if (method != null) {
            //已经创建了方法
            PsiCodeBlock body = method.getBody();
            //这里的PsiStatement数组就是方法中的一行行代码
            PsiStatement[] statements = body.getStatements();
            for (ResIdBean resIdBean : resIdBeanList) {
                String findViewById = createFindViewById(resIdBean, addParams);
//                System.out.println("findViewById="+findViewById);
                boolean created = false;
                //判断是否已经有代码findViewById
                PsiStatement createPsiStatement = null;
                for (PsiStatement psiStatement : statements) {
                    String text = psiStatement.getText().trim().replace(" ", "");
                    if (text.contains(findViewById)) {
                        createPsiStatement = psiStatement;
                        created = true;
                        System.out.println("有代码:" + psiStatement.getText());
                        break;
                    }
                }

                //需要创建findViewById
                if (resIdBean.isSelect() && !created) {
                    PsiStatement statementFromText = _psiElementFactory.createStatementFromText(findViewById, body);
                    body.add(statementFromText);
                    System.out.println("需要创建:" + findViewById);
                } else if (!resIdBean.isSelect() && created) {
                    //不需要创建findViewById 删除
                    createPsiStatement.delete();
                    System.err.println("删除:" + createPsiStatement.getText());
                }
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            }

        } else {
            //创建方法
            StringBuilder methodBuffer = new StringBuilder();
            if (addParams) {
                methodBuffer.append("private void findViews(View view){");
            } else {
                methodBuffer.append("private void findViews(){");
            }
            for (ResIdBean resIdBean : resIdBeanList) {
                if (!resIdBean.isSelect()) {
                    continue;
                }
                methodBuffer.append(createFindViewById(resIdBean, addParams));
            }
            methodBuffer.append("}");
            String s = methodBuffer.toString();
            System.out.println(s);
            PsiMethod methodFromText = _psiElementFactory.createMethodFromText(s, _psiClass);
            _psiClass.add(methodFromText);
        }
    }

    /**
     * 根据ResIdBean创建 findViewById代码
     *
     * @param resIdBean
     * @param addParams
     * @return
     */
    private String createFindViewById(ResIdBean resIdBean, boolean addParams) {
        StringBuffer findViewById = new StringBuffer();
        findViewById.append(resIdBean.getFieldName());
        findViewById.append("=");
        //需要强制装换
        if (!API26) {
            findViewById.append("(");
            findViewById.append(resIdBean.getName());
            findViewById.append(")");
        }
        if (addParams) {
            findViewById.append("view.");
        }
        findViewById.append("findViewById(");
        findViewById.append("R.id.");
        findViewById.append(resIdBean.getId());
        findViewById.append(");");

        return findViewById.toString();
    }


    /**
     * 判断是否为需要在findViews方法中添加参数生成方法findViews(View view)
     *
     * @return
     */
    private boolean methodShouldParams() {
        boolean params = false;
        if (mPsiClassUtils == null) {
            mPsiClassUtils = new PsiClassUtils(project);
        }
        System.out.println(">>>>>>>>是否是Fragment>>>>>>>>>");
        if(mPsiClassUtils.isFragment(_psiClass)){
            //当前类为fragment
            params = true;
        }
        return params;
    }


}
