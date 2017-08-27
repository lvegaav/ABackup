
package com.americavoice.backup.settings.ui;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.explorer.Const;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.settings.presenter.SettingsPresenter;
import com.americavoice.backup.utils.DisplayUtils;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
public class SettingsFragment extends BaseFragment implements SettingsView {

    /**
     * Interface for listening submit button.
     */
    public interface Listener {
        void onBackSettingsClicked();
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

    /*@OnClick(R.id.btn_logout)
    public void logout(){
        Account account = AccountUtils.getCurrentOwnCloudAccount(getContext());
        AccountUtils.removeAccount(getContext(), account);
        mPresenter.logout();
        //getActivity().finishAffinity();
    }*/
    private float getPercent(BigDecimal value, BigDecimal size)
    {
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
        float availablePercent = getPercent(totalAvailable, total);


        tvImages.setText(String.format("%.1f %%", photoPercent));
        tvVideos.setText(String.format("%.1f %%", videoPercent));
        tvContacts.setText(String.format("%.1f %%", contactPercent));
        tvFiles.setText(String.format("%.1f %%", documentPercent));

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        List<Integer> colors = new ArrayList<>();

        if (photoPercent > 0)
        {
            entries.add(new PieEntry(photoPercent, photos));
            colors.add(Color.rgb(49, 61, 102));
        }
        if (videoPercent > 0)
        {
            entries.add(new PieEntry(videoPercent, videos));
            colors.add(Color.rgb(72, 82, 118));
        }
        if (contactPercent > 0)
        {
            entries.add(new PieEntry(contactPercent, contacts));
            colors.add(Color.rgb(95, 104, 136));
        }
        if (documentPercent > 0)
        {
            entries.add(new PieEntry(documentPercent, documents));
            colors.add(Color.rgb(118, 126, 153));
        }
        entries.add(new PieEntry(availablePercent, available));
        colors.add(Color.rgb(16, 24, 51));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        dataSet.setColors(colors);
        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(1f);
        dataSet.setValueLinePart2Length(0.4f);
        //dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        chartPie.setData(data);

        chartPie.setCenterText(DisplayUtils.bytesToHumanReadable(total.longValue()));
        chartPie.setCenterTextColor(Color.rgb(243, 114, 54));

        chartPie.setDrawSliceText(false);
        // undo all highlights
        chartPie.highlightValues(null);
        Legend l = chartPie.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setEnabled(true);

        chartPie.invalidate();
        //chartPie.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        chartPie.spin(2000, 0, 360, Easing.EasingOption.EaseInOutQuad);

    }
}

