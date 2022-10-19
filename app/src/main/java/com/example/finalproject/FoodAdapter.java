package com.example.finalproject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    private ArrayList<FoodItem> foodItems;
    private Context context;
    static String id;
    private OnItemClickListener mListener;
    private String activityName;
    public static final String KEY_ID = "id";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail());


    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }
    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name, readyTime, ingredients, extraInfo;
        public ImageView picture;
        public Button favBtn;
        Boolean wasFound = false;


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        public static final String KEY_URL = "imageUrl", KEY_INGREDIENTS = "ingredients", KEY_READY = "ready";
        public static final String KEY_NAME = "name";
        public static final String KEY_XTRAINFO = "info";
        public static final String KEY_ID = "id";
        public static final String KEY_FAV = "favorited";


        public ViewHolder(View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.title);
            picture = itemView.findViewById(R.id.thumbnail);
            readyTime = itemView.findViewById(R.id.readyInMinutes);
            ingredients = itemView.findViewById(R.id.ingredients);
            extraInfo = itemView.findViewById(R.id.extraInfo);
            favBtn = itemView.findViewById(R.id.favBtn);

            itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    if(mListener!=null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
            if(activityName.equals("MainActivity"))
                favBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int position = getAdapterPosition();
                        FoodItem foodItem = foodItems.get(position);
                        Map<String, Object> favFood = new HashMap<>();
                        favFood.put(KEY_NAME, foodItem.getTitle());
                        favFood.put(KEY_URL, foodItem.getImageUrl());
                        favFood.put(KEY_ID, foodItem.getId());
                        favFood.put(KEY_INGREDIENTS, foodItem.getIngredients());
                        favFood.put(KEY_READY, foodItem.getReadyTime());
                        favFood.put(KEY_XTRAINFO, foodItem.getExtraInfo());

                        id = foodItem.getId();

                        userRef.get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        String retreivedId;
                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                        for( int i = 0; i < list.size(); i++){
                                            DocumentSnapshot documentSnapshot = list.get(i);

                                            if(documentSnapshot.exists()){
                                                Map<String, Object> favFood = documentSnapshot.getData();
                                                retreivedId = favFood.get(KEY_ID).toString();
                                                if(retreivedId.equals(id)){
                                                    wasFound = true;
                                                    break;
                                                }
                                                else {
                                                    wasFound = false;
                                                }
                                            }
                                            else{

                                            }
                                        }


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
                        Log.d("TAGG", ""+wasFound);

                        if(wasFound){
                            favBtn.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24);
                            userRef.document(id).delete();
                            wasFound = false;
                        }
                        else{
                            userRef.document(id).set(favFood)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(itemView.getContext(), "Added to Favorites", Toast.LENGTH_SHORT);
                                            favBtn.setBackgroundResource(R.drawable.ic_favorite_red_24);
                                            Log.d("TAGG", "HELLO");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(itemView.getContext(), "Was Not Added From Favorites", Toast.LENGTH_SHORT);
                                            Log.d("Error in Storing", e.toString());
                                        }
                                    });
                        }





                    }
                });
        }
    }
    public FoodAdapter(ArrayList<FoodItem> foodItems, Context context, String classCall){
        this.activityName = classCall;
        this.foodItems = foodItems;
        this.context = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(context).inflate(R.layout.card, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem foodItem = foodItems.get(position);
        String mImageUrl = foodItem.getImageUrl();
        String mName = foodItem.getTitle();
        id = foodItem.getId();

        holder.name.setText(mName);
        if(activityName.equals("MainActivity")) {
            Log.d("TAGG", "DIDNT CHANGE IMAGE");
            userRef.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            String retreivedId;
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (int i = 0; i < list.size(); i++) {
                                DocumentSnapshot documentSnapshot = list.get(i);
                                if (documentSnapshot.exists()) {
                                    Map<String, Object> favFood = documentSnapshot.getData();
                                    retreivedId = favFood.get(KEY_ID).toString();
                                    if (retreivedId.equals(id)) {

                                        holder.favBtn.setBackgroundResource(R.drawable.ic_favorite_red_24);
                                        break;

                                    } else {
                                        Log.d("TAGG", "THIS WAS NOT FAVORITED");
                                        holder.favBtn.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24);
                                    }
                                } else {
                                }
                            }
                            //foodAdapter.setOnItemClickListener();


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
        /*holder.readyTime.setText("Preparation Time: " + mReadyTime);
        holder.ingredients.setText("Ingredients: " + mIngredients);
        holder.extraInfo.setText("Extra Info: " + mExtraInfo);*/
        if(activityName.equals("FavItem")) {
            holder.favBtn.setBackgroundResource(R.drawable.ic_favorite_red_24);
            Log.d("TAGG", "DIDNT CHANGE IMAGE");
        }
        Picasso.with(context).load(mImageUrl).fit().centerInside().into(holder.picture);
    }
}
