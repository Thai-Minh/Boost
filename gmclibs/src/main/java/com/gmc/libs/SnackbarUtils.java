package com.gmc.libs;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtils {

    public static Snackbar make(View v, String btnAction) {
        Snackbar snackbar = Snackbar.make(v, "This Snackbar!", Snackbar.LENGTH_LONG)
                .setAction(btnAction, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
        return snackbar;
    }
}
