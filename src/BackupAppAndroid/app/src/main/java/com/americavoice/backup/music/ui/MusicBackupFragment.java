package com.americavoice.backup.music.ui;


import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.americavoice.backup.R;
import com.americavoice.backup.di.components.AppComponent;
import com.americavoice.backup.main.event.OnBackPress;
import com.americavoice.backup.main.ui.BaseFragment;
import com.americavoice.backup.music.event.EnableSelectSongEvent;
import com.americavoice.backup.music.model.Song;
import com.americavoice.backup.music.presenter.MusicBackupPresenter;
import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicBackupFragment extends BaseFragment implements MusicBackupView {

    private Unbinder mUnBind;
    private Listener mListener;
    private List<Song> songs = new ArrayList<>();
    private SongBackupAdapter songBackupAdapter;
    private Menu mMenu;

    private AsyncTask loadMusicBackupTask;

    /**
     * Interface for listening file list events.
     */
    public interface Listener {
        void onMusicBackPressed();
    }


    @Inject
    MusicBackupPresenter mPresenter;

    @BindView(R.id.musicRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.empty_list_container)
    public FrameLayout emptyListContainer;

    @BindView(R.id.music_restore_selected)
    public Button restoreContacts;


    public MusicBackupFragment() {
        // no-op
    }

    public static MusicBackupFragment newInstance() {
        return new MusicBackupFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_music_backup, container, false);
        mUnBind = ButterKnife.bind(this, fragmentView);
        setHasOptionsMenu(true);

        songBackupAdapter = new SongBackupAdapter(songs);
        recyclerView.setAdapter(songBackupAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        restoreContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSelectedSongs();
            }
        });

        try {
            getLoadMusicBackupTask().execute();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return fragmentView;
    }

    public void sendSelectedSongs() {
        List<Song> songList = songBackupAdapter.getSongsListSelected();
        if (! songList.isEmpty()) {
            List<String> paths = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (int i = 0; i < songList.size(); i++) {
                paths.add(songList.get(i).getPath());
                names.add(songList.get(i).getTitle());
            }
            Intent intent = getActivity().getIntent();
            intent.putStringArrayListExtra("songPaths", (ArrayList<String>) paths);
            intent.putStringArrayListExtra("songNames", (ArrayList<String>) names);
            getActivity().setResult(RESULT_OK, intent);
        }
        getActivity().onBackPressed();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.contactlist_menu, menu);
        mMenu = menu;
        mMenu.getItem(0).setVisible(false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retval;
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                retval = true;
                break;
            case R.id.action_select_all:
                item.setChecked(! item.isChecked());
                setSelectAllMenuItem(item, item.isChecked());
                songBackupAdapter.selectAllFiles();
                retval = true;
                break;
            default:
                retval = super.onOptionsItemSelected(item);
                break;
        }
        return retval;
    }

    private void setSelectAllMenuItem(MenuItem selectAll, boolean checked) {
        selectAll.setChecked(checked);
        if (checked) {
            selectAll.setTitle(R.string.contacts_select_all);
        } else {
            selectAll.setTitle(R.string.contacts_select_none);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnableSelectSongEvent event) {
        if (event.isEnableBtn()) {
            restoreContacts.setVisibility(View.VISIBLE);
        } else {
            restoreContacts.setVisibility(GONE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initialize();
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof MusicBackupFragment.Listener) {
            this.mListener = (MusicBackupFragment.Listener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mPresenter.resume();
        if (getLoadMusicBackupTask().isCancelled()) {
            loadMusicBackupTask = null;
            try {
                getLoadMusicBackupTask().execute();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mPresenter.pause();
        getLoadMusicBackupTask().cancel(true);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mPresenter.destroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBind.unbind();
    }

    private void initialize() {
        this.getComponent(AppComponent.class).inject(this);
        this.mPresenter.setView(this);
        this.mPresenter.initialize(getString(R.string.main_music));

    }

    void onButtonBack() {
        if (this.mListener != null) {
            this.mListener.onMusicBackPressed();
        }
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

    @Subscribe
    public void onEvent(OnBackPress onBackPress) {
        onButtonBack();
    }


    public AsyncTask getLoadMusicBackupTask() {
        if (loadMusicBackupTask == null) {
            loadMusicBackupTask = new AsyncTask() {

                @Override
                protected Object doInBackground(Object[] objects) {
                    if (! isCancelled()) {
                        String path = Environment.getExternalStorageDirectory().getAbsolutePath(); //+ "/Music";
                        songs = getPlayList(path);

                        return true;
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Object o) {
                    if (! isCancelled()) {
                        songBackupAdapter.replaceSongList(songs);
                        emptyListContainer.setVisibility(GONE);
                        restoreContacts.setVisibility(View.VISIBLE);
                        restoreContacts.setEnabled(true);
                        if (mMenu != null) {
                            mMenu.getItem(0).setVisible(true);
                            onOptionsItemSelected(mMenu.findItem(R.id.action_select_all));
                        }
                    }
                }
            };
        }

        return loadMusicBackupTask;
    }

    List<Song> getPlayList(String rootPath) {
        List<Song> SongsList = new ArrayList<>();
        try {
            File rootFolder = new File(rootPath);
            File[] files = rootFolder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (File file : files) {
                if (file.isDirectory()) {
                    if (getPlayList(file.getAbsolutePath()) != null && ! file.getAbsolutePath().contains("americavoice")) {
                        SongsList.addAll(getPlayList(file.getAbsolutePath()));
                    } else {
                        break;
                    }
                } else if (file.getName().endsWith(".mp3") || file.getName().endsWith(".aac")) {
                    Uri uri = Uri.parse(file.getAbsolutePath());
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(getActivity(), uri);

                    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    mmr.setDataSource(file.getAbsolutePath());
                    int miliSecond = Integer.parseInt(durationStr);
                    byte[] data = mmr.getEmbeddedPicture();
                    Song song = new Song(file.getName(), artist, file.getAbsolutePath(), miliSecond, data);
                    SongsList.add(song);
                }
            }
            return SongsList;
        } catch (Exception e) {
            return null;
        }
    }
}

