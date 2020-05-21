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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
    public void onBindViewHolder(@NonNull final SearchAdapter.ViewHolder holder, final int position) {
        holder.tv.setText( trackList.get(position).name);
        holder.tv1.setText( trackList.get(position).artist);

        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Track item = new Track();

                item = trackList.get(position);

                FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();

                Main main = (Main)context;
                Fragment fr = new Player();
                main.selectedFragment = fr;
                FragmentTransaction transaction;
                transaction = main.getSupportFragmentManager().beginTransaction();

                if(App.firstStart == true)
                {
                    App.current_track = item;


                    transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit, R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim);
                    ((Main)context).getSupportFragmentManager().beginTransaction().add(R.id.main_fragment, fr).addToBackStack(Player.class.toString()).commit();
                }
                else
                {
                    if(App.current_track.id!=null)
                    {
                        if(App.current_track.id.equals(item.id))
                        {

                            Fragment temp_frag = main.getSupportFragmentManager().findFragmentByTag(Player.class.toString());
                            main.selectedFragment = new Player();
                            if(temp_frag == null)
                            {
                                transaction = main.getSupportFragmentManager().beginTransaction();
                                main.getSupportFragmentManager().popBackStack(Player.class.toString(),FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                transaction.add(R.id.main_fragment, main.selectedFragment, Player.class.toString());
                                transaction.addToBackStack(Player.class.toString());
                                transaction.commit();
                            }
                            else
                            {
                                main.displayFragmentPlayer();
                            }


                        }
                        else
                        {
                            App.current_track = item;
                            ((Main)context).getSupportFragmentManager().popBackStack(Player.class.toString(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
                            ((Main)context).getSupportFragmentManager().beginTransaction().add(R.id.main_fragment, fr).addToBackStack(Player.class.toString()).commit();
                        }
                    }
                    else
                    {
                        App.current_track = item;


                        transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit, R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim);
                        ((Main)context).getSupportFragmentManager().beginTransaction().add(R.id.main_fragment, fr).addToBackStack(Player.class.toString()).commit();

                    }
                }



                App.firstStart = false;


                main.bottomNavigationView.getMenu().findItem(R.id.play).setChecked(true);



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
