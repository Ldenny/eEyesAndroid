package com.idv.napchen.asynctest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ItemArrayAdapter extends ArrayAdapter<String[]> {
	private List<String[]> valueList = new ArrayList<String[]>();

    static class ItemViewHolder {
        TextView number;
        TextView sensorID;
        TextView tempValue;
        TextView humidValue;
        TextView dateTime;

    }

    public ItemArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

	@Override
	public void add(String[] object) {
		valueList.add(object);
		super.add(object);
	}

    @Override
	public int getCount() {
		return this.valueList.size();
	}

    @Override
	public String[] getItem(int index) {
		return this.valueList.get(index);
	}

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
        ItemViewHolder viewHolder;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.csvitem_layout, parent, false);
            viewHolder = new ItemViewHolder();
            viewHolder.number = (TextView) row.findViewById(R.id.number);
            viewHolder.sensorID = (TextView) row.findViewById(R.id.sensorID);
            viewHolder.tempValue = (TextView) row.findViewById(R.id.tempValue);
            viewHolder.humidValue = (TextView) row.findViewById(R.id.humidValue);
            viewHolder.dateTime = (TextView) row.findViewById(R.id.dateTime);
            row.setTag(viewHolder);
		} else {
            viewHolder = (ItemViewHolder)row.getTag();
        }
        String[] stat = getItem(position);
        if(stat.length == 5) {
            viewHolder.number.setText(stat[0]);
            viewHolder.sensorID.setText(stat[1]);
            viewHolder.tempValue.setText(stat[2]);
            viewHolder.humidValue.setText(stat[3]);
            viewHolder.dateTime.setText(stat[4]);
        } else if (stat.length == 4){
            viewHolder.number.setText(stat[0]);
            viewHolder.sensorID.setText(stat[1]);
            viewHolder.tempValue.setText(stat[2]);
            viewHolder.dateTime.setText(stat[3]);

        }
		return row;
	}
}
