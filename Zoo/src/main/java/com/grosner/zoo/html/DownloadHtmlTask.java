package com.grosner.zoo.html;

import android.os.AsyncTask;
import android.widget.LinearLayout;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by: andrewgrosner
 * Date: 8/10/14.
 * Contributors: {}
 * Description:
 */
public abstract class DownloadHtmlTask extends AsyncTask<Void, Void, Void> {

    String mLink;

    String mHtml = "";

    private LinearLayout mLayout;

    private DownloadListener mListener;

    public DownloadHtmlTask(String link, LinearLayout layout, DownloadListener listener) {
        mLink = link;
        mLayout = layout;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(mLink);

        HttpResponse response;
        try {
            response = httpClient.execute(get);
            mHtml = EntityUtils.toString(response.getEntity());
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    protected void onDownloadComplete(Object object){
        if (mListener != null) {
            mListener.onDownloadComplete(object);
        }
    }

    protected void onDownloadFailed(){
        if (mListener != null) {
            mListener.onDownloadFailed();
        }
    }
}

