package com.wayloo.wayloo.ui.mispeluquerias;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MisPeluqueriasModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MisPeluqueriasModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}