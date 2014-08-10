package com.grosner.zoo.html;

import com.grosner.zoo.database.content.ActivityObject;

/**
* Created by: andrewgrosner
* Date: 8/10/14.
* Contributors: {}
* Description:
*/
public interface DownloadListener {
    public void onDownloadComplete(Object htmlObject);

    public void onDownloadFailed();
}
