package com.example.musicstreamer;

import android.content.Intent;
import android.gesture.GestureLibraries;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.paperdb.Paper;


public class TrackList extends Fragment {


    Random r = new Random();
    ArrayAdapter<String> ad;
    List<String> list = new ArrayList<>();
    List<String> ids = new ArrayList<>();
    List<String> urls = new ArrayList<>();
    ImageView img;
    RecyclerView rv;
    MyAdapter adapter;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Tracks");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_track_list, container, false);
        rv = view.findViewById(R.id.track_list);
        img = view.findViewById(R.id.image_cover_app);

        FirebaseFirestore.getInstance().collection("CoverImages").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int a = queryDocumentSnapshots.size();
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                Glide.with(getActivity()).load(documents.get(r.nextInt(a)).getString("url")).centerCrop().into(img);
            }
        });



        setUpRecyclerView();

        return view;
    }

    private void setUpRecyclerView()
    {

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(7)
                .build();

        Query query = notebookRef.orderBy("name", Query.Direction.DESCENDING);
        FirestorePagingOptions<Track> options = new FirestorePagingOptions.Builder<Track>()
                .setQuery(query, config ,Track.class)
                .build();
        adapter = new MyAdapter(options);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(adapter);

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


                Paper.init(getActivity());
                Paper.book().write("current_track", item);

                getParentFragmentManager().beginTransaction().replace(R.id.main_fragment, new Player()).commit();

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
