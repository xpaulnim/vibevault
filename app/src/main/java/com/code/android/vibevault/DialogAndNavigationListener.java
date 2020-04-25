package com.code.android.vibevault;

import android.os.Bundle;

public interface DialogAndNavigationListener {

    void showLoadingDialog(String message);

    void showLoadingDialog(String message, boolean useTitle);

    void showDialog(String message, String title);

    void hideDialog();

    void showSettingsDialog(Bundle b);

    void goHome();

}