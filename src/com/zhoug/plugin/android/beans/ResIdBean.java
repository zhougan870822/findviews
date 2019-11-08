package com.zhoug.plugin.android.beans;

import com.zhoug.plugin.android.ClassWriter;

public class ResIdBean {
    private String name;//标签名,即组件类名
    private String id;//标签id
    private String _fieldName;//生成的全局变量的名字
    private boolean addOnclick=false;
    private boolean select=true;//是否生成

    public ResIdBean() {

    }

    public boolean isAddOnclick() {
        return addOnclick;
    }

    public void setAddOnclick(boolean addOnclick) {
        this.addOnclick = addOnclick;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public ResIdBean(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;

    }

    public String getFieldName() {
        if(_fieldName ==null){
            if(ClassWriter.prefixM){
                _fieldName =getId2();
            }else{
                _fieldName =getId1();
            }
        }
        return _fieldName;
    }

    public void reFieldName(){
        _fieldName=null;
        getFieldName();
    }


    private String getId1() {
        String[] split = id.split("_");
        String id = "";
        for (int i = 0; i < split.length; i++) {
            String s1 = split[i];
            if (i != 0) {
                String first = s1.substring(0, 1);
                String last = s1.substring(1);
                id += first.toUpperCase() + last;
            } else {
                id += s1;
            }
        }

        return id;
    }

    private String getId2() {
        String[] split = id.split("_");
        String id = "m";
        for (int i = 0; i < split.length; i++) {
            String s1 = split[i];
            String first = s1.substring(0, 1);
            String last = s1.substring(1);
            id += first.toUpperCase() + last;
        }

        return id;
    }



    @Override
    public String toString() {
        return "name:" + name + ",id=" + id;
    }
}
