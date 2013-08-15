package com.hostgator.quickinstall;

import java.util.ArrayList;
import java.util.HashMap;
 
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class ManageListAdapter extends BaseAdapter {
 
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;

    public ManageListAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return data.size();
    }
 
    public HashMap<String, String> getItem(int position) {
        return data.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
    
    public void setData(ArrayList<HashMap<String, String>> d) {
        data = new ArrayList<HashMap<String, String>>();
        data = d;
    }
    
    public void removeItem(int position) {
        data.remove(position);
        notifyDataSetChanged();
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = inflater.inflate(R.layout.manage_list_row, null);
 
        ImageView img = (ImageView)vi.findViewById(R.id.list_image);
        TextView title = (TextView)vi.findViewById(R.id.name);
        TextView url_path = (TextView)vi.findViewById(R.id.url_path);
        TextView formatted_time = (TextView)vi.findViewById(R.id.formatted_time);
        TextView size = (TextView)vi.findViewById(R.id.size);
        TextView upgrade_avail = (TextView)vi.findViewById(R.id.upgrade_avail);
 
        HashMap<String, String> install = new HashMap<String, String>();
        install = data.get(position);
        
        vi.setTag(ManageApplications.ID, install.get("id") + "_" + install.get("install_start"));
        img.setImageBitmap(MainActivity.qApi.getIcon(install.get("icon")));
        title.setText(install.get("name") + " " + install.get("version"));
        url_path.setText(install.get("app_domain") + "/" + install.get("url_path"));
        formatted_time.setText(install.get("formatted_time"));
        size.setText(install.get("size"));
        
        if(install.get("updateavail") != null) {
            if(!(install.get("updateavail").equals("none")))
                upgrade_avail.setVisibility(View.VISIBLE);
        }
        
        return vi;
    }
}