
package com.smilo.bullpen.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.smilo.bullpen.definitions.Constants;

public class ListViewService extends RemoteViewsService {

    private static final String TAG = "ListViewService";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    public static final String LISTVIEW_SERVICE_CLASS_NAME = Constants.Specific.PACKAGE_NAME + ".services." + TAG;
    
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new ListViewFactory(this.getApplicationContext(),
                intent));
    }
}
