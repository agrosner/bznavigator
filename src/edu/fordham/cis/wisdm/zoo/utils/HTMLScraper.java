package edu.fordham.cis.wisdm.zoo.utils;

import java.io.IOException;

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

import edu.fordham.cis.wisdm.zoo.main.R;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Provides handy utils for scraping html code from the zoos web pages
 * @author Andrew Grosner
 *
 */
public class HTMLScraper {

	/**
	 * Loads information content about a specific exhibit into a linearlayout
	 * @param layout
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public void getInfoContent(Context con, LinearLayout layout, String link) throws ClientProtocolException, IOException{
		new DownloadHtmlTask(con, link, layout).execute();
	}
	
	public class DownloadHtmlTask extends AsyncTask<Void, Void, Void>{

		private String mLink;
		
		private String mHtml = "";
		
		private LinearLayout mLayout;
		
		private Context mCtx;
		
		private ProgressBar mBar;
		
		public DownloadHtmlTask(Context con, String link, LinearLayout layout){
			mLink = link;
			mLayout = layout;
			mCtx = con;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			mBar = new ProgressBar(mCtx);
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
			for(Element e: el){
				if(Tag.valueOf("h2").equals(e.tag()) && !e.text().equals("")){
					
					TextView header = new TextView(mCtx);
					header.setText(e.text());
					header.setTextSize(25);
					header.setTextColor(mCtx.getResources().getColor(R.color.forestgreen));
					mLayout.addView(header);
					
				} else if(Tag.valueOf("p").equals(e.tag()) && !e.text().equals("")){
					TextView para = new TextView(mCtx);
					para.setText(Html.fromHtml(e.html()));
					para.setTextSize(15);
					para.setMovementMethod(LinkMovementMethod.getInstance());
					para.setLinksClickable(true);
					mLayout.addView(para);
				} else if(Tag.valueOf("img").equals(e.tag())){
					
				}
			}
			mLayout.removeView(mBar);
		}
	}
	
}
