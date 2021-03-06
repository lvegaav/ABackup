
package com.americavoice.backup.main.presenter;


import com.americavoice.backup.Const;
import com.americavoice.backup.main.data.SharedPrefsUtils;
import com.americavoice.backup.main.network.NetworkProvider;

public abstract class BasePresenter {

    protected final NetworkProvider mNetworkProvider;
    protected final SharedPrefsUtils mSharedPrefsUtils;

    public BasePresenter(SharedPrefsUtils sharedPrefsUtils, NetworkProvider networkProvider) {
        mNetworkProvider = networkProvider;
        this.mSharedPrefsUtils = sharedPrefsUtils;
    }
}
