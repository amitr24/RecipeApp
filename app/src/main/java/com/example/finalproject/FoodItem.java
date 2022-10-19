package com.example.finalproject;

public class FoodItem {
    private String imageUrl;
    private String title;
    private String readyTime;
    private String extraInfo;
    private String ingredients;
    private String favStatus;
    private String id;
    private String userId;


    public FoodItem(){

    }
    public FoodItem(String imageUrl, String title, String readyTime, String extraInfo, String ingredients, String id, String userId){
        this.imageUrl = imageUrl;
        this.title = title;
        this.readyTime = readyTime;
        this.extraInfo = extraInfo;
        this.ingredients = ingredients;
        this.favStatus = favStatus;
        this.id = id;
        this.userId = userId;
    }

    public String getImageUrl(){
        return imageUrl;
    }
    public String getTitle(){
        return title;
    }

    public String getFavStatus() {
        return favStatus;
    }

    public String getReadyTime() {
        return readyTime;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }
}
