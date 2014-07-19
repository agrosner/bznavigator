package com.grosner.zoo.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.grosner.zoo.R;
import com.grosner.zoo.application.ZooApplication;

/**
 * Provides handy utils for scraping html code from the zoos web pages
 * @author Andrew Grosner
 *
 */
public class HTMLScraper {

    public interface DownloadListener{
        public void onDownloadComplete();
    }

	/**
	 * Loads information content about a specific exhibit into a linearlayout
	 * @param layout
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public void getInfoContent(LinearLayout layout, String link, DownloadListener listener) throws IOException{
        if(StringUtils.stringNotNullOrEmpty(link)) {
            new DownloadHtmlTask(link, layout, listener).execute();
        }
	}
	
	public class DownloadHtmlTask extends AsyncTask<Void, Void, Void>{

		private String mLink;
		
		private String mHtml = "";
		
		private LinearLayout mLayout;
		
		private ProgressBar mBar;

        private DownloadListener mListener;
		
		public DownloadHtmlTask(String link, LinearLayout layout, DownloadListener listener){
			mLink = link;
			mLayout = layout;
            mListener = listener;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			mBar = new ProgressBar(ZooApplication.getContext());
			mBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			mLayout.addView(mBar);
			
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
		
		@Override
		public void onPostExecute(Void result){
			Document doc = Jsoup.parse(mHtml);
			Elements el = doc.getElementsByClass("main-content").first().children();
            ArrayList<String> images = new ArrayList<>();
			for(Element e: el){
				if(Tag.valueOf("h2").equals(e.tag()) && !e.text().equals("")){
					
					TextView header = new TextView(mLayout.getContext());
					header.setText(e.text());
					header.setTextSize(25);
					header.setTextColor(Color.BLACK);
					mLayout.addView(header);
					
				} else if(Tag.valueOf("p").equals(e.tag()) && !e.text().equals("")){
                    TextView para = getParagraphText(ZooApplication.getContext());
                    para.setText(Html.fromHtml(e.html()));
                    mLayout.addView(para);
				} else if(Tag.valueOf("img").equals(e.tag())){
					images.add(e.html());
				}
			}
			mLayout.removeView(mBar);

            if(mListener!=null){
                mListener.onDownloadComplete();
            }
		}
	}

    public static TextView getParagraphText(Context context){
        TextView para = new TextView(context);
        para.setTextSize(15);
        para.setTextColor(context.getResources().getColor(android.R.color.white));
        para.setMovementMethod(LinkMovementMethod.getInstance());
        para.setLinksClickable(true);
        return para;
    }
}
