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

    private ListView listView;

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("addContactList")) {

            List<String> countries = new ArrayList<String>();

            countries.add("Italy");
            countries.add("Italy");
            countries.add("Italy");
            countries.add("Italy");
            countries.add("Italy");
            countries.add("Spain");
            countries.add("France");
            countries.add("Germany");
            countries.add("United Kingdom");
            countries.add("United Kingdom");
            countries.add("United Kingdom");
            countries.add("United Kingdom");
            countries.add("Austria");
            countries.add("Ireland");
            countries.add("Portugal");
            countries.add("Portugal");
            countries.add("Portugal");
            countries.add("Portugal");
            countries.add("Belgium");
            countries.add("Denmark");
            countries.add("Finland");
            countries.add("Finland");
            countries.add("Finland");
            countries.add("Finland");
            countries.add("Norway");
            countries.add("Sweden");
            countries.add("Netherlands");
            countries.add("Greece");
            countries.add("Greece");
            countries.add("Greece");
            countries.add("Greece");
            countries.add("Greece");
            countries.add("Greece");
            countries.add("Luxembourg");
            countries.add("Malta");
            countries.add("Malta");
            countries.add("Malta");
            countries.add("Malta");
            countries.add("Malta");
            countries.add("Malta");
            countries.add("Latvia");
            countries.add("Slovakia");
            countries.add("Slovenia");
            countries.add("Poland");
            countries.add("Hungary");
            countries.add("Romania");
            countries.add("Romania");
            countries.add("Romania");
            countries.add("Romania");



//            Collections.sort(countries);

            int height = args.getInt(0);

            listView = new ListView(cordova.getActivity());
            listView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

//            ArrayAdapter<String> sa = new ArrayAdapter<String>(cordova.getActivity(), android.R.layout.simple_list_item_1, countries);
            //listView.setAdapter(sa);

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
        } else if (action.equals("setContacts")) {
            System.out.println(":::::" + args.toString());
            JSONArray jsonContacts = args.getJSONArray(0);

            int len = jsonContacts.length();
            List<String> countries = new ArrayList<String>(len);

            for (int i = 0; i < len; i++) {
                countries.add(((JSONObject) jsonContacts.get(i)).getString("name"));
            }

            final ArrayAdapter<String> sa = new ArrayAdapter<String>(cordova.getActivity(), android.R.layout.simple_list_item_1, countries);

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(sa);
                }
            });

        }

        callbackContext.error("Invalid action");
        return false;
    }

    private LinearLayoutSoftKeyboardDetect getParentView() {
        return (LinearLayoutSoftKeyboardDetect) webView.getParent();
    }


}