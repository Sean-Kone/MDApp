package gsi.com.mdapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public abstract class BaseActivity extends AppCompatActivity {

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public void showAlert(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        Resources res = getResources();
        View view = inflater.inflate(R.layout.alert_dialog_with_one_button, null);
        TextView textView = (TextView) view.findViewById(R.id.alert_dialog_message1);
        textView.setText(message);
        Button positiveButton = (Button) view.findViewById(R.id.positive_button1);
        positiveButton.setText(res.getString(R.string.ok_button_text));
        builder.setView(view);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    /**
     * Permission checking methods
     */
    public boolean checkPermission(String permissions[], int resultCode) {
        int permsOk = 0;
        int length = permissions.length;
        for (int i = 0; i < length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                permsOk++;
            }
        }
        if (permsOk == length) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, permissions, resultCode);
            return false;
        }
    }

    public void showPermissionRationaleDialog(final String message, final Runnable positiveButtonTask, final Runnable negativeButtonTask) {
        View view = getLayoutInflater().inflate(R.layout.alert_dialog_with_two_buttons, null);
        TextView textView = (TextView) view.findViewById(R.id.alert_dialog_message2);
        textView.setText(message);
        Resources res = getResources();
        Button negBtn = (Button) view.findViewById(R.id.negative_button2);
        negBtn.setText(res.getString(R.string.deny_button_text));
        Button posBtn = (Button) view.findViewById(R.id.positive_button2);
        posBtn.setText(res.getString(R.string.allow_button_text));
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(view)
                .create();

        posBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (positiveButtonTask != null) {
                    positiveButtonTask.run();
                }
            }
        });

        negBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (negativeButtonTask != null) {
                    negativeButtonTask.run();
                }

            }
        });
        alertDialog.show();
    }

    public boolean checkPlayServices(boolean isShowDialog) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (isShowDialog) {
                if (apiAvailability.isUserResolvableError(resultCode)) {
                    apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                            .show();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = LayoutInflater.from(this);
                    Resources res = getResources();
                    View view = inflater.inflate(R.layout.alert_dialog_with_one_button, null);
                    TextView textView = (TextView) view.findViewById(R.id.alert_dialog_message1);
                    textView.setText(res.getString(R.string.play_services_not_supported));
                    Button positiveButton = (Button) view.findViewById(R.id.positive_button1);
                    positiveButton.setText(res.getString(R.string.ok_button_text));
                    builder.setView(view);
                    builder.setCancelable(false);
                    final AlertDialog dialog = builder.create();
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
            return false;
        }
        return true;
    }

}
