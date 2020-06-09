package com.wayloo.wayloo.ui.misReservas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MisReservasModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MisReservasModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is tools fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}