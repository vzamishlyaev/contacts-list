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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactList extends CordovaPlugin {

    private FastSearchListView listView;
    private ImageLoaderManager loaderManager = new ImageLoaderManager(new Handler(), cordova.getActivity());

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("addContactList")) {

            int height = args.getInt(0);

            listView = new FastSearchListView(cordova.getActivity());
            listView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            //listView.setFastScrollEnabled(true);
            listView.setBackgroundResource(android.R.color.transparent);
            listView.setCacheColorHint(android.R.color.transparent);
            listView.setScrollingCacheEnabled(false);


            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getParentView().addView(listView, 1);
                }
            });

            callbackContext.success();
            return true;
        } else if (action.equals("hideContactList")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getParentView().removeView(listView);
                }
            });

            callbackContext.success();
            return true;
        } else if (action.equals("setContacts")) {

            JSONArray jsonContacts = args.getJSONArray(0);

            int len = jsonContacts.length();
            List<SimpleIndexAdapter.Contact> contacts = new ArrayList<SimpleIndexAdapter.Contact>();

            for (int i = 0; i < len; i++) {
                JSONObject object = (JSONObject) jsonContacts.get(i);
                contacts.add(new SimpleIndexAdapter.Contact(object.getInt("id"), object.getString("name"),
                        object.getString("lastName"), object.getString("photo"), object.getString("data")));
            }

            Collections.sort(contacts, SimpleIndexAdapter.Contact.lastNameComparator);

            final SimpleIndexAdapter sa = new SimpleIndexAdapter(contacts, cordova.getActivity(), loaderManager);

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

    private LinearLayoutSoftKeyboardDetect getParentView() {
        return (LinearLayoutSoftKeyboardDetect) webView.getParent();
    }


}
