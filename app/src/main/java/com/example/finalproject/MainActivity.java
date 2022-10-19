package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements FoodAdapter.OnItemClickListener{


    private static final int RC_SIGN_IN = 123;
    public static final String EXTRA_URL = "imageUrl", EXTRA_INGREDIENTS = "ingredients", EXTRA_READY = "ready";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_XTRAINFO = "info";
    public static final String EXTRA_FAV = "favorite";
    public static final String EXTRA_ID = "id";

    EditText editText;
    JSONObject myJSONObject;
    Button searchButton;
    String information;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference favFoods = db.collection("Favorite Foods");

    FoodItem foodItem;
    List<AuthUI.IdpConfig> providers;
    String name;
    String email;
    Uri photoUrl;
    private RecyclerView recyclerView;
    private RequestQueue mRequestQueue;
    private FoodAdapter foodAdapter;
    private Button favoriteButton;
    private ArrayList<FoodItem> foodItemArrayList;
    private ArrayList<String> foodIDs= new ArrayList<>();
    private ArrayList<String> readyTimes= new ArrayList<String>();
    private ArrayList<String> foodImageUrls = new ArrayList<String>();
    private ArrayList<String> names= new ArrayList<String>();
    private static String API_KEY = "apiKey=e32a523e97b84640870f3e7ff45b7112";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.authUser();

        this.retrieveUserInfo();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodItemArrayList = new ArrayList<>();

        mRequestQueue = Volley.newRequestQueue(this);

        editText = findViewById(R.id.editTextTextPersonName);
        searchButton = findViewById(R.id.button);
        favoriteButton = findViewById(R.id.favPgButton);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FavItem.class);
                startActivity(intent);
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDownloader downloader = new MyDownloader();
                String search = editText.getText().toString();
                if (editText.length() > 0) {
                    editText.getText().clear();
                }

                foodItemArrayList.clear();
                foodIDs.clear();
                readyTimes.clear();
                foodImageUrls.clear();
                names.clear();

                downloader.execute("https://api.spoonacular.com/recipes/search?query=" + search + "&number=5&instructionsRequired=true&"+API_KEY);

            }
        });



    }

    private void parseJSON(/*String searchJSONObject jsonObject*/) throws JSONException {

        String url = "https://api.spoonacular.com/recipes/informationBulk?" +API_KEY + "&ids=";
        for(int i = 0; i < foodIDs.size()-1; i++){
            url = url + foodIDs.get(i) + ",";
        }
        url = url + foodIDs.get(foodIDs.size()-1);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("TAGG", "HELLO");
                for(int i = 0; i < response.length(); i++){
                    try {

                        JSONObject jsonObject = response.getJSONObject(i);
                        String extraInfo = "Extra Info: Vegetarian - " + jsonObject.getString("vegetarian") + "; Vegan - " + jsonObject.getString("vegan")
                                + "; Gluten Free - " + jsonObject.getString("glutenFree") + "; Dairy Free - " + jsonObject.getString("dairyFree");
                        JSONArray extendedIngredients = jsonObject.getJSONArray("extendedIngredients");
                        String ingredients = "Ingredients: ";
                        for(int h = 0; h < extendedIngredients.length(); h++){
                            ingredients += extendedIngredients.getJSONObject(h).getString("name");
                            if(h!=extendedIngredients.length()-1){
                                ingredients+=", ";
                            }
                        }
                        foodItemArrayList.add(new FoodItem(foodImageUrls.get(i), names.get(i), readyTimes.get(i), extraInfo, ingredients, foodIDs.get(i), FirebaseAuth.getInstance().getCurrentUser().getUid()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    foodAdapter = new FoodAdapter(foodItemArrayList, MainActivity.this, "MainActivity");
                    recyclerView.setAdapter(foodAdapter);
                    foodAdapter.setOnItemClickListener(MainActivity.this);


                }


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                // TODO Auto-generated method stub

                Log.d("TEXT", arg0.toString());

            }


        }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(request);

    }

    public void retrieveUserInfo(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                String providerId = profile.getProviderId();

                // UID specific to the provider
                String uid = profile.getUid();

                // Name, email address, and profile photo Url
                name = profile.getDisplayName();
                email = profile.getEmail();
                photoUrl = profile.getPhotoUrl();
            }
        }
    }

    public void authUser(){
        // Choose authentication providers
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.logo)
                        .build(),
                RC_SIGN_IN);

        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(/* yourPackageName= */ getApplicationContext().getPackageName(), /* installIfNotAvailable= */ true,
                        /* minimumVersion= */ null)
                .setHandleCodeInApp(true) // This must be set to true
                .setUrl("https://google.com") // This URL needs to be whitelisted
                .build();

        if (AuthUI.canHandleIntent(getIntent())) {
            if (getIntent().getExtras() == null) {
                return;
            }
            String link = getIntent().getExtras().getString(ExtraConstants.EMAIL_LINK_SIGN_IN);
            if (link != null) {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setEmailLink(link)
                                .setAvailableProviders(getAvailableProviders())
                                .build(),
                        RC_SIGN_IN);
            }
        }

        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });

        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }


    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(this, DetailCard.class);
        FoodItem clickedItem = foodItemArrayList.get(position);

        detailIntent.putExtra(EXTRA_URL, clickedItem.getImageUrl());
        detailIntent.putExtra(EXTRA_READY, clickedItem.getReadyTime());
        detailIntent.putExtra(EXTRA_INGREDIENTS, clickedItem.getIngredients());
        detailIntent.putExtra(EXTRA_XTRAINFO, clickedItem.getExtraInfo());
        detailIntent.putExtra(EXTRA_NAME, clickedItem.getTitle());
        detailIntent.putExtra(EXTRA_FAV, clickedItem.getFavStatus());

        startActivity(detailIntent);
    }


    private class MyDownloader extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... param){
            try{
                URL url = new URL(param[0]); //URL to API to get data
                URLConnection urlConnection = url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();

                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String t;
                while((t = bufferedReader.readLine())!=null){
                    stringBuilder.append(t);
                }

                information = stringBuilder.toString();
                myJSONObject = new JSONObject(information);

            }catch (MalformedURLException e) {
                e.printStackTrace();
            }catch(JSONException e) {
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }

            return myJSONObject;
        }

        @Override
        protected void onPostExecute(JSONObject obj) {
            super.onPostExecute(obj);
            try {
                JSONArray jsonArray = myJSONObject.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject food = jsonArray.getJSONObject(i);
                    String title = food.getString("title");
                    String readyTime = "Preparation Time: " + food.getInt("readyInMinutes") + " minutes";
                    String imageUrl = "https://spoonacular.com/recipeImages/" + food.getInt("id") + "-556x370.jpg";
                    foodImageUrls.add(imageUrl);
                    foodIDs.add(""+food.getInt("id"));
                    readyTimes.add(readyTime);
                    names.add(title);


                }
                parseJSON();
                Log.d("TEXT", myJSONObject.toString()/*.getJSONArray("results").getJSONObject(0).getString("title")*/);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    private List<AuthUI.IdpConfig> getAvailableProviders() {
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                .setHandleCodeInApp(true)
                .setUrl("https://foo")
                .build();

        return Collections.singletonList(
                new AuthUI.IdpConfig.EmailBuilder()
                        .setAllowNewAccounts(false)
                        .enableEmailLinkSignIn()
                        .setActionCodeSettings(actionCodeSettings)
                        .build());

    }



}