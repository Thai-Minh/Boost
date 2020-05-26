package com.cleaning.boost.ibooster.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MemoryViewModel extends ViewModel {

    /**
     * Live Data Instance
     */
    private final MutableLiveData<String> mName = new MutableLiveData<>();

    public void setName(String name) {
        mName.setValue(name);
    }

    public LiveData<String> getName() {
        return mName;
    }

}
