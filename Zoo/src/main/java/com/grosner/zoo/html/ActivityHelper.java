package com.grosner.zoo.html;

import com.activeandroid.manager.SingleDBManager;
import com.grosner.zoo.database.content.FeatureObject;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by: andrewgrosner
 * Date: 7/22/14.
 * Contributors: {}
 * Description:
 */
public class ActivityHelper {

    public static String getTextForMeta(String metaName, Element meta){
        String text = "";
        Element metaDescription = meta.select("div[class=" + metaName + " padded]").first();
        if(metaDescription!=null) {
            Element divText = metaDescription.select("div.text").first();
            if(divText!=null) {
                Element p = divText.select("p").first();
                if(p!=null) {
                    text = p.text();
                }
            }
        }
        return text;
    }

    public static String[] getTestimonialForContent(Element content){
        Element testimonial = content.select("testimonial").first();
        return null;
    }

    public static void storeFeaturesFromContent(String activityUrl, Element content){
        Element element = content.select("div[class=featuring]").first();
        ArrayList<FeatureObject> featureObjects = new ArrayList<>();
        if(element!=null){
            Elements elements = element.select("div[class=featuring__cards__item]");
            for(Element e: elements){
                Element image = e.select("img").first();
                if(image!=null) {
                    FeatureObject featureObject = new FeatureObject();
                    featureObject.setImageUrl(image.attr("src"));
                    featureObject.setActivity_url(activityUrl);
                    Element cardWrapper = e.select("div[class=featuring__cards__wrapper]")
                            .first();
                    if(cardWrapper!=null) {
                        Element label = cardWrapper.select("span[class=featuring__cards__label").first();
                        if(label!=null) {
                            featureObject.setLabel(label.text());
                        }
                    }
                    featureObjects.add(featureObject);
                }
            }
        }

        SingleDBManager.getSharedInstance().addAll(featureObjects);
    }
}
