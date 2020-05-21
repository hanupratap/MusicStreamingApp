package com.example.musicstreamer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;


public class Search extends Fragment {

//    SearchView searchView;
    String queryText;
    SearchAdapter searchAdapter;
    private AdView mAdView;
    int temp = 0;
    MaterialSearchBar searchView;
    List<Track> list = new ArrayList<>();
    CollectionReference notebookRef = FirebaseFirestore.getInstance().collection("Tracks");
    Query query1;
    DocumentSnapshot documentSnapshot_temp;
    List<Track> fav_list = new ArrayList<>();
    RecyclerView rv2, favRecycler;
    private SwipeRefreshLayout mySwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);


        searchView = view.findViewById(R.id.search_track);

        mySwipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateRecycler();

                    }
                }
        );



        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        favRecycler = view.findViewById(R.id.my_fav_list);
        favRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        favRecycler.setHasFixedSize(true);
        favRecycler.setAdapter(null);

        updateRecycler();



        searchView.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                queryText = text.toString();
                queryText = queryText.substring(0,1).toUpperCase() + queryText.substring(1);
                Query query = notebookRef.orderBy("name").startAt(queryText)
                        .endAt(queryText + "\uf8ff");
                func(query);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        return view;
    }

    private void updateRecycler() {

        fav_list.clear();

        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Favourites").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for(DocumentSnapshot documentSnapshot:queryDocumentSnapshots)
                {
                    if(documentSnapshot.getBoolean("isfav")==true)
                    {
                        FirebaseFirestore.getInstance().document(documentSnapshot.getString("path")).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                Track item = new Track();

                                item.name = documentSnapshot.getString("name");
                                item.artist = documentSnapshot.getString("artist");
                                item.artist_art = documentSnapshot.getString("artist_art");
                                item.album_art = documentSnapshot.getString("album_art");
                                item.url = documentSnapshot.getString("url");
                                item.lyrics = documentSnapshot.getString("lyrics");
                                item.id = documentSnapshot.getId();
                                item.path = documentSnapshot.getReference().getPath();
                                list.add(item);
                                fav_list.add(item);

                                SearchAdapter searchAdapter = new SearchAdapter(getActivity(), fav_list);
                                favRecycler.setAdapter(searchAdapter);

                                if(mySwipeRefreshLayout.isRefreshing())
                                {
                                    mySwipeRefreshLayout.setRefreshing(false);
                                }

                            }
                        });

                    }
                }


            }
        });
    }


    private void func(Query query) {

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
                    item.path = documentSnapshot.getReference().getPath();
                    list.add(item);

                }

                if(list.size()>0)
                {
                    FragmentTransaction transaction;
                    transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    getActivity().getSupportFragmentManager().popBackStack(Player.class.toString(),0);
                    transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
                    transaction.add(R.id.main_fragment, new SearchResults(list), SearchResults.class.toString());
                    transaction.addToBackStack(SearchResults.class.toString());
                    transaction.commit();
                }
                else {
                    if(temp == -2)
                    {
                        Toast.makeText(getActivity(), "Not Available", Toast.LENGTH_SHORT).show();
                        temp = 0;
                    }
                    else
                    {
                        temp--;
                        Query query = notebookRef.orderBy("artist").startAt(queryText)
                                .endAt(queryText + "\uf8ff");
                        func(query);
                    }
                }
            }
        });



    }


    protected void setUpRecyclerView()
    {

    }






}
