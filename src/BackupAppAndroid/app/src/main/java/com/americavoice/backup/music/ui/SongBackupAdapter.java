package com.americavoice.backup.music.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.americavoice.backup.R;
import com.americavoice.backup.music.event.EnableSelectSongEvent;
import com.americavoice.backup.music.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pj on 2/15/18.
 */

public class SongBackupAdapter extends RecyclerView.Adapter<SongBackupAdapter.SongViewHolder> {


    private List<Song> songsList;
    private boolean checkedAll;
    private EnableSelectSongEvent enableSelectSongEvent;

    public SongBackupAdapter(List<Song> songsList) {
        this.songsList = songsList;
        this.enableSelectSongEvent = new EnableSelectSongEvent();
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.song_item_layout, parent, false);
        return new SongViewHolder(itemView);
    }

    public List<Song> getSongsListSelected() {
        List<Song> songsListResult = new ArrayList<>();

        for (Song song : songsList) {
            if (song.isSelected()) {
                songsListResult.add(song);
            }
        }

        return songsListResult;
    }

    void replaceSongList(List<Song> songsList) {
        this.songsList = songsList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final SongViewHolder holder, final int position) {
        if (songsList.get(position).getCover() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(songsList.get(position).getCover(), 0, songsList.get(position).getCover().length);
            holder.cover.setImageBitmap(bitmap);
        }
        holder.title.setText(songsList.get(position).getTitle());
        holder.time.setText(songsList.get(position).getDuration());

        holder.time.setChecked(songsList.get(position).isSelected());

        holder.time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.time.isChecked()) {
                    holder.time.setChecked(false);
                    songsList.get(position).setSelected(false);

                } else {
                    holder.time.setChecked(true);
                    songsList.get(position).setSelected(true);
                }

                boolean enableBtn = false;
                for (Song song : songsList) {
                    if (song.isSelected()) {
                        enableBtn = true;
                    }

                }
                enableSelectSongEvent.setEnableBtn(enableBtn);
                enableSelectSongEvent.post();
            }
        });
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public void selectAllFiles() {
        checkedAll = ! checkedAll;
        for (Song song : songsList) {
            song.setSelected(checkedAll);
        }

        enableSelectSongEvent.setEnableBtn(checkedAll);
        enableSelectSongEvent.post();
        notifyDataSetChanged();

    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        public final ImageView cover;
        public final TextView title;
        public final TextView subtitle;
        public final CheckedTextView time;

        SongViewHolder(View itemView) {
            super(itemView);

            cover = itemView.findViewById(R.id.row_image);
            title = itemView.findViewById(R.id.row_title);
            subtitle = itemView.findViewById(R.id.row_subtitle);
            time = itemView.findViewById(R.id.row_time);

        }
    }
}
