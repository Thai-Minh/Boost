package com.cleaning.boost.ibooster.inf;

import android.content.Context;

public interface ActionListener {

    void onStarted(Object[] params);

    void onCompleted(Context context, Object[] params);

}
