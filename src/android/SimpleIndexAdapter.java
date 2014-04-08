package org.apache.cordova.contactlist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.qordinate.mobile.R;

import java.util.List;

public class SimpleIndexAdapter extends ArrayAdapter<SimpleIndexAdapter.Contact> implements SectionIndexer {

    private List<Contact> contacts;
    private Context context;
    private static String sections = "abcdefghijklmnopqrstuvwxyz";

    public SimpleIndexAdapter(List<Contact> contacts, Context ctx) {
        super(ctx, R.layout.contact_item, contacts);
        this.contacts = contacts;
        this.context = ctx;
    }

    public int getCount() {
        return contacts.size();
    }

    public Contact getItem(int position) {
        return contacts.get(position);
    }

    public long getItemId(int position) {
        return contacts.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.contact_item, null);
            //v.setBackgroundResource(android.R.color.transparent);

            viewHolder = new ViewHolder();
            viewHolder.photo = (ImageView) v.findViewById(R.id.photo);
            viewHolder.name = (TextView) v.findViewById(R.id.name);
            viewHolder.data = (TextView) v.findViewById(R.id.data);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        Contact contact = getItem(position);
        viewHolder.name.setText(contact.name);
        viewHolder.data.setText(contact.data);

        return v;

    }

    @Override
    public int getPositionForSection(int section) {
//        Log.d("ListView", "Get position for section: " + section);
        for (int i = 0; i < getCount(); i++) {
            String item = getItem(i).name.toLowerCase();
            if (item.charAt(0) == sections.charAt(section)) {
                Log.d("ListView", "Get position for section: " + sections.charAt(section) + " " +  item);
                return i;
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int arg0) {
        Log.d("ListView", "Get section");
        return 0;
    }

    @Override
    public Object[] getSections() {
        Log.d("ListView", "Get sections");
        String[] sectionsArr = new String[sections.length()];
        for (int i = 0; i < sections.length(); i++)
            sectionsArr[i] = "" + sections.charAt(i);

        return sectionsArr;
    }

    static class ViewHolder {
        TextView name;
        TextView data;
        ImageView photo;
    }

    static class Contact {
        String id;
        String name;
        String photo;
        String data;

        Contact(String id, String name, String photo, String data) {
            this.id = id;
            this.name = name;
            this.photo = photo;
            this.data = data;
        }
    }
}