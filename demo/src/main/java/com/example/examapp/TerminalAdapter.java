package com.example.examapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.conference.model.TerminalsListBean;

public class TerminalAdapter extends BaseViewAdapter<TerminalsListBean>{

    public TerminalAdapter(Context context) {
        super(context);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_terminal_list, null);
        }
        LinearLayout itemview = BaseViewHolder.get(convertView, R.id.item_terminalList_itemview);
        CheckBox checkBox = BaseViewHolder.get(convertView, R.id.item_terminalList_checkBox);
        TextView titleText = BaseViewHolder.get(convertView, R.id.item_terminalList_titleText);
        TerminalsListBean bean = getDatas().get(position);

        checkBox.setChecked(bean.isSelected());

        if (bean.isOnline())
            titleText.setText(bean.getName() + "(在线）");
        else
            titleText.setText(bean.getName() + "(离线）");

        return convertView;
    }
}
