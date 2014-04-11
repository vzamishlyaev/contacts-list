package org.apache.cordova.contactlist;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.qordinate.mobile.R;

public class SimpleIndexAdapter extends ArrayAdapter<SimpleIndexAdapter.Contact> implements SectionIndexer {

    private static final String SECTIONS = "abcdefghijklmnopqrstuvwxyz#";

    private List<Contact> contacts;
    private List<Contact> contactItems;
    private Context context;
    private ImageLoaderManager loaderManager;
    private ClickListener listener;
    private HashMap<Character, Integer> letterPositions;

    private int textColorGreen;
    private int textColorWhite;

    public SimpleIndexAdapter(List<Contact> contacts, Context ctx, ImageLoaderManager loaderManager, ClickListener listener) {
        super(ctx, R.layout.contact_item, contacts);
        this.contacts = contacts;
        this.context = ctx;
        this.loaderManager = loaderManager;
        this.listener = listener;

        textColorGreen = Color.parseColor(ctx.getString(R.color.main_green));
        textColorWhite = Color.parseColor(ctx.getString(R.color.white));

        contactItems = new ArrayList<Contact>();
        letterPositions = new HashMap<Character, Integer>();

        char currentHeaderLetter = contacts.get(0).lastName.toUpperCase().charAt(0);

        int i = 0;

        contactItems.add(new ContactItem(String.valueOf(currentHeaderLetter)));
        letterPositions.put(currentHeaderLetter, i++);
        for (Contact contact : contacts) {
            char ch = contact.lastName.toUpperCase().charAt(0);

            if (ch < 'A' || ch > 'Z') {
                ch = '#';
            }

            if (ch != currentHeaderLetter) {
                currentHeaderLetter = ch;
                contactItems.add(new ContactItem(String.valueOf(currentHeaderLetter)));
                letterPositions.put(currentHeaderLetter, i);
                i++;
            }
            contactItems.add(contact);
            i++;
        }

    }

    public int getCount() {
        return contactItems.size();
    }

    public Contact getItem(int position) {
        return contactItems.get(position);
    }

    public long getItemId(int position) {
        return contactItems.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.contact_item, null);

            viewHolder = new ViewHolder();
            viewHolder.photo = (ImageView) v.findViewById(R.id.photo);
            viewHolder.name = (TextView) v.findViewById(R.id.name);
            viewHolder.data = (TextView) v.findViewById(R.id.data);
            viewHolder.listHeaderTitle = (TextView) v.findViewById(R.id.list_header_title);
            viewHolder.quickConnect = (TextView) v.findViewById(R.id.quick_connect);
            viewHolder.frame = v.findViewById(R.id.main_frame);

            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final Contact contact = getItem(position);

        boolean isHeaderItem = contact instanceof ContactItem;

        viewHolder.frame.setVisibility(isHeaderItem ? View.GONE : View.VISIBLE);
        viewHolder.listHeaderTitle.setVisibility(isHeaderItem ? View.VISIBLE : View.GONE);

        if (isHeaderItem) {
            viewHolder.listHeaderTitle.setText(contact.name);
        } else {
            viewHolder.name.setText(contact.name);
            viewHolder.data.setText(contact.data);
            viewHolder.frame.setBackgroundResource(contact.isConnected ? R.color.light_green : R.color.white);

            loaderManager.loadContactPhoto(viewHolder.photo, contact);
            v.setSelected(contact.isConnected);
        }

        viewHolder.quickConnect.setText(contact.isConnected ? R.string.introduce : R.string.quick_connect);
        viewHolder.quickConnect.setTextColor(contact.isConnected ? textColorGreen : textColorWhite);
        viewHolder.quickConnect.setBackgroundResource(contact.isConnected ? R.drawable.empty_frame : R.drawable.filled_frame);
                viewHolder.quickConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contact.isConnected) {
                    listener.onIntroduce(contact);
                } else {
                    listener.onQuickConnect(contact);
                }
            }
        });

        viewHolder.frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(contact);
            }
        });

        return v;

    }

    @Override
    public int getPositionForSection(int section) {
        return letterPositions.get(Character.toUpperCase(SECTIONS.charAt(section)));
    }

    @Override
    public int getSectionForPosition(int arg0) {
        Log.d("ListView", "Get section");
        return 0;
    }

    @Override
    public Object[] getSections() {
        Log.d("ListView", "Get SECTIONS");
        String[] sectionsArr = new String[SECTIONS.length()];
        for (int i = 0; i < SECTIONS.length(); i++) {
            sectionsArr[i] = "" + SECTIONS.charAt(i);
        }

        return sectionsArr;
    }


    static class ViewHolder {
        TextView name;
        TextView data;
        ImageView photo;
        TextView quickConnect;
        View frame;
        TextView listHeaderTitle;
    }

    static class Contact implements Comparable<Contact> {
        int id;
        boolean isConnected;
        String name;
        String lastName;
        String photo;
        String data;

        private Contact() {}

        Contact(int id, boolean isConnected, String name, String lastName, String photo, String data) {
            this.id = id;
            this.isConnected = isConnected;
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

                boolean isCh1 = Character.isLetter(name1.charAt(0));
                boolean isCh2 = Character.isLetter(name2.charAt(0));

                return isCh1 && isCh2 ? name1.compareTo(name2) : isCh1 ? -1 : 1;
            }

        };
    }

    // can be used as contact item or as contact header
    static class ContactItem extends Contact {

        ContactItem(String name) {
            this.name = name;
        }

    }

    static interface ClickListener {
        void onQuickConnect(Contact contact);
        void onIntroduce(Contact contact);
        void onItemClick(Contact contact);
    }

}
