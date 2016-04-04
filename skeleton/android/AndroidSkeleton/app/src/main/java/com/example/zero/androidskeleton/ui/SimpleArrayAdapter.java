package com.example.zero.androidskeleton.ui;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.zero.androidskeleton.R;

/**
 * Created by zero on 2016/4/4.
 */
public class SimpleArrayAdapter extends ArrayAdapter<BluetoothDevice> {

    private final LayoutInflater mInflater;
    private final int mResourceId;

    public SimpleArrayAdapter(Context context, int resource) {
        super(context, resource);

        mInflater = LayoutInflater.from(getContext());
        mResourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // return super.getView(position, convertView, parent);
        if (convertView == null) {
            convertView = mInflater.inflate(mResourceId, parent, false);
        }

        BluetoothDevice device = getItem(position);

        TextView nameView = (TextView) convertView.findViewById(R.id.name);
        TextView addressView = (TextView) convertView.findViewById(R.id.address);

        nameView.setText(device.getName());
        addressView.setText(device.getName());

        return convertView;
    }
}
