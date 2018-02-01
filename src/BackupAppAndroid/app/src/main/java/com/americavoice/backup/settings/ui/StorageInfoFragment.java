
package com.americavoice.backup.settings.ui;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.payment.ui.PaymentActivity;
import com.americavoice.backup.service.MediaContentJob;
import com.americavoice.backup.service.WifiRetryJob;
import com.americavoice.backup.settings.presenter.StorageInfoPresenter;
import com.americavoice.backup.sms.ui.SmsBackupFragment;
import com.americavoice.backup.sync.service.SyncBackupJob;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.ConnectivityUtils;
import com.americavoice.backup.utils.PermissionUtil;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.greenrobot.eventbus.Subscribe;

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
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Fragment that shows details of a certain political party.
 */
public class StorageInfoFragment extends BaseFragment implements StorageInfoView {

    /**
     * Interface for listening submit button.
     */
    public interface Listener {
        void onBackStorageInfoClicked();
    }

    @Inject
    StorageInfoPresenter mPresenter;
    private Listener mListener;
    private Unbinder mUnBind;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindString(R.string.main_title)
    String title;
    @BindView(R.id.btn_sync_files)
    RelativeLayout btnSyncFiles;
    @BindView(R.id.tv_images)
    TextView tvImages;
    @BindView(R.id.tv_contacts)
    TextView tvContacts;
    @BindView(R.id.tv_videos)
    TextView tvVideos;
    @BindView(R.id.tv_files)
    TextView tvFiles;
    @BindView(R.id.tv_sms)
    TextView tvSms;
    @BindView(R.id.tv_calls)
    TextView tvCalls;

    @BindView(R.id.capacity_text)
    TextView mCapacityView;
    @BindView(R.id.available_capacity_text)
    TextView mAvailableView;
    @BindView(R.id.used_capacity_text)
    TextView mUsedView;

    @BindView(R.id.ratios)
    View mRatios;
    @BindView(R.id.ll_space_description)
    LinearLayout llSpaceDescription;

    @BindView(R.id.used_capacity_percentage)
    TextView tvUsedCapacityPercentage;

    @BindView(R.id.ll_information)
    LinearLayout llCapacityInfo;

    @BindString(R.string.main_photos)
    String photos;
    @BindString(R.string.main_videos)
    String videos;
    @BindString(R.string.main_contacts)
    String contacts;
    @BindString(R.string.main_documents)
    String documents;
    @BindString(R.string.main_available)
    String available;

    public StorageInfoFragment() {
        super();
    }

    public static StorageInfoFragment newInstance() {
        return new StorageInfoFragment();
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

        View fragmentView = inflater.inflate(R.layout.fragment_storage_info, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);

        tvTitle.setText(getText(R.string.storage_info_title));

        return fragmentView;
    }

