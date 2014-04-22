package org.apache.cordova.contactlist;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LinearLayoutSoftKeyboardDetect;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.view.View;
import android.widget.*;
import android.view.*;

public class ContactList extends CordovaPlugin {

    private FastSearchListView listView;
    private ImageLoaderManager loaderManager;
    private int webViewHeight;

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("addContactList")) {

            final int height = args.getInt(0);

            listView = new FastSearchListView(cordova.getActivity());

            listView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            listView.setBackgroundResource(android.R.color.white);
            listView.setCacheColorHint(android.R.color.transparent);
            listView.setScrollingCacheEnabled(false);


            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webViewHeight = webView.getLayoutParams().height;
                    webView.getLayoutParams().height = height;
            		loaderManager = new ImageLoaderManager(new Handler(), cordova.getActivity());
                    getParentView().addView(listView, 1);
                }
            });

            callbackContext.success();
            return true;
        } else if (action.equals("removeContactList")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.getLayoutParams().height = webViewHeight;
                    getParentView().removeView(listView);
                }
            });

            callbackContext.success();
            return true;
        } else if (action.equals("changeContactListVisibility")) {
            final int visibility = args.getInt(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.setVisibility(visibility == 1 ? View.VISIBLE : View.GONE);
                }
            });

            callbackContext.success();
            return true;
        } else if (action.equals("setContacts")) {

            JSONArray jsonContacts = args.getJSONArray(0);

            int len = jsonContacts.length();
            final List<SimpleIndexAdapter.Contact> contacts = new ArrayList<SimpleIndexAdapter.Contact>();

            for (int i = 0; i < len; i++) {
                JSONObject object = (JSONObject) jsonContacts.get(i);
                contacts.add(new SimpleIndexAdapter.Contact(object.getInt("id"), object.getBoolean("connected"), object.getString("name"),
                        object.getString("lastName"), object.getString("photo"), object.getString("data")));
            }

            //Collections.sort(contacts, SimpleIndexAdapter.Contact.lastNameComparator);


            final SimpleIndexAdapter sa = new SimpleIndexAdapter(contacts, cordova.getActivity(), loaderManager, new SimpleIndexAdapter.ClickListener() {
                @Override
                public void onQuickConnect(SimpleIndexAdapter.Contact contact) {
                    System.out.println(":::::::::: onQuickConnect  " + contact.name);
                    webView.loadUrl(String.format("javascript:onQuickConnect(%s, %s)", contact.id, contact.isConnected));
                }

                @Override
                public void onIntroduce(SimpleIndexAdapter.Contact contact) {
                    System.out.println(":::::::::: onIntroduce  " + contact.name);
                    webView.loadUrl(String.format("javascript:onIntroduce(%s, %s)", contact.id, contact.isConnected));
                }

                @Override
                public void onItemClick(SimpleIndexAdapter.Contact contact) {
                    System.out.println(":::::::::: onItemClick  " + contact.name);
                    webView.loadUrl(String.format("javascript:onItemClick(%s, %s)", contact.id, contact.isConnected));
                }
            });

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(sa);
                }
            });

            callbackContext.success();
            return true;
        }

        callbackContext.error("Invalid action");
        return false;
    }

    public void sort(List<SimpleIndexAdapter.Contact> l) {
        for (int sz = l.size(), i = 1; i < sz; i++) {
            int j = i;
            while (j > 0) {
                SimpleIndexAdapter.Contact prev = l.get(j - 1);
                SimpleIndexAdapter.Contact thisOne = l.get( j );
                if (compare(prev, thisOne)) {
                    l.set(j - 1, thisOne);
                    l.set(j, prev);
                } else {
                    break;
                }
                j--;
            }
        }
    }

    private boolean compare(SimpleIndexAdapter.Contact contact1, SimpleIndexAdapter.Contact contact2) {
        String name1 = contact1.lastName.toUpperCase();
        String name2 = contact2.lastName.toUpperCase();

        boolean isCh1 = Character.isLetter(name1.charAt(0));
        boolean isCh2 = Character.isLetter(name2.charAt(0));

       return isCh1 && isCh2 ? name1.compareTo(name2) > 0 : !isCh1;
    }

    private LinearLayoutSoftKeyboardDetect getParentView() {
        return (LinearLayoutSoftKeyboardDetect) webView.getParent();
    }


}

