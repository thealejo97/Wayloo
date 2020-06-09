package com.wayloo.wayloo.ui.anadirpeluqueria;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AnadirPeluqueriaModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AnadirPeluqueriaModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}