
package com.smilo.bullpen;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class BullpenListViewService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new BullpenListViewFactory(this.getApplicationContext(),
                intent));
    }

}
