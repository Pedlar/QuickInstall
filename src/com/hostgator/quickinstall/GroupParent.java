package com.hostgator.quickinstall;

import java.util.ArrayList;

public class GroupParent {
 
    private String Name;
    private ArrayList<GroupChild> Items;
    
    public String getName() {
        return Name;
    }
    public void setName(String name) {
        this.Name = name;
    }
    public ArrayList<GroupChild> getItems() {
        return Items;
    }
    public void setItems(ArrayList<GroupChild> Items) {
        this.Items = Items;
    }
}
