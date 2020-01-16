package de.reckendrees.systems.userjsupdater;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Void, String> {

    private DownloadInterface<String> mCallBack;
    private Context mContext;
    public Exception mException;


    public DownloadTask(Context context, DownloadInterface callback) {
        mCallBack = callback;
        mContext = context;
    }

    @Override
    public String doInBackground(String... params) {
        try {
            URL u = new URL(params[0]);
            Log.e("THREAD",params[0]+"   "+params[1]);
            InputStream is = u.openStream();

            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[1024];
            int length;

            FileOutputStream fos = new FileOutputStream(new File(params[1]));
            while ((length = dis.read(buffer))>0) {
                fos.write(buffer, 0, length);
            }
        } catch (MalformedURLException e) {
            Log.e("download ERROR", "malformed url error", e);
            mException = e;
        } catch (IOException e) {
            Log.e("download ERROR", "io error", e);
            mException = e;
        } catch (SecurityException e) {
            Log.e("download ERROR", "security error", e);
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (mCallBack != null) {
            if (mException == null) {
                mCallBack.onDownloadSuccess(result);
            } else {
                mCallBack.onDownloadFailure(mException);
            }
        }
    }
}