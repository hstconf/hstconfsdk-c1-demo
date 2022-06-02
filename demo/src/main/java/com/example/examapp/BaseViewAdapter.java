package com.example.examapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class BaseViewAdapter<T> extends BaseAdapter {

	private Context context;
	private List<T> datas;

	public BaseViewAdapter(Context context) {
		super();
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	public List<T> getDatas() {
		return datas;
	}

	public void setDatas(List<T> datas) {
		
		this.datas = datas;
	}

	@Override
	public int getCount() {
		if (null != datas) {
			return datas.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
}
