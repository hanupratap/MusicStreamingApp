package com.example.musicstreamer;


import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import android.widget.SearchView;


import com.bumptech.glide.Glide;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;

import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;




public class TrackList extends Fragment {

    private PagedList.Config config;

    private Random r = new Random();
    private ImageView img;
    private RecyclerView rv;

    private MyAdapter adapter;
    private Query query;
    private Main main ;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Tracks");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_track_list, container, false);
        rv = view.findViewById(R.id.track_list);
        img = view.findViewById(R.id.image_cover_app);
        main = (Main) getActivity();





        if(main!=null)
        {
            main.selectedFragment = new TrackList();

            config = new PagedList.Config.Builder()
                    .setInitialLoadSizeHint(10)
                    .setPageSize(5)
                    .build();


            setUpRecyclerView();
        }





        return view;
    }



    private void setUpRecyclerView()
    {
        query = notebookRef.orderBy("artist", Query.Direction.ASCENDING);

        FirestorePagingOptions<Track> options = new FirestorePagingOptions.Builder<Track>()
                .setQuery(query, config ,Track.class)
                .build();
        adapter = new MyAdapter(options);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(adapter);
        rv.setHasFixedSize(true);
        rv.setItemViewCacheSize(20);
        rv.setDrawingCacheEnabled(true);
        rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);


        adapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(DocumentSnapshot documentSnapshot, int position) {



                Track item = new Track();

                item.name = documentSnapshot.getString("name");
                item.artist = documentSnapshot.getString("artist");
                item.artist_art = documentSnapshot.getString("artist_art");
                item.album_art = documentSnapshot.getString("album_art");
                item.url = documentSnapshot.getString("url");
                item.lyrics = documentSnapshot.getString("lyrics");
                item.id = documentSnapshot.getId();
                item.path = documentSnapshot.getReference().getPath();

                Fragment fr = new Player();
                main.selectedFragment = fr;
                FragmentTransaction transaction;
                transaction = main.getSupportFragmentManager().beginTransaction();

                if(App.firstStart == true)
                {
                    App.current_track = item;


                    transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit, R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim);
                    getParentFragmentManager().beginTransaction().add(R.id.main_fragment, fr).addToBackStack(Player.class.toString()).commit();
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
                            getParentFragmentManager().popBackStack(Player.class.toString(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
                            getParentFragmentManager().beginTransaction().add(R.id.main_fragment, fr).addToBackStack(Player.class.toString()).commit();
                        }
                    }
                    else
                    {
                        App.current_track = item;


                        transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit, R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim);
                        getParentFragmentManager().beginTransaction().add(R.id.main_fragment, fr).addToBackStack(Player.class.toString()).commit();

                    }
                }


                App.firstStart = false;


                main.bottomNavigationView.getMenu().findItem(R.id.play).setChecked(true);


            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseFirestore.getInstance().collection("CoverImages").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int a = queryDocumentSnapshots.size();
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();


                Glide.with(main).load(documents.get(r.nextInt(a)).getString("url")).centerCrop().into(img);
            }
        });
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onResume() {

        super.onResume();
    }
}
