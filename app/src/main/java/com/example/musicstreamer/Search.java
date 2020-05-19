package com.example.musicstreamer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class Search extends Fragment {

    SearchView searchView;
    String queryText;
    SearchAdapter searchAdapter;
    private AdView mAdView;

    List<Track> list = new ArrayList<>();
    CollectionReference notebookRef = FirebaseFirestore.getInstance().collection("Tracks");
    Query query1;
    RecyclerView rv2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);


        searchView = view.findViewById(R.id.search_track);




        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryText = query;
                func();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });


        return view;
    }



    private void func() {
        Query query = notebookRef.orderBy("name").startAt(queryText)
                .endAt(queryText + "\uf8ff");



        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                list.clear();
                for(DocumentSnapshot documentSnapshot:queryDocumentSnapshots)
                {


                    Track item = new Track();

                    item.name = documentSnapshot.getString("name");
                    item.artist = documentSnapshot.getString("artist");
                    item.artist_art = documentSnapshot.getString("artist_art");
                    item.album_art = documentSnapshot.getString("album_art");
                    item.url = documentSnapshot.getString("url");
                    item.lyrics = documentSnapshot.getString("lyrics");
                    item.id = documentSnapshot.getId();
                    Toast.makeText(getActivity(), item.name, Toast.LENGTH_SHORT).show();

                    list.add(item);

                    Toast.makeText(getActivity(), documentSnapshot.getString("name"), Toast.LENGTH_SHORT).show();

                }

                FragmentTransaction transaction;
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                getActivity().getSupportFragmentManager().popBackStack(Player.class.toString(),0);
                transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit);

                transaction.add(R.id.main_fragment, new SearchResults(list), SearchResults.class.toString());
                transaction.addToBackStack(SearchResults.class.toString());
                transaction.commit();


            }
        });



    }






}
