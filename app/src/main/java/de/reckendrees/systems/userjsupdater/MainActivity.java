package de.reckendrees.systems.userjsupdater;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DownloadInterface {
    DownloadManager manager;
    RadioButton radioButton1, radioButton2, radioButton3, radioButton4, radioButton5;
    ProgressBar progressBar;
    EditText editText;
    Button button;
    SharedPreferences prefs;
    Map<String, String> packageNameMap = new HashMap<String, String>();
    final CharSequence browserList[] = {"Fennec F-Droid", "Firefox Preview", "Firefox Preview Nightly","IceCat", "Firefox", "Orfox" };
    SharedPreferences.Editor editor;
    BroadcastReceiver onComplete;

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_browser:
                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this,R.style.AppAlertDialog);
                initDialog(ad, getResources().getString(R.string.dialog_browser_combo_title));
                ad.setIcon(R.drawable.ic_settings_black_24dp);
                ad.setSingleChoiceItems(browserList, prefs.getInt("package_combo",0),  new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        editor.putString("package_name",packageNameMap.get(browserList[arg1].toString()));
                        editor.putInt("package_combo", arg1);
                        editor.commit();
                        arg0.cancel();

                    }
                });
                ad.show();
                return true;

            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context me = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        radioButton1 = findViewById(R.id.radioButton);
        radioButton2 = findViewById(R.id.radioButton2);
        radioButton3 = findViewById(R.id.radioButton3);
        radioButton4 = findViewById(R.id.radioButton4);
        radioButton5 = findViewById(R.id.radioButton5);
        progressBar = findViewById(R.id.progressBar);
        final RadioGroup radioGroup = findViewById(R.id.radioGroup);
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        initPackgeNameMap();
        editor = prefs.edit();
        onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                Bundle extras = intent.getExtras();
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
                Cursor c = manager.query(q);
                if (c.moveToFirst()) {
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        String filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        handleDownload(filename.substring(7));
                    }
                }


            }
        };
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.colorText));
        myToolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        showIntro();
        String custom_url = prefs.getString("custom_url","error");
        if(!custom_url.equals("error")){
            editText.setText(custom_url);
        }
        switch(prefs.getInt("user.js_combo",0)){
            case 0:
                radioButton1.toggle();
                break;
            case 1:
                radioButton2.toggle();
                break;
            case 2:
                radioButton3.toggle();
                break;
            case 3:
                radioButton5.toggle();
                break;
            case 4:
                radioButton4.toggle();
                break;
        }
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        try {
            if(data.toString().length() > 0){
                String url = getResources().getString(R.string.user_url420);
                switch (prefs.getInt("user.js_combo",0)){
                    case 1:
                        url = getResources().getString(R.string.user_url2);
                        break;
                    case 2:
                        url = getResources().getString(R.string.user_url3);
                        break;
                    case 3:
                        url = getResources().getString(R.string.user_url4);
                        break;
                    case 0:
                        url = getResources().getString(R.string.user_url1);
                        break;
                    case 4:
                        url = prefs.getString("custom_url",url);
                        break;
                }
                downloadUserJS(url);
            }
        }catch(NullPointerException e){
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = getResources().getString(R.string.user_url420);
                editor.putInt("user.js_combo",420);
                if(radioButton1.isChecked()){
                    url = getResources().getString(R.string.user_url1);
                    editor.putInt("user.js_combo",0);
                }
                else if(radioButton2.isChecked()){
                    url = getResources().getString(R.string.user_url2);
                    editor.putInt("user.js_combo",1);
                }else if(radioButton3.isChecked()){
                    url = getResources().getString(R.string.user_url3);
                    editor.putInt("user.js_combo",2);
                } else if(radioButton5.isChecked()){
                    url = getResources().getString(R.string.user_url4);
                    editor.putInt("user.js_combo",3);
                }
                else if(radioButton4.isChecked()){
                    url = editText.getText().toString();
                    editor.putString("custom_url",url);
                    editor.putInt("user.js_combo",4);

                }
                editor.commit();
                downloadUserJS(url);

            }
        });
    }

    private void showIntro(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);
                if (isFirstStart) {
                    final Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            startActivity(i);
                        }
                    });
                }
            }
        });
        t.start();
    }

    private void downloadUserJS(final String url){
        toggleBar(true);
        int timeOut;
        try{
            timeOut =  Integer.parseInt(prefs.getString("download_timeout","10"))*1000;
        }catch(Exception e){
            timeOut = 10000;
        }
        Log.e("TImeout",""+timeOut);
        Handler handler = new Handler();
        Context me = this;
        DownloadInterface me2 = this;
        try{
            if(prefs.getBoolean("downloader",true)){
                final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDescription("User.js Update");
                request.setTitle("User.js Update");
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloaded_user.js");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                registerReceiver(onComplete, new IntentFilter(manager.ACTION_DOWNLOAD_COMPLETE));
                manager.enqueue(request);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("ERRO","timeouted");
                        DownloadManager.Query query = null;
                        Cursor c = null;
                        DownloadManager downloadManager = null;
                        downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                        query = new DownloadManager.Query();
                        if(query!=null) {
                            query.setFilterByStatus(DownloadManager.STATUS_FAILED|DownloadManager.STATUS_PAUSED|DownloadManager.STATUS_SUCCESSFUL|
                                    DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PENDING);
                        } else {
                            return;
                        }
                        c = downloadManager.query(query);
                        if(c.moveToFirst()) {
                            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                            if(status != DownloadManager.STATUS_SUCCESSFUL){
                                unregisterReceiver(onComplete);
                                showErrorDialog(R.string.dialog_error_timeout);
                            }
                        }
                    }
                }, timeOut);
            }else{
                final DownloadTask downloadTask = new DownloadTask(me,me2);
                downloadTask.execute(url,Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/downloaded_user.js" );
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("ERRO","timeouted");
                        if(downloadTask.getStatus() != AsyncTask.Status.FINISHED){
                            downloadTask.cancel(true);
                            showErrorDialog(R.string.dialog_error_timeout);
                        }
                    }
                }, timeOut);
            }



        }catch(Exception e){
            e.printStackTrace();
            showErrorDialog(R.string.dialog_error_file);
        }
    }

    private boolean moveUserJSFile(String filename, String packageName, String profile, String user, boolean purgePrefs){
        try {
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write(("rm /data/data/"+packageName+"/files/mozilla/"+profile+"/user.js\n").getBytes());
            if(purgePrefs){
                stdin.write(("rm /data/data/"+packageName+"/files/mozilla/"+profile+"/prefs.js\n").getBytes());
            }
            stdin.write(("cp "+filename+" /data/data/"+packageName+"/files/mozilla/"+profile+"/user.js\n").getBytes());
            stdin.write(("chown "+user+":"+user+" /data/data/"+packageName+"/files/mozilla/"+profile+"/user.js\n").getBytes());
            stdin.write(("chmod 600 /data/data/"+packageName+"/files/mozilla/"+profile+"/user.js\n").getBytes());
            stdin.write(("rm "+filename+"\n").getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                Log.d("[Output]", line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("[Error]", line);
            }
            br.close();
            process.waitFor();
            process.destroy();

        } catch (Exception ex) {
            Log.e("[Error]", "No root");
        }
        return false;
    }

    private void handleDownload(String filename){
        final String packageName = prefs.getString("package_name",getResources().getString(R.string.default_package_name));
        if(prefs.getBoolean("restart_browser",true)){
            killAppBypackage(packageName);
        }
        String profile = getProfileString(packageName);
        Boolean error = profile.equals("error");
        String user = getUserString(packageName,profile);
        Log.e("[User String]", user);

        if(!error){
            error = user.equals("error");
        }
        if(!error){
            boolean purgePrefs = prefs.getBoolean("clear",true);
            error = moveUserJSFile(filename,packageName,profile,user,purgePrefs);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AppAlertDialog);
        AlertDialog alertDialog = builder.create();
        if(!error){
            alertDialog.setTitle(getResources().getString(R.string.dialog_done_title));
            alertDialog.setMessage(getResources().getString(R.string.dialog_done_text));
            alertDialog.setIcon(R.drawable.ic_done_black_24dp);
            alertDialog.setButton(getResources().getString(R.string.dialog_done_button), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent browserIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    startActivity(browserIntent);
                }
            });

        }else{
            alertDialog.setTitle(getResources().getString(R.string.dialog_error_title));
            alertDialog.setMessage(getResources().getString(R.string.dialog_error_su));
            alertDialog.setIcon(R.drawable.ic_error_black_24dp);
        }
        toggleBar(false);
        alertDialog.show();
    }

    @Override
    public void onDownloadFailure(Exception e) {
        showErrorDialog(R.string.dialog_error_download);
    }

    @Override
    public void onDownloadSuccess(Object object) {
        String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/downloaded_user.js";
        handleDownload(filename);
    }

    private void initDialog(AlertDialog.Builder alertDialogBuilder, String title){
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.colorAccent));
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(title);
        ssBuilder.setSpan(
                foregroundColorSpan,
                0,
                title.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        alertDialogBuilder.setTitle(ssBuilder);
    }

    private void initPackgeNameMap(){
        packageNameMap.put("Fennec F-Droid", "org.mozilla.fennec_fdroid");
        packageNameMap.put("Firefox Preview", "org.mozilla.fenix");
        packageNameMap.put("Firefox Preview Nightly","org.mozilla.fenix.nightly");
        packageNameMap.put("IceCat", "org.gnu.icecat");
        packageNameMap.put("Firefox", "org.mozilla.firefox");
        packageNameMap.put("Orfox", "info.guardianproject.orfox");
    }

    private void killAppBypackage(String packageTokill){
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = getPackageManager();
        packages = pm.getInstalledApplications(0);
        ActivityManager mActivityManager = (ActivityManager) MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        String myPackage = getApplicationContext().getPackageName();
        for (ApplicationInfo packageInfo : packages) {
            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM)==1) {
                continue;
            }
            if(packageInfo.packageName.equals(myPackage)) {
                continue;
            }
            if(packageInfo.packageName.equals(packageTokill)) {
                mActivityManager.killBackgroundProcesses(packageInfo.packageName);
            }
        }
    }

    private String getUserString(String packageName, String profile){
        try {
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write(("ls -l /data/data/"+packageName+"/files/mozilla/"+profile+"/addons.json\n").getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                Log.d("[Output]", line);
                String[] splited = line.split(" ");
                if(splited.length>2){
                    return splited[2];
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("[Error]", line);
            }
            br.close();
            process.waitFor();
            process.destroy();

        } catch (Exception ex) {
            Log.e("[Error]", ex.getMessage());
        }
        return "error";
    }

    private String getProfileString(String packageName){
        try {
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            stdin.write(("ls /data/data/"+packageName+"/files/mozilla/\n").getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                Log.d("[Output]", line);
                if(line.length()>8){
                    String tmp = line.substring(line.length()-8);
                    Log.d("[Output]", tmp);
                    if(tmp.equals(".default")){
                        return line;
                    }
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("[Error]", line);
            }
            br.close();
            process.waitFor();
            process.destroy();

        } catch (Exception ex) {
            Log.e("[Error]", ex.getMessage());
        }
        return "error";
    }

    private void showErrorDialog(int textId){
        toggleBar(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AppAlertDialog);
        initDialog(builder,getResources().getString(R.string.dialog_error_title));
        AlertDialog alertDialog = builder.create();
        alertDialog.setMessage(getResources().getString(textId));
        alertDialog.setIcon(R.drawable.ic_error_black_24dp);
        alertDialog.show();
    }

    public void toggleBar(boolean on){
        if(on){
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setVisibility(View.INVISIBLE);
        }

    }
}
