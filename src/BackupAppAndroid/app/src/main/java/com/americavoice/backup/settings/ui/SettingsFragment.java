
package com.americavoice.backup.settings.ui;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.db.PreferenceManager;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.explorer.Const;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.service.MediaContentJob;
import com.americavoice.backup.service.WifiRetryJob;
import com.americavoice.backup.settings.presenter.SettingsPresenter;
import com.americavoice.backup.utils.DisplayUtils;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.MPPointF;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
        void onRestoreClicked();
    }

    @Inject
    SettingsPresenter mPresenter;
    private Listener mListener;
    private Unbinder mUnBind;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindString(R.string.main_title)
    String title;
    @BindView(R.id.tv_sync_files)
    TextView tvSyncFiles;
    @BindView(R.id.tv_images)
    TextView tvImages;
    @BindView(R.id.tv_contacts)
    TextView tvContacts;
    @BindView(R.id.tv_videos)
    TextView tvVideos;
    @BindView(R.id.tv_files)
    TextView tvFiles;
    @BindView(R.id.chart)
    PieChart chartPie;
    @BindView(R.id.tv_version_name)
    TextView tvVersionName;

    @BindView(R.id.use_mobile_data)
    SwitchCompat mUseMobileData;

    @BindView(R.id.ratios)
    View mRatios;

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

        tvTitle.setText("");

        PackageInfo pInfo = null;
        try {
            pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            tvVersionName.setText(getString(R.string.settings_version, pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tvSyncFiles.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sync, 0, 0, 0);
        } else {
            Drawable draw = AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.ic_sync);
            tvSyncFiles.setCompoundDrawablesWithIntrinsicBounds(draw, null, null, null);
        }

        chartPie.setUsePercentValues(true);
        chartPie.getDescription().setEnabled(false);
        chartPie.setExtraOffsets(5, 10, 5, 5);

        chartPie.setDragDecelerationFrictionCoef(0.95f);

        chartPie.setDrawHoleEnabled(true);
        chartPie.setHoleColor(Color.WHITE);
        chartPie.getLegend().setTextColor(Color.WHITE);

        chartPie.setTransparentCircleColor(Color.WHITE);
        chartPie.setTransparentCircleAlpha(110);

        chartPie.setHoleRadius(58f);
        chartPie.setTransparentCircleRadius(61f);

        chartPie.setDrawCenterText(true);

        chartPie.setRotationAngle(0);
        // enable rotation of the chart by touch
        chartPie.setRotationEnabled(true);
        chartPie.setHighlightPerTapEnabled(true);

        // checkbox initial values
        boolean useMobileData = PreferenceManager.instantUploadWithMobileData(getContext());
        mUseMobileData.setChecked(useMobileData);
        return fragmentView;
    }

    private void createRatioBar(List<Integer> colors, List<Float> ratios) {
        //TODO:
        final List<Integer> barColors = new ArrayList<>();
        List<Float> barRatios = new ArrayList<>();
        float curRatio = 0;
        for(int i = 0; i < colors.size(); i++) {
            barColors.add(colors.get(i));
            barColors.add(colors.get(i));
            barRatios.add(curRatio);
            curRatio += ratios.get(i);
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
        paintDrawable.setShape(new RoundRectShape(new float[]{100,100,100,100,100,100,100,100}, null, null));
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
        this.mPresenter.initialize();
    }

    @Override
    public void showLoading() {
        if (mProgress != null) {
            mProgress.hide();
            mProgress.dismiss();
            mProgress = null;
        }
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
    public void showDefaultError() {
        showToastMessage(getString(R.string.exception_message_generic));
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
        Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());
        AccountUtils.removeAccount(getContext(), account);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // cancel job makes the isScheduled validation
            MediaContentJob.cancelJob(getContext());
            WifiRetryJob.cancelJob(getContext());
        }
        mPresenter.logout();
        ActivityCompat.finishAffinity(getActivity());
    }

    @OnClick(R.id.tv_sync_files)
    public void sync()
    {
        if (this.mListener != null) this.mListener.onRestoreClicked();
    }
    private float getPercent(BigDecimal value, BigDecimal size) {
        float x = value.floatValue() * 100;
        float x1 = x / size.floatValue();
        BigDecimal x2= new BigDecimal(x1).setScale(1,BigDecimal.ROUND_HALF_UP);
        float x3 = x2.floatValue();
        return x3;
    }

    @Override
    public void showPercent(HashMap<String, BigDecimal> sizes, BigDecimal total, BigDecimal totalAvailable) {
        float photoPercent = getPercent(sizes.get(Const.Photos),total);
        float videoPercent = getPercent(sizes.get(Const.Videos),total);
        float contactPercent = getPercent(sizes.get(Const.Contacts),total);
        float documentPercent = getPercent(sizes.get(Const.Documents),total);
        float callsPercent = getPercent(sizes.get(Const.Calls),total);
        float smsPercent = getPercent(sizes.get(Const.Sms), total);
        float availablePercent = getPercent(totalAvailable, total);
        Log.v("percents", String.format("%s %s %s %s %s", photoPercent, videoPercent, contactPercent, documentPercent, availablePercent));

//        int[] colors = new int[] {
//                ContextCompat.getColor(getContext(), R.color.photos_ratio),
//                ContextCompat.getColor(getContext(), R.color.videos_ratio),
//                ContextCompat.getColor(getContext(), R.color.contacts_ratio),
//                ContextCompat.getColor(getContext(), R.color.documents_ratio),
//                ContextCompat.getColor(getContext(), R.color.calls_ratio),
//                ContextCompat.getColor(getContext(), R.color.sms_ratio),
//        };
        List<Integer> colors = Arrays.asList(
                ContextCompat.getColor(getContext(), R.color.photos_ratio),
                ContextCompat.getColor(getContext(), R.color.videos_ratio),
                ContextCompat.getColor(getContext(), R.color.contacts_ratio),
                ContextCompat.getColor(getContext(), R.color.documents_ratio),
                ContextCompat.getColor(getContext(), R.color.calls_ratio),
                ContextCompat.getColor(getContext(), R.color.sms_ratio),
                ContextCompat.getColor(getContext(), R.color.available_ratio)
        );
        List<Float> ratios = Arrays.asList(
                photoPercent, videoPercent, contactPercent, documentPercent, callsPercent,
                smsPercent, availablePercent
//                0.5f, 0.1f, 0.05f, 0.05f, 0.1f, 0f, 0.2f
        );

        tvImages.setText(String.format("%.1f %%", photoPercent));
        tvVideos.setText(String.format("%.1f %%", videoPercent));
        tvContacts.setText(String.format("%.1f %%", contactPercent));
        tvFiles.setText(String.format("%.1f %%", documentPercent));

        createRatioBar(colors, ratios);

    }

    @OnCheckedChanged(R.id.use_mobile_data)
    public void onChangePhotoOverWifi() {
        Log_OC.d("Upload using mobile data", "" + mUseMobileData.isChecked());
        PreferenceManager.setInstantUploadUsingMobileData(getContext(), mUseMobileData.isChecked());
    }


    @OnClick(R.id.iv_logo)
    public void onLogo() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(com.americavoice.backup.Const.ICON_URL));
        startActivity(browserIntent);
    }
}

