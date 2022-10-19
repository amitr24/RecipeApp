package com.example.finalproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import static com.example.finalproject.MainActivity.EXTRA_FAV;
import static com.example.finalproject.MainActivity.EXTRA_INGREDIENTS;
import static com.example.finalproject.MainActivity.EXTRA_NAME;
import static com.example.finalproject.MainActivity.EXTRA_READY;
import static com.example.finalproject.MainActivity.EXTRA_URL;
import static com.example.finalproject.MainActivity.EXTRA_XTRAINFO;

public class DetailCard extends AppCompatActivity {


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_card);

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra(EXTRA_URL);
        String name = intent.getStringExtra(EXTRA_NAME);
        String extra = intent.getStringExtra(EXTRA_XTRAINFO);
        String ready = intent.getStringExtra(EXTRA_READY);
        String ingredients = intent.getStringExtra(EXTRA_INGREDIENTS);
        String favStatus = intent.getStringExtra(EXTRA_FAV);

        ImageView imageView = findViewById(R.id.foodImage);
        TextView textViewName = findViewById(R.id.title2);
        TextView textViewReady = findViewById(R.id.readyInMinutes);
        TextView textViewIngredients = findViewById(R.id.ingredients);
        TextView textViewExtraInfo = findViewById(R.id.extraInfo);

        Picasso.with(this).load(imageUrl).fit().centerInside().into(imageView);
        textViewIngredients.setText(ingredients);
        textViewName.setText(name);
        textViewReady.setText(ready);
        textViewExtraInfo.setText(extra);



    }
}