package com.example.musicstreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.paperdb.Paper;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    List<Track> trackList;
    Context context;

    SearchAdapter(Context context, List<Track> list)
    {
        this.context = context;
        this.trackList = list;
    }


    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.track_lis_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final SearchAdapter.ViewHolder holder, int position) {
        holder.tv.setText( trackList.get(position).name);
        holder.tv1.setText( trackList.get(position).artist);

        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Track item = new Track();


                Paper.init(context);
                Paper.book().write("current_track", item);
                FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();

                manager.beginTransaction().replace(R.id.main_fragment, new Player()).commit();

            }
        });

        holder.tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.tv.performClick();
            }
        });
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tv,tv1;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.track_string);
            tv1 = itemView.findViewById(R.id.artist_name);


        }
    }

}