    private void createRatioBar(List<Integer> colors, List<Float> ratios) {
        final List<Integer> barColors = new ArrayList<>();
        List<Float> barRatios = new ArrayList<>();
        float curRatio = 0;
        for (int i = 0; i < colors.size(); i++) {
            barColors.add(colors.get(i));
            barColors.add(colors.get(i));
            barRatios.add(curRatio);
            curRatio += ratios.get(i) / 100;
            barRatios.add(curRatio);
        }
        final int[] barColorsArray = new int[barColors.size()];
        final float[] barRatiosArray = new float[barRatios.size()];
        for (int i = 0; i < barColors.size(); i++) {
            barColorsArray[i] = barColors.get(i);
            barRatiosArray[i] = barRatios.get(i);
        }

        ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int i, int i1) {
                LinearGradient lg = new LinearGradient(0, 0, mRatios.getWidth(), 0,
                  barColorsArray,
                  barRatiosArray,
                  Shader.TileMode.REPEAT);

                return lg;
            }
        };
        PaintDrawable paintDrawable = new PaintDrawable();
        paintDrawable.setShape(new RoundRectShape(new float[]{100, 100, 100, 100, 100, 100, 100, 100}, null, null));
        paintDrawable.setShaderFactory(sf);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mRatios.setBackgroundDrawable(paintDrawable);
        } else {
            mRatios.setBackground(paintDrawable);
        }

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
    public void showGettingPending() {
        hideLoading();
        mProgress = ProgressDialog.show(getActivity(),
          getResources().getString(R.string.app_name),
          getResources().getString(R.string.sync_getting_pending_files),
          true,
          false);
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
    public void showDefaultError() {
        showToastMessage(getString(R.string.exception_message_generic));
    }

    @Override
    public Context getContext() {
        return getActivity().getApplicationContext();
    }

    @OnClick(R.id.btn_back)
    void onButtonBack() {
        if (this.mListener != null) this.mListener.onBackStorageInfoClicked();
    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        if (this.mListener != null) this.mListener.onBackStorageInfoClicked();
    }

    @OnClick(R.id.btn_sync_files)
    public void sync() {
        if (PermissionUtil.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (this.mPresenter != null) this.mPresenter.getPendingFiles();
        } else {
            showRequestPermissionDialog();
        }
    }

    @Override
    public void showSyncDialog(int pendingPhotos, int pendingVideos) {
        if (mDialogIsShowing)
            return;

        String textToDisplay = "";
        String permissionArgument = getString(R.string.sync_backup_photos_and_videos_as_well);
        if (!PermissionUtil.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS)
          || !PermissionUtil.checkSelfPermission(getContext(), Manifest.permission.READ_SMS)
          || !PermissionUtil.checkSelfPermission(getContext(), Manifest.permission.READ_CALL_LOG))
            permissionArgument = getString(R.string.sync_backup_photos_and_videos_when_permission_granted);
        if (pendingPhotos == 0 && pendingVideos == 0) {
            if (PreferenceManager.getInstantUploadUsingMobileData(getContext()) && !ConnectivityUtils.isAppConnectedViaUnmeteredWiFi(getContext())) {
                textToDisplay = getString(R.string.sync_backup_no_files_pending_warning, permissionArgument, getString(R.string.sync_warning_mobile_data_on));
            } else {
                textToDisplay = getString(R.string.sync_backup_no_files_pending, permissionArgument);
            }
        } else {
            if (PreferenceManager.getInstantUploadUsingMobileData(getContext()) && !ConnectivityUtils.isAppConnectedViaUnmeteredWiFi(getContext())) {
                textToDisplay = getString(R.string.sync_backup_photos_and_videos_warning, pendingPhotos, pendingVideos, permissionArgument, getString(R.string.sync_warning_mobile_data_on));
            } else {
                textToDisplay = getString(R.string.sync_backup_photos_and_videos, pendingPhotos, pendingVideos, permissionArgument);
            }
        }
        new MaterialDialog.Builder(getActivity())
          .onPositive(new MaterialDialog.SingleButtonCallback() {
              @Override
              public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                  if (mPresenter != null) {
                      mPresenter.scheduleSync();
                  }
              }
          })
          .onAny(new MaterialDialog.SingleButtonCallback() {
              @Override
              public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                  mDialogIsShowing = false;
              }
          })
          .title(R.string.app_name)
          .content(textToDisplay)
          .positiveText(R.string.sync_backup_now)
          .negativeText(R.string.common_cancel).build().show();
        mDialogIsShowing = true;
    }

    private float getPercent(BigDecimal value, BigDecimal size) {
        float x = value != null ? value.floatValue() * 100 : 0;
        float x1 = x / size.floatValue();
        BigDecimal x2 = new BigDecimal(x1).setScale(1, BigDecimal.ROUND_HALF_UP);
        return x2.floatValue();
    }

    @Override
    public void showPercent(HashMap<String, BigDecimal> sizes, BigDecimal total, BigDecimal totalAvailable) {
        if (llCapacityInfo != null) {
            llCapacityInfo.setVisibility(View.VISIBLE);

            float photoPercent = getPercent(sizes.get(BaseConstants.PHOTOS_FOLDER), total);
            float videoPercent = getPercent(sizes.get(BaseConstants.VIDEOS_FOLDER), total);
            float contactPercent = getPercent(sizes.get(BaseConstants.CONTACTS_FOLDER), total);
            float documentPercent = getPercent(sizes.get(BaseConstants.DOCUMENTS_FOLDER), total);
            float smsPercent = getPercent(sizes.get(BaseConstants.SMS_FOLDER), total);
            float callsPercent = getPercent(sizes.get(BaseConstants.CALLS_FOLDER), total);
            float availablePercent = getPercent(totalAvailable, total);

            float totalPercent = photoPercent + videoPercent + contactPercent + documentPercent + smsPercent + callsPercent;
            tvUsedCapacityPercentage.setText(getString(R.string.settings_storage_percentage, String.format(Locale.US, "%.1f", totalPercent)));

            List<Integer> colors = Arrays.asList(
              ContextCompat.getColor(getContext(), R.color.photos_ratio),
              ContextCompat.getColor(getContext(), R.color.videos_ratio),
              ContextCompat.getColor(getContext(), R.color.contacts_ratio),
              ContextCompat.getColor(getContext(), R.color.documents_ratio),
              ContextCompat.getColor(getContext(), R.color.sms_ratio),
              ContextCompat.getColor(getContext(), R.color.calls_ratio),
              ContextCompat.getColor(getContext(), R.color.available_ratio)
            );
            List<Float> ratios = Arrays.asList(
              photoPercent, videoPercent, contactPercent, documentPercent, smsPercent,
              callsPercent, availablePercent
            );

            createRatioBar(colors, ratios);

            float sizeGb = total.divide(new BigDecimal(1073741824), 1, BigDecimal.ROUND_HALF_UP).floatValue();
            float availableGb = totalAvailable.divide(new BigDecimal(1073741824), 1, BigDecimal.ROUND_HALF_UP).floatValue();
            float usedGb = sizeGb - availableGb;
            tvImages.setText(String.format(Locale.US, "%.1f %%", photoPercent));
            tvVideos.setText(String.format(Locale.US, "%.1f %%", videoPercent));
            tvContacts.setText(String.format(Locale.US, "%.1f %%", contactPercent));
            tvFiles.setText(String.format(Locale.US, "%.1f %%", documentPercent));
            tvSms.setText(String.format(Locale.US, "%.1f %%", smsPercent));
            tvCalls.setText(String.format(Locale.US, "%.1f %%", callsPercent));

            mCapacityView.setText(String.format(Locale.US, "%.1f GB", sizeGb));
            mAvailableView.setText(String.format(Locale.US, "%.1f GB", availableGb));
            mUsedView.setText(String.format(Locale.US, "%.1f GB", usedGb));
        }
    }

    @Override
    public void scheduleSyncJob(List<String> pendingPhotos, List<String> pendingVideos) {
        final Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());
        if (pendingPhotos.size() > 0 || pendingVideos.size() > 0) {
            PersistableBundleCompat bundle = new PersistableBundleCompat();
            bundle.putString(SyncBackupJob.ACCOUNT, account.name);
            bundle.putStringArray(SyncBackupJob.PENDING_PHOTOS, pendingPhotos.toArray(new String[pendingPhotos.size()]));
            bundle.putStringArray(SyncBackupJob.PENDING_VIDEOS, pendingVideos.toArray(new String[pendingVideos.size()]));
            bundle.putBoolean(SyncBackupJob.FORCE, true);

            new JobRequest.Builder(SyncBackupJob.TAG)
              .setExtras(bundle)
              .setExecutionWindow(3_000L, 10_000L)
              .setRequiresCharging(false)
              .setPersisted(false)
              .setUpdateCurrent(false)
              .build()
              .schedule();
        }
        ContactsBackupFragment.startForcedContactBackupJob(account);
        SmsBackupFragment.startForcedSmsBackupJob(account);
        CallsBackupFragment.startForcedCallBackupJob(account);
        int messageId = R.string.sync_preferences_backup_scheduled;

        if (!PreferenceManager.getInstantUploadUsingMobileData(getContext()) && !ConnectivityUtils.isAppConnectedViaUnmeteredWiFi(getContext())) {
            messageId = R.string.sync_preferences_backup_scheduled_on_wifi;
        }

        Toast.makeText(getContext(), messageId, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showRequestPermissionDialog() {
        showMessageOKCancel(getString(R.string.files_getting_pending_no_access), new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                  PermissionUtil.PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }
        });
    }


    private void showMessageOKCancel(String message, MaterialDialog.SingleButtonCallback okListener) {
        if (!mDialogIsShowing) {
            new MaterialDialog.Builder(getActivity())
              .onPositive(okListener)
              .onAny(new MaterialDialog.SingleButtonCallback() {
                  @Override
                  public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                      mDialogIsShowing = false;
                  }
              })
              .title(R.string.app_name)
              .content(message)
              .positiveText(R.string.common_ok)
              .negativeText(R.string.common_cancel)
              .build()
              .show();
            mDialogIsShowing = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.PERMISSIONS_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        MediaContentJob.scheduleJob(getContext());
                        WifiRetryJob.scheduleJob(getContext());
                    }
                    if (this.mPresenter != null) this.mPresenter.getPendingFiles();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @OnClick(R.id.btn_upgrade)
    public void onUpgrade() {
        Intent intent = new Intent(getContext(), PaymentActivity.class);
        startActivity(intent);
    }
}

