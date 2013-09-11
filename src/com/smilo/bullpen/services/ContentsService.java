
package com.smilo.bullpen.services;

import com.smilo.bullpen.Constants;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ContentsService extends RemoteViewsService {

    private static final String TAG = "ContentsService";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    public static final String CONTENTS_SERVICE_CLASS_NAME = Constants.Specific.PACKAGE_NAME + ".services." + TAG;
    
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new ContentsFactory(this.getApplicationContext(),
                intent));
    }

}
