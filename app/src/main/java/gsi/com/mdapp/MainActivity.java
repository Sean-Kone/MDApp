package gsi.com.mdapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends BaseActivity implements MainFragment.OnMainFragmentListener {

    private static final String FRAGMENT_TAG_MAIN = "MainFragment";
    private static final int CONTACT_PICKER_RESULT = 1001;
    private static final int RESULT_OK = -1;
    public static final int MD_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 5001;
    public static final int MD_PERMISSION_REQUEST_READ_CONTACTS = 5002;
    public static final int MD_PERMISSION_REQUEST_CALL_PHONE = 5003;
    private static final String BTN_CFG_URL = "https://api.jsonbin.io/b/5b91335d3ffac56f4bdac489/latest";

    private String mChosenNumber;
    private FrameLayout mBlockingProgressBar;

    private HashMap<String, ButtonAction.ActionInfo> mActionsOccurMap = new HashMap<>();
    private ArrayList<ButtonAction> mActions = new ArrayList<>();
    private int mCurrBtnActionIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBlockingProgressBar = (FrameLayout) findViewById(R.id.am_blocking_progress_bar);
        getSupportFragmentManager().beginTransaction().add(R.id.am_fl_root, MainFragment.newInstance(), FRAGMENT_TAG_MAIN).commit();
        getBtnCfg(BTN_CFG_URL);
    }

    void parseJson(JSONObject jsonObject) {
        try {
            JSONArray array = jsonObject.getJSONArray("buttonActions");

            JSONObject btnCfg = jsonObject.getJSONObject("buttonConfiguration");
            String title = btnCfg.getString("buttonTitle");
            String color = btnCfg.getString("buttonColor");
            final ButtonConfiguration cfg = new ButtonConfiguration(color, title);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainFragment fragment = getMainFragment();
                    if (fragment != null) {
                        fragment.updateButton(cfg);
                    }
                    mBlockingProgressBar.setVisibility(View.GONE);
                }
            });

            int size = array.length();
            if (size > 0) {
                JSONObject actionJson;
                ButtonAction action;
                int priority;
                boolean isEnabled;
                float aip;
                String type = "";
                for (int i = 0; i < size; i++) {
                    actionJson = array.getJSONObject(i);
                    priority = actionJson.getInt("priority");
                    isEnabled = actionJson.getBoolean("enabled");
                    type = actionJson.getString("type");

                    if (isEnabled) {
                        action = new ButtonAction(isEnabled, priority, type);
                        mActions.add(action);
                    }

                    aip = isEnabled ? (float) priority : ButtonAction.ACTION_PRIORITY_WEIGHT_DISABLED*priority;
                    if (mActionsOccurMap.containsKey(type)) {
                        ButtonAction.ActionInfo info = mActionsOccurMap.get(type);
                        int oc = info.getOccurances();
                        float sum = info.getPrioritySum();
                        info.setOccurances(oc + 1);
                        info.setPrioritySum(sum + aip);
                        mActionsOccurMap.put(type, info);
                    } else {
                        mActionsOccurMap.put(type, new ButtonAction.ActionInfo(1, aip));
                    }
                }
                sortActionsByPriority();
            }
        } catch (JSONException e) {
            MDALogger.logStackTrace(e);
        }
    }


    /**
     * Equal priorities will be sorted by the average priority of the action occurances
     *
     * Actions which are disabled are given a lessened weight to their priority
     */
    void sortActionsByPriority() {
        Collections.sort(mActions, new Comparator<ButtonAction>() {
            public int compare(ButtonAction o1, ButtonAction o2) {

                Integer op1 = o1.getPriority();
                Integer op2 = o2.getPriority();
                int c;
                c = op1.compareTo(op2);
                if (c == 0) {
                    ButtonAction.ActionInfo ai1 = mActionsOccurMap.get(o1.getType());
                    Float pAvg1 = ai1.getAveragePriority();
                    ButtonAction.ActionInfo ai2 = mActionsOccurMap.get(o2.getType());
                    Float pAvg2 = ai2.getAveragePriority();

                    c = pAvg1.compareTo(pAvg2);
                }
                return c;
            }
        });
    }


    void getBtnCfg(String url) {
        new ApiManager().getUrl(url, new ApiManager.UrlResponse() {
            @Override
            public void onResponse(boolean response, String content) {
                if (response) {
                    try {
                        JSONObject jsonObject = new JSONObject(content);
                        parseJson(jsonObject);
                    } catch (JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showAlert(getString(R.string.error_actions_json_msg));
                            }
                        });
                    }

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAlert(getString(R.string.error_actions_json_msg));
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MDANotificationManager.cancelAll(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            triggerBackgroundNotification(false);
        }
    }

    /**
     * Button action methods
     */
    private boolean getLocationAttempt() {
        boolean result = false;
        if (MDALocationManager.isLocationEnabled(this)) {
            if (checkPlayServices(true)) {
                if (checkPermission(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MD_PERMISSION_REQUEST_ACCESS_FINE_LOCATION)) {
                    getLocation();
                }
                result = true;
            }
        }
        return result;
    }

    protected void getLocation() {
        final MDALocationManager locMgr = new MDALocationManager(this, true);
        locMgr.setOnLocationUpdateListener(new MDALocationManager.OnLocationUpdateListener() {
            @Override
            public void onLocationUpdate(Location location) {}

            @Override
            public void onLocationAdrsUpdate(Location location, String adrs) {
                showAlert(getString(R.string.current_adrs_msg) + ": " + adrs + ".\n\n" + getString(R.string.current_pos_msg) + ": " + location.getLatitude() + " , " + location.getLongitude());

            }

            @Override
            public void onLocationUpdateError(String error) {
                showAlert(getString(R.string.error_loc_msg) + ": " + error);
            }

            @Override
            public void onLocationUpdatesStopped() {}
        });
        locMgr.build();
        locMgr.connect();
    }

    private void attemptCall(String phoneNumber) {
        if (checkPermission(new String[]{Manifest.permission.CALL_PHONE}, MD_PERMISSION_REQUEST_CALL_PHONE)) {
            makeCall(phoneNumber);
        }
    }

    private void makeCall(String phoneNumber) {
        Intent i = new Intent(Intent.ACTION_DIAL);
        String p = "tel:" + phoneNumber;
        i.setData(Uri.parse(p));
        startActivity(i);
    }


    void callContact() {
        if (checkPermission(new String[]{Manifest.permission.READ_CONTACTS}, MD_PERMISSION_REQUEST_READ_CONTACTS)) {
            openContactList();
        }
    }

    private void openContactList() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    void triggerBackgroundNotification(boolean isNotif) {
        MDAApp app = (MDAApp) getApplication();
        app.setBgNotif(isNotif);
        if (isNotif) {
            Toast.makeText(this, getString(R.string.bg_notif_msg), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:
                    // handle contact result
                    Cursor cursor = null;
                    try {
                        String phoneNo = null ;
                        String name = null;
                        Uri uri = data.getData();
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        phoneNo = cursor.getString(phoneIndex);
                        mChosenNumber = phoneNo;
                        attemptCall(phoneNo);
                    } catch (Exception e) {
                        MDALogger.logStackTrace(e);
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MD_PERMISSION_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showPermissionRationaleDialog(
                                getString(R.string.perm_rationale_location), new Runnable() {
                                    @Override
                                    public void run() {
                                        ActivityCompat.requestPermissions(
                                                MainActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                MD_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
                                    }
                                }, null);
                    } else {

                    }
                }
                break;
            }
            case MD_PERMISSION_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openContactList();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                        showPermissionRationaleDialog(
                                getString(R.string.perm_rationale_contacts), new Runnable() {
                                    @Override
                                    public void run() {
                                        ActivityCompat.requestPermissions(
                                                MainActivity.this,
                                                new String[]{Manifest.permission.READ_CONTACTS},
                                                MD_PERMISSION_REQUEST_READ_CONTACTS);
                                    }
                                }, null);
                    }
                }
                break;
            }
            case MD_PERMISSION_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall(mChosenNumber);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                        showPermissionRationaleDialog(
                                getString(R.string.perm_rationale_phone_call), new Runnable() {
                                    @Override
                                    public void run() {
                                        ActivityCompat.requestPermissions(
                                                MainActivity.this,
                                                new String[]{Manifest.permission.CALL_PHONE},
                                                MD_PERMISSION_REQUEST_CALL_PHONE);
                                    }
                                }, null);
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    /**
     * Fragments getters and methods
     */
    MainFragment getMainFragment() {
        MainFragment fragment = null;
        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAIN) != null) {
            fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAIN);
        }
        return fragment;
    }

    /**
     * {@link MainFragment.OnMainFragmentListener} interface methods
     */
    @Override
    public void onPerformAction() {
        if (mCurrBtnActionIdx < mActions.size()) {
            ButtonAction action = mActions.get(mCurrBtnActionIdx);
            mCurrBtnActionIdx++;

            boolean isEnabled = action.isIsEnabled();
            if (isEnabled) {
                @MDA.ActionType String type = action.getType();
                switch (type) {
                    case MDA.ACTION_TYPE_ANIMATION: {
                        MainFragment fragment = getMainFragment();
                        if (fragment != null) {
                            fragment.animateButton();
                        }
                        break;
                    }
                    case MDA.ACTION_TYPE_CALL: {
                        callContact();
                        break;
                    }
                    case MDA.ACTION_TYPE_LOCATION: {
                        getLocationAttempt();
                        break;
                    }
                    case MDA.ACTION_TYPE_NOTIFICATION: {
                        triggerBackgroundNotification(true);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        } else {
            showAlert(getString(R.string.no_actions_msg));
        }
    }

    JSONObject mockCfgObj() throws JSONException {


        JSONObject typeLocJson = new JSONObject();
        typeLocJson.put("priority", 2);
        typeLocJson.put("enabled", true);
        typeLocJson.put("type", "location");

        JSONObject typeLocJson1 = new JSONObject();
        typeLocJson1.put("priority", 1);
        typeLocJson1.put("enabled", true);
        typeLocJson1.put("type", "location");


        JSONObject typeNotifJson = new JSONObject();
        typeNotifJson.put("priority", 3);
        typeNotifJson.put("enabled", true);
        typeNotifJson.put("type", "notification");

        JSONObject typeCallJson = new JSONObject();
        typeCallJson.put("priority", 2);
        typeCallJson.put("enabled", true);
        typeCallJson.put("type", "call");


        JSONObject typeCallJson2 = new JSONObject();
        typeCallJson2.put("priority", 2);
        typeCallJson2.put("enabled", true);
        typeCallJson2.put("type", "call");


        JSONObject typeAnimJson = new JSONObject();
        typeAnimJson.put("priority", 4);
        typeAnimJson.put("enabled", true);
        typeAnimJson.put("type", "animation");

        JSONObject typeAnimJson2 = new JSONObject();
        typeAnimJson2.put("priority", 4);
        typeAnimJson2.put("enabled", false);
        typeAnimJson2.put("type", "animation");

        JSONArray buttonActions = new JSONArray();
        buttonActions.put(typeLocJson);
        buttonActions.put(typeAnimJson);
        buttonActions.put(typeCallJson);
        buttonActions.put(typeNotifJson);
        buttonActions.put(typeCallJson2);


//        buttonActions.put(typeAnimJson2);
//        buttonActions.put(typeAnimJson2);
//        buttonActions.put(typeLocJson);
//        buttonActions.put(typeLocJson);
//        buttonActions.put(typeCallJson);
//        buttonActions.put(typeNotifJson);
//        buttonActions.put(typeAnimJson2);
//        buttonActions.put(typeAnimJson);
//        buttonActions.put(typeAnimJson);
//        buttonActions.put(typeCallJson);
//        buttonActions.put(typeNotifJson);
//        buttonActions.put(typeAnimJson2);
//        buttonActions.put(typeLocJson);
//        buttonActions.put(typeAnimJson);
//        buttonActions.put(typeAnimJson);

        JSONObject buttonConfiguration = new JSONObject();
        buttonConfiguration.put("buttonTitle", "I am a button");
        buttonConfiguration.put("buttonColor", "#d72525");


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("buttonActions", buttonActions);
        jsonObject.put("buttonConfiguration", buttonConfiguration);


        return jsonObject;
    }

    void mockUrlRequest(final ApiManager.MockUrlResponse response) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    try {
                        response.onResponse(true, mockCfgObj());
                    } catch (JSONException e) {
                        MDALogger.logStackTrace(e);
                    }
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    MDALogger.logStackTrace(e);
                }

            }
        }).start();
    }
}
