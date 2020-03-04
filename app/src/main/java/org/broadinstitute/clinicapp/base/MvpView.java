package org.broadinstitute.clinicapp.base;


import androidx.annotation.StringRes;

@SuppressWarnings("unused")
public interface MvpView {

    void showLoading();

    void hideLoading();

     void onError(@StringRes int resId);

    void onError(String message);

    void showMessage(String message);

    boolean isNetworkConnected();

}