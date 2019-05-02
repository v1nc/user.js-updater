package de.reckendrees.systems.userjsupdater;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
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
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView textLastUpdate;
    DownloadManager manager;
    RadioButton radioButton1, radioButton2, radioButton3, radioButton4;
    EditText editText;
    Button button, button2;
    ImageView imageView;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Bundle extras = intent.getExtras();
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = manager.query(q);
            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    String filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    filename = filename.substring(7);
                    final String packageName = prefs.getString("package_name",getResources().getString(R.string.default_package_name));
                    killAppBypackage(packageName);
                    String profile = getProfileString(packageName);
                    Boolean error = profile.equals("error");
                    String user = getUserString(packageName,profile);
                    Log.e("[User String]", user);

                    if(!error){
                        error = user.equals("error");
                    }
                    if(!error){
                        error = moveUserJSFile(filename,packageName,profile,user);
                    }
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
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
                    alertDialog.show();
                }
            }


        }
    };
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
                    SharedPreferences.Editor e = getPrefs.edit();
                    e.putBoolean("firstStart", false);
                    e.apply();
                }
            }
        });
        t.start();
    }
    private void killAppBypackage(String packageTokill){

        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = getPackageManager();
        //get a list of installed apps.
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
            stdin.write(("ls -l /data/data/"+packageName+"/files/mozilla/"+profile+"/prefs.js\n").getBytes());
            //stdin.write(("id\n").getBytes());
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
            br =
                    new BufferedReader(new InputStreamReader(stderr));
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
            br =
                    new BufferedReader(new InputStreamReader(stderr));
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
    private boolean moveUserJSFile(String filename,String packageName, String profile, String user){
        try {
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write(("rm /data/data/"+packageName+"/files/mozilla/"+profile+"/user.js\n").getBytes());
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
            br =
                    new BufferedReader(new InputStreamReader(stderr));
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        radioButton1 = findViewById(R.id.radioButton);
        radioButton2 = findViewById(R.id.radioButton2);
        radioButton3 = findViewById(R.id.radioButton3);
        radioButton4 = findViewById(R.id.radioButton4);
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        imageView = findViewById(R.id.imageView);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        showIntro();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = getResources().getString(R.string.user_url1);
                if(radioButton2.isChecked()){
                    url = getResources().getString(R.string.user_url2);
                }else if(radioButton3.isChecked()){
                    url = getResources().getString(R.string.user_url3);
                }else if(radioButton4.isChecked()){
                    url = editText.getText().toString();
                }
                try{
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setDescription("User.js Update");
                    request.setTitle("User.js Update");
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloaded_user.js");
                    manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    registerReceiver(onComplete, new IntentFilter(manager.ACTION_DOWNLOAD_COMPLETE));
                    manager.enqueue(request);
                }catch(Exception e){
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle(getResources().getString(R.string.dialog_error_title));
                    alertDialog.setMessage(getResources().getString(R.string.dialog_error_file));
                    alertDialog.setIcon(R.drawable.ic_done_black_24dp);
                    alertDialog.show();
                }

            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Title");
                final EditText input = new EditText(MainActivity.this);

                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        editor.putString("package_name",input.getText().toString());
                        editor.commit();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(
                        MainActivity.this).create();
                alertDialog.setTitle(getResources().getString(R.string.dialog_head));
                alertDialog.setMessage(getResources().getString(R.string.dialog_text));
                alertDialog.setIcon(R.drawable.ic_help_black_24dp);
                alertDialog.setButton(getResources().getString(R.string.dialog_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(getResources().getString(R.string.telegram_url)));
                        startActivity(i);
                    }
                });
                alertDialog.show();
            }
        });
    }
}
