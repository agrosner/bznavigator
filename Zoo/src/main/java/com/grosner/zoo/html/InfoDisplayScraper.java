package com.grosner.zoo.html;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import android.content.Context;
import android.os.AsyncTask;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grosner.zoo.database.content.ActivityObject;
import com.grosner.zoo.utils.StringUtils;

/**
 * Provides handy utils for scraping html code from the zoos web pages
 *
 * @author Andrew Grosner
 */
public class InfoDisplayScraper {

    /**
     * Loads information content about a specific exhibit into a linearlayout
     *
     * @param layout
     * @throws IOException
     * @throws ClientProtocolException
     */
    public void getInfoContent(LinearLayout layout, String link, DownloadListener listener) throws IOException {
        if (StringUtils.stringNotNullOrEmpty(link)) {
            new DownloadInfoDisplayTask(link, layout, listener).execute();
        }
    }

    public class DownloadInfoDisplayTask extends DownloadHtmlTask {

        public DownloadInfoDisplayTask(String link, LinearLayout layout, DownloadListener listener) {
            super(link, layout, listener);
        }

        @Override
        public void onPostExecute(Void result) {
            boolean success = true;
            if (StringUtils.stringNotNullOrEmpty(mHtml)) {
                Document doc = Jsoup.parse(mHtml);
                Pattern pattern = Pattern.compile("background(-image)?: url[\\s]*\\((.?)*\\)");

                Element style = doc.select("style").first();
                if (style != null) {

                    List<Node> childNodes = style.childNodes();
                    String heroImageData = childNodes.get(0).attr("data");
                    Matcher matcher = pattern.matcher(heroImageData);
                    String heroUrl = "";
                    if (matcher.find()) {
                        heroUrl = matcher.group(0);
                    }

                    Matcher urlMatcher = Patterns.WEB_URL.matcher(heroUrl);
                    if (urlMatcher.find()) {
                        heroUrl = urlMatcher.group(0);
                    }

                    Element meta = doc.select("div.meta").first();
                    Element content = doc.getElementById("content");
                    ActivityObject activityObject = new ActivityObject();

                    if (StringUtils.stringNotNullOrEmpty(heroUrl)) {
                        heroUrl = heroUrl.replace("(", "").replace(")", "");
                    }
                    activityObject.setImageUrl(heroUrl);
                    activityObject.setEndPoint(mLink);
                    activityObject.setDescription(ActivityHelper.getTextForMeta("meta__description", meta));
                    activityObject.setSchedule(ActivityHelper.getTextForMeta("meta__schedule", meta));

                    ActivityHelper.storeFeaturesFromContent(activityObject.getEndPoint(), content);

                    onDownloadComplete(activityObject);
                } else {
                    success = false;
                }
            } else {
                success = false;
            }

            if (!success) {
                onDownloadFailed();
            }
        }
    }

    public static TextView getParagraphText(Context context) {
        TextView para = new TextView(context);
        para.setTextSize(15);
        para.setTextColor(context.getResources().getColor(android.R.color.white));
        para.setMovementMethod(LinkMovementMethod.getInstance());
        para.setLinksClickable(true);
        return para;
    }


    public static String getHeroImageLink(Element style) {
        String url = "";


        return url;
    }
}
