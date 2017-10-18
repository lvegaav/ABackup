
package com.americavoice.backup.settings.ui;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.calls.ui.CallsBackupFragment;
import com.americavoice.backup.contacts.ui.ContactsBackupFragment;
import com.americavoice.backup.db.PreferenceManager;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.news.ui.NewsActivity;
import com.americavoice.backup.service.MediaContentJob;
import com.americavoice.backup.service.WifiRetryJob;
import com.americavoice.backup.settings.presenter.SettingsPresenter;
import com.americavoice.backup.sms.ui.SmsBackupFragment;
import com.americavoice.backup.sync.service.SyncBackupJob;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.ConnectivityUtils;
import com.americavoice.backup.utils.PermissionUtil;
import com.americavoice.backup.utils.WifiUtils;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.greenrobot.eventbus.Subscribe;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Fragment that shows details of a certain political party.
 */
public class SettingsFragment extends BaseFragment implements SettingsView {

    /**
     * Interface for listening submit button.
     */
    public interface Listener {
        void onBackSettingsClicked();
        void viewStorageInfo();
    }

    @Inject
    SettingsPresenter mPresenter;
    private Listener mListener;
    private Unbinder mUnBind;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindString(R.string.main_title)
    String title;

    @BindView(R.id.tv_version_name)
    TextView tvVersionName;

    @BindView(R.id.use_mobile_data)
    SwitchCompat mUseMobileData;

    @BindView(R.id.btn_share)
    RelativeLayout btnShare;

    public SettingsFragment() {
        super();
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            this.mListener = (Listener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_settings, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);

        tvTitle.setText(getText(R.string.settings_title));

        PackageInfo pInfo = null;
        try {
            pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            tvVersionName.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // checkbox initial values
        boolean useMobileData = PreferenceManager.instantUploadWithMobileData(getContext());
        mUseMobileData.setChecked(useMobileData);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mPresenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mPresenter.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBind.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mPresenter.destroy();
    }


    private void initialize() {
        this.getComponent(AppComponent.class).inject(this);
        this.mPresenter.setView(this);
        // check if there is no connectivity
        if (!ConnectivityUtils.isAppConnected(getContext())) {
            showToastMessage(getString(R.string.common_connectivity_error));
            return;
        }
        this.mPresenter.initialize();
    }

    @Override
    public void showLoading() {
        hideLoading();
        mProgress = ProgressDialog.show(getActivity(),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.common_loading),
                true,
                false);
    }

    @Override
    public void hideLoading() {
        if (mProgress != null) {
            mProgress.hide();
            mProgress.dismiss();
            mProgress = null;
        }
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        this.showDialogMessage(message);
    }


    @Override
    public Context getContext() {
        return getActivity().getApplicationContext();
    }

    @OnClick(R.id.btn_back)
    void onButtonBack()
    {
        if (this.mListener != null) this.mListener.onBackSettingsClicked();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        if (this.mListener != null) this.mListener.onBackSettingsClicked();
    }

    @OnClick(R.id.tv_logout)
    public void logout(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // cancel job makes the isScheduled validation
            MediaContentJob.cancelJob(getContext());
            WifiRetryJob.cancelJob(getContext());
        }
        mPresenter.logout();
        ActivityCompat.finishAffinity(getActivity());
    }

    @OnCheckedChanged(R.id.use_mobile_data)
    public void onChangePhotoOverWifi() {
        Log_OC.d("Upload using mobile data", "" + mUseMobileData.isChecked());
        if (mUseMobileData.isChecked()) {
            WifiUtils.wifiConnected(getContext());
        }
        PreferenceManager.setInstantUploadUsingMobileData(getContext(), mUseMobileData.isChecked());
    }

    @OnClick(R.id.ll_contact_us)
    public void onContactUsClick() {
        Intent intent = new Intent(getActivity(), ContactUsActivity.class);
        startActivity(intent);
    }
  
    @OnClick(R.id.ll_news_info)
    public void onNewsInfoClick() {
        Intent intent = new Intent(getActivity(), NewsActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.btn_share)
    public void onShare() {
        final String appPackageName = getContext().getPackageName();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "Check this app at: https://play.google.com/store/apps/details?id=" + appPackageName);
        intent.setType("text/plain");
        startActivity(intent);
    }

    @OnClick(R.id.ll_storage_info)
    public void onStorageInfo() {
        if (mListener != null) {
            mListener.viewStorageInfo();
        }
    }

}

