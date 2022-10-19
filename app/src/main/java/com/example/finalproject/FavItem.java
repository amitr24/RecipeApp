package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.finalproject.MainActivity.EXTRA_FAV;
import static com.example.finalproject.MainActivity.EXTRA_INGREDIENTS;
import static com.example.finalproject.MainActivity.EXTRA_NAME;
import static com.example.finalproject.MainActivity.EXTRA_READY;
import static com.example.finalproject.MainActivity.EXTRA_URL;
import static com.example.finalproject.MainActivity.EXTRA_XTRAINFO;

public class FavItem extends AppCompatActivity implements FoodAdapter.OnItemClickListener{
    FoodAdapter foodAdapter;
    public static final String KEY_URL = "imageUrl", KEY_INGREDIENTS = "ingredients", KEY_READY = "ready";
    public static final String KEY_NAME = "name";
    public static final String KEY_XTRAINFO = "info";
    public static final String KEY_ID = "id";
    public static final String KEY_FAV = "favorited";

    Button update;
    ArrayList<FoodItem> allFavoritedFoods = new ArrayList<FoodItem>();
    RecyclerView recyclerView;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail());

    public FavItem(){

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);

        Intent intent = getIntent();
        recyclerView = findViewById(R.id.favorites);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //
        update();



    }

    public void update(){
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        allFavoritedFoods.clear();
                        String imageUrl;
                        String name;
                        String extra;
                        String ready;
                        String ingredients;
                        String id;
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for( int i = 0; i < list.size(); i++){
                            DocumentSnapshot documentSnapshot = list.get(i);
                            Log.d("TAGG", documentSnapshot.toString());
                            if(documentSnapshot.exists()){
                                Map<String, Object> favFood = documentSnapshot.getData();
                                name = favFood.get(KEY_NAME).toString();
                                imageUrl = favFood.get(KEY_URL).toString();
                                extra = favFood.get(KEY_XTRAINFO).toString();
                                ready = favFood.get(KEY_READY).toString();
                                ingredients = favFood.get(KEY_INGREDIENTS).toString();
                                id = favFood.get(KEY_ID).toString();
                                allFavoritedFoods.add(new FoodItem(imageUrl, name, ready, extra, ingredients, id, FirebaseAuth.getInstance().getCurrentUser().getUid()));
                            }
                            else{
                                Toast.makeText(FavItem.this, "Document doesn't exist", Toast.LENGTH_SHORT);
                            }
                        }

                        foodAdapter = new FoodAdapter(allFavoritedFoods, FavItem.this, "FavItem");
                        recyclerView.setAdapter(foodAdapter);
                        foodAdapter.setOnItemClickListener(FavItem.this);



                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FavItem.this, "Error!", Toast.LENGTH_LONG);
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(this, DetailCard.class);
        FoodItem clickedItem = allFavoritedFoods.get(position);
        final String EXTRA_URL = "imageUrl", EXTRA_INGREDIENTS = "ingredients", EXTRA_READY = "ready";
        final String EXTRA_NAME = "name";
        final String EXTRA_XTRAINFO = "info";
        final String EXTRA_FAV = "favorite";

        detailIntent.putExtra(EXTRA_URL, clickedItem.getImageUrl());
        detailIntent.putExtra(EXTRA_READY, clickedItem.getReadyTime());
        detailIntent.putExtra(EXTRA_INGREDIENTS, clickedItem.getIngredients());
        detailIntent.putExtra(EXTRA_XTRAINFO, clickedItem.getExtraInfo());
        detailIntent.putExtra(EXTRA_NAME, clickedItem.getTitle());
        detailIntent.putExtra(EXTRA_FAV, clickedItem.getFavStatus());

        startActivity(detailIntent);
    }
}
