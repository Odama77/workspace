package org.span.manager;

import java.util.ArrayList;

import org.span.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter{

	ArrayList<String> messageArrayList;
	Context context;
	static LayoutInflater inflater = null;
	
	public ChatAdapter(Context context, ArrayList<String> messageArrayList){
		this.context = context;
		this.messageArrayList = messageArrayList;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return messageArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return messageArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = convertView;
		if (view == null)
            view = inflater.inflate(R.layout.item_view, null);
		
		TextView textview = (TextView)view.findViewById(R.id.item_view_id);
        textview.setText(messageArrayList.get(position));
		
		return view;
	}

}
