package com.example.b2001140_dbmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends BaseAdapter {
    private ArrayList<ListViewItem> listViewItems = new ArrayList<ListViewItem>();
    public ListViewAdapter(){
        //생성자
    }
    public void updateItem(int index, ListViewItem element){
        listViewItems.set(index,element);
    }

    public void addItem(Integer no, String date,double lat, double log, String time){
        ListViewItem item = new ListViewItem();

        item.setNo(no);
        item.setDate(date);
        item.setLat(lat);
        item.setLog(log);
        item.setTime(time);
        listViewItems.add(item);
    }
    @Override
    public int getCount() {
        return listViewItems.size();
    }

    @Override
    public ListViewItem getItem(int position) {
        return listViewItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clearItems(){
        listViewItems.clear();
    }

    public  void  deleteItem(int index){
        listViewItems.remove(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context =  parent.getContext();
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item,parent,false);
        }
        TextView textView2, textView3, textView4, textView5, textView6;
        textView2 =(TextView) convertView.findViewById(R.id.textView2);  //번호
        textView3 =(TextView) convertView.findViewById(R.id.textView3);  //날짜
        textView4 =(TextView) convertView.findViewById(R.id.textView4);  //위도
        textView5 =(TextView) convertView.findViewById(R.id.textView5); //경도
        textView6 =(TextView) convertView.findViewById(R.id.textView6); //시간

        ListViewItem item= listViewItems.get(position);


        textView2.setText(String.valueOf(item.getNo()));
        textView3.setText(String.valueOf(item.getDate()));
        textView4.setText(String.valueOf(item.getLat()));
        textView5.setText(String.valueOf(item.getLog()));
        textView6.setText(String.valueOf(item.getTime()));

        return convertView;
    }
}
