package com.zhoug.plugin.android.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;

public class PsiClassUtils {
    public PsiClass activity;
    public PsiClass fragment;
    public PsiClass supportFragment;

    public PsiClassUtils(Project project) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        activity = JavaPsiFacade.getInstance(project).findClass("android.app.Activity", scope);
        fragment = JavaPsiFacade.getInstance(project).findClass("android.app.Fragment", scope);
        supportFragment = JavaPsiFacade.getInstance(project).findClass("android.support.v4.app.Fragment", scope);
    }

    /**
     * 判断psiClass是否是parentClass的子类
     * @param psiClass
     * @param parentClass
     * @return
     */
    public boolean isChild(PsiClass psiClass,PsiClass parentClass) {
        if(parentClass!=null && psiClass!=null){
            //判断psiClass是否继承parentClass
            if (psiClass.isInheritor(parentClass, false)) {
                System.out.println("是子类");
                return true;
            }else{
                //判断psiClass的父类是否继承parentClass
                psiClass = psiClass.getSuperClass();
                System.out.println("psiClass="+psiClass);
                return isChild(psiClass,parentClass);
            }
        }else{
            return false;
        }
    }

    /**
     * 是否是activity
     * @param psiClass
     * @return
     */
    public boolean isActivity(PsiClass psiClass){
        return isChild(psiClass,activity);
    }

    /**
     * 是否是Fragment
     * @param psiClass
     * @return
     */
    @Deprecated
    public boolean isFragment1(PsiClass psiClass){
        return isChild(psiClass,fragment) ||isChild(psiClass,supportFragment);
    }

    /**
     * 是否是Fragment 精简算法
     * @param psiClass
     * @return
     */
    public boolean isFragment(PsiClass psiClass){
        if(psiClass!=null){
            if((fragment!=null && psiClass.isInheritor(fragment,false)) ||
                    (supportFragment!=null && psiClass.isInheritor(supportFragment,false))){
                return true;
            }else{
                //判断psiClass的父类是否继承parentClass
                psiClass = psiClass.getSuperClass();
                System.out.println("psiClass="+psiClass);
                return isFragment(psiClass);
            }
        }else{
            return false;
        }
    }

}
