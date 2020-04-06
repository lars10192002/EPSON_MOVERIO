package com.epson.moverio.bt350.sample.trackpadcontrol;

import java.util.ArrayList;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.content.Context;

public class KeyValuePairAdapter extends ArrayAdapter<Pair<String, String>> {

    public KeyValuePairAdapter(Context context,
                               int resourceId,
                               ArrayList<Pair<String, String>> list) {
        super(context, resourceId, list);
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getView(pos, convertView, parent);
        textView.setText(getItem(pos).second);
        return textView;
    }

    @Override
    public View getDropDownView(int pos, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getDropDownView(pos,
                convertView,
                parent);
        textView.setText(getItem(pos).second);
        return textView;
    }
}
