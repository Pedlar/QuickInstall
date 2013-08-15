package com.hostgator.quickinstall;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InstallListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<GroupParent> groups;
    public InstallListAdapter(Context context, ArrayList<GroupParent> groups) {
        this.context = context;
        this.groups = groups;
    }
    
    public void addItem(GroupChild item, GroupParent group) {
        if (!groups.contains(group)) {
            groups.add(group);
        }
        int index = groups.indexOf(group);
        ArrayList<GroupChild> ch = groups.get(index).getItems();
        ch.add(item);
        groups.get(index).setItems(ch);
    }
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        ArrayList<GroupChild> chList = groups.get(groupPosition).getItems();
        return chList.get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view,
            ViewGroup parent) {
        GroupChild child = (GroupChild) getChild(groupPosition, childPosition);
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.install_child_item, null);
        }
        TextView tv = (TextView) view.findViewById(R.id.tvChild);
        tv.setText(child.getName().toString());
        tv.setTag(child.getTag());
        
        ImageView img = (ImageView)view.findViewById(R.id.app_image);
        img.setImageBitmap(child.getIcon());
        
        view.setTag(child.getTag());
        // TODO Auto-generated method stub
        return view;
    }

    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        ArrayList<GroupChild> chList = groups.get(groupPosition).getItems();

        return chList.size();

    }

    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return groups.get(groupPosition);
    }

    public int getGroupCount() {
        // TODO Auto-generated method stub
        return groups.size();
    }

    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isLastChild, View view,
            ViewGroup parent) {
        GroupParent group = (GroupParent) getGroup(groupPosition);
        if (view == null) {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.install_group_item, null);
        }
        TextView tv = (TextView) view.findViewById(R.id.tvGroup);
        tv.setText(group.getName());
        // TODO Auto-generated method stub
        return view;
    }

    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return true;
    }

    public boolean isChildSelectable(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return true;
    }

}