
package com.smilo.bullpen;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class BullpenContentService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new BullpenContentFactory(this.getApplicationContext(),
                intent));
    }

}
