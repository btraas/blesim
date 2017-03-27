package as.tra.brayden.blesim;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Created by Brayden on 3/14/2017.
 */

public abstract class WebPostTask extends AsyncTask<String, String, Void> {

    private static String TAG = WebPostTask.class.getName();

    public static long LAST_FETCH = 0;

    //String url_select = getString(R.string.rest_url) + "m=manifest&app_key="+LoginBarcodeActivity.KEY;


    private ProgressDialog progressDialog;
    final JSONObject params;
    URL urlSelect;
    String version;
    final Activity activity;
    final Context ctx;

    boolean suppressErrors = false;
    String message = "Downloading your data...";
    String result = "";

    public WebPostTask(Activity a, JSONObject params) throws ActivityNotFoundException {
        if(a == null) throw new ActivityNotFoundException("Activity is null!");

        this.activity = a;
        this.ctx = a.getApplicationContext();
        this.params = params;
    }

    public WebPostTask(Activity a, JSONObject params, String message) {
        this(a, params);
        this.message = message;
    }


    protected void onPreExecute() {

        int urlResourceId = R.string.rest_url;
        String urlBase = activity.getResources().getString(urlResourceId);

        //Iterator i = params.iterator();
        try {
            params.put("app_package", BuildConfig.APPLICATION_ID);
            params.put("app_version", BuildConfig.VERSION_NAME);
            params.put("last_fetch", WebPostTask.LAST_FETCH);

            Iterator<String> columns = params.keys();
            while(columns.hasNext()) {
                String key = columns.next();
                urlBase += URLEncoder.encode(key,"UTF-8") + "=" + URLEncoder.encode(params.getString(key), "UTF-8");
                if(columns.hasNext()) urlBase += "&";
            }
            urlSelect = new URL(urlBase);
        } catch (JSONException e) {
            Messaging.showError(activity, "Invalid parameters!");
            Log.e(TAG, e.getMessage());

        } catch (MalformedURLException e) {
            Messaging.showError(activity, "Invalid URL");
            Log.e(TAG, e.getMessage());

        } catch (Exception e) {
            Messaging.showError(activity, e.getMessage());
            //throw e;
        }

        // null message indicates no UI shown, this is a background task.
        if(message != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    progressDialog = new ProgressDialog(activity);


                    progressDialog.setMessage(message);
                    progressDialog.show();
                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface arg0) {
                            WebPostTask.this.cancel(true);
                        }
                    });
                }
            });
        }

    }




    @Override
    protected Void doInBackground(String[] params) {

        InputStream inputStream;

        try {
            // Set up HTTP post
            Log.d(TAG, "Opening stream from "+urlSelect.toString());

            Messaging.hideInput(activity);
            inputStream = (urlSelect).openStream();
            java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
            result = s.hasNext() ? s.next() : "";

        } catch (FileNotFoundException e3) {
            Messaging.showError(activity, "No connection!");
            Log.e("FNFException", e3.toString());
            e3.printStackTrace();

        } catch (IOException e4) {
            Messaging.showError(activity, "No Connection!");
            Log.e("IOException", e4.toString());
            e4.printStackTrace();

        }
        catch (Exception e) {
            Messaging.showError(activity, "Download Failed");
            Log.e(TAG, "StringBuilding & BufferedReader " + "Error converting result " + e.toString());
            e.printStackTrace();
        }
        return null;
    } // protected Void doInBackground(String... params)

    protected void onPostExecute(Void v) {

        try {

            JSONObject data;
            try {
                data = (new JSONObject(result));
            } catch (JSONException e) {
                int end = result.length() > 40 ? 40 : result.length();
                Log.e(TAG, "Invalid JSON response: " + result.substring(0, end));
                if (!WebPostTask.this.suppressErrors)
                    Messaging.showError(activity, "Invalid server response");
                return;
            }

            try {
                if (data.has("last_fetch")) WebPostTask.LAST_FETCH = data.getLong("last_fetch");
            } catch (JSONException e) {
            }

            try {
                // do this first

                if (data.has("alert")) {

                    Messaging.OnClickListener dialogAction;
                    if (data.has("link")) {

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
                            public void onClick(View v) {
                            }

                            @Override
                            public void onClick(DialogInterface iface, int num) {
                            }
                        };
                    }
                    String buttonText = data.has("buttontext") ? data.getString("buttontext")
                            : (data.has("link") ? "Download" : "OK");
                    if (data.has("description") || (data.has("forcego") && data.getBoolean("forcego"))) {
                        String msg = data.has("description") ? data.getString("description")
                                : "App version " + BuildConfig.VERSION_NAME + " is too old";


                        new AlertDialog.Builder(activity)
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
                if (data.has("error")) {
                    Messaging.showError(activity, data.get("error").toString());
                    return;
                } else if (data.has("success")) {
                    Messaging.showSuccess(activity, data.get("success").toString());
                    return;
                } else if (data.has("message")) {
                    Messaging.showMessage(activity, data.get("message").toString());
                    return;
                }


            } catch (JSONException e) {
            }

            Log.d(TAG, "Recieved from server: " + result);

            callback(data);

        } finally {
            if(this.progressDialog != null) this.progressDialog.dismiss();
        }

    } // protected void onPostExecute(Void v)

    protected abstract void callback(JSONObject result);


    public void execute() {
        this.execute(new String[] {});
    }
} //class MyAsyncTask extends AsyncTask<String, String, Void>