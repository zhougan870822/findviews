package com.zhoug.plugin.android.beans;

public class ViewPart {
    private boolean select=true;
    private String element;//标签名,即组件类名
    private String id;//标签id





    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String generateName() {
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

}
