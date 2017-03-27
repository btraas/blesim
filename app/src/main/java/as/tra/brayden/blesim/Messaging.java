package as.tra.brayden.blesim;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Brayden on 3/16/2017.
 */

public abstract class Messaging {

    private static final int DEFAULT_LENGTH = Snackbar.LENGTH_LONG;
    private static AlertDialog dialog;

    private static String LAST_MESSAGE = "";

    private static View getView(Activity a) {
        if(a == null) return null;

        View v = null;
        if(AppCompatActivity.class.isInstance(a)) {
            v = a.findViewById(android.R.id.content).getRootView();
        } else if(ListActivity.class.isInstance(a)) {
            v = ((ListActivity) a).getListView();
        }


        return a.findViewById(android.R.id.content).getRootView();
    }

    private static android.os.IBinder getBinder(Activity a) {
        if(a == null) return null;
        return getView(a).getWindowToken();
    }

    public static void hideInput(Activity activity) {
        if(activity == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getBinder(activity), 0);
    }

    public static class NoMessageException extends Exception {
        public NoMessageException(String message) {
            super(message);
        }
    }

    public static class SilentlyFailExceptionHandler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread th, Throwable ex) {
            ex.printStackTrace();
            th.interrupt();
        }
    };

    public static void handle(final Activity activity, JSONObject json) {
        try {
            handleOrThrow(activity, json);
        } catch (NoMessageException e) {

        }
    }

    public static void handleOrThrow(final Activity activity, JSONObject json) throws NoMessageException {
        if(activity == null) return;
        try {
            JSONObject data = json;


            if(data.has("alert")) {

                Messaging.OnClickListener dialogAction;
                if(data.has("link")) {

                    final String link = data.getString("link");
                    final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));

                    //activity, String msg, String buttonText, View.OnClickListener action) {
                    dialogAction = new Messaging.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.startActivity(browserIntent);
                        }

                        @Override
                        public void onClick(DialogInterface iface, int num) {
                            activity.startActivity(browserIntent);
                        }
                    };
                } else {
                    dialogAction = new Messaging.OnClickListener() {
                        @Override
                        public void onClick(View v) {}
                        @Override
                        public void onClick(DialogInterface iface, int num) { }
                    };
                }
                String buttonText = data.has("buttontext") ? data.getString("buttontext")
                        : (data.has("link") ? "Download" : "OK");
                if(data.has("description") || ( data.has("forcego") && data.getBoolean("forcego"))) {
                    String msg = data.has("description") ? data.getString("description")
                            : "App version "+BuildConfig.VERSION_NAME+" is too old";

                    if(dialog != null && dialog.isShowing()) return;
                    dialog = new AlertDialog.Builder(activity)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(data.getString("alert"))
                            .setMessage(msg)
                            .setPositiveButton(buttonText, dialogAction)
                            //.setNegativeButton("No", null)
                            .show();
                } else {
                    Messaging.showAction(activity, data.getString("alert"), buttonText, dialogAction);
                }

                //Messaging.showAction(activity, data.getString("alert"), "OK", action, Snackbar.LENGTH_INDEFINITE
                //Messaging.showAction(activity, data.getString(""))
                return;

            }
            if(data.has("error")) {
                Messaging.showError(activity, data.get("error").toString());
                return;
            } else if(data.has("success")) {
                Messaging.showSuccess(activity, data.get("success").toString());
                return;
            } else if(data.has("message")) {
                Messaging.showMessage(activity, data.get("message").toString());
                return;
            }
            throw new NoMessageException("No message found in "+data.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            showError(activity, "JSON parse exception...");
        }
    }


    public static void show(Activity activity, Snackbar snackbar, int color) {
        snackbar.getView().setBackgroundColor(color);
        show(activity, snackbar);
    }

    public static void show(Activity activity, Snackbar snackbar) {
        Messaging.LAST_MESSAGE = snackbar.toString();
        hideInput(activity);
        snackbar.show();
    }

    public static void showSuccess(Activity activity, String msg, int length) {

        // if(!msg.trim().substring(0,6).toLowerCase().equals("success")) msg = "Success: " + msg;
        if(activity == null) return;
        Snackbar snackbar = Snackbar.make(getView(activity), msg, length);
        show(activity, snackbar, ContextCompat.getColor(activity, R.color.primary_dark));
    }

    public static void showSuccess(Activity activity, String msg) {
        showSuccess(activity, msg, Snackbar.LENGTH_SHORT);
    }

    public static void showMessage(Activity activity, String msg, int length) {
        if(activity == null) return;
        Snackbar snackbar = Snackbar.make(getView(activity), msg, length);
        show(activity, snackbar);
    }
    public static void showMessage(Activity activity, String msg) {
        showMessage(activity, msg, Snackbar.LENGTH_SHORT);
    }

    public static void showError(Activity activity, String msg, int length) {

        // if(!msg.trim().substring(0,4).toLowerCase().equals("error")) msg = "Error: " + msg;
        if(activity == null) return;


        Snackbar snackbar = Snackbar.make(getView(activity), msg, length);
        show(activity, snackbar, ContextCompat.getColor(activity, android.R.color.holo_red_dark));
    }
    public static void showError(Activity activity, String msg) {
        showError(activity, msg, Snackbar.LENGTH_LONG);
    }



    public static void showAction(Activity activity, String msg, String buttonText, View.OnClickListener action, int length) {
        if(activity == null) return;


        Snackbar snackbar = Snackbar.make(getView(activity), msg, length)
                .setAction(buttonText, action);

        show(activity, snackbar);
    }

    public static void showAction(Activity activity, String msg, String buttonText, View.OnClickListener action) {
        showAction(activity, msg, buttonText, action, Snackbar.LENGTH_INDEFINITE);
    }


    public static void showAction(Activity activity, Snackbar snackbar, int color) {
        snackbar.getView().setBackgroundColor(color);
        showAction(activity, snackbar);
    }

    public static void showAction(Activity activity, Snackbar snackbar) {
        if(activity == null) return;
        LAST_MESSAGE = snackbar.toString();
        hideInput(activity);
        snackbar.show();
    }

    public static interface OnClickListener extends DialogInterface.OnClickListener, View.OnClickListener {

    }

}
