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

import java.util.Comparator;
import java.util.List;

public class SimpleIndexAdapter extends ArrayAdapter<SimpleIndexAdapter.Contact> implements SectionIndexer {

    private List<Contact> contacts;
    private Context context;
    private static String sections = "abcdefghijklmnopqrstuvwxyz";
    private ImageLoaderManager loaderManager;

    public SimpleIndexAdapter(List<Contact> contacts, Context ctx, ImageLoaderManager loaderManager) {
        super(ctx, R.layout.contact_item, contacts);
        this.contacts = contacts;
        this.context = ctx;
        this.loaderManager = loaderManager;
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

        loaderManager.loadContactPhoto(viewHolder.photo, contact);

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

    static class Contact implements Comparable<Contact> {
        int id;
        String name;
        String lastName;
        String photo;
        String data;

        Contact(int id, String name, String lastName, String photo, String data) {
            this.id = id;
            this.name = name;
            this.lastName = lastName;
            this.photo = photo;
            this.data = data;
        }

        public int compareTo(Contact contact) {
            String lastName = contact.lastName;
            return lastName.compareTo(lastName);
        }

        public static Comparator<Contact> lastNameComparator = new Comparator<Contact>() {

            public int compare(Contact contact1, Contact contact2) {

                String name1 = contact1.lastName.toUpperCase();
                String name2 = contact2.lastName.toUpperCase();

                return name1.compareTo(name2);
            }

        };
    }
}
