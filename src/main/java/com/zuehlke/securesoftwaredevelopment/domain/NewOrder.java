package com.zuehlke.securesoftwaredevelopment.domain;


import java.util.Arrays;

public class NewOrder {
    Integer restaurantId;
    String comment;
    Integer address;
    FoodItem[] items;


    public NewOrder(Integer restaurantId, String comment,  Integer address, FoodItem[] items) {
        this.restaurantId = restaurantId;
        this.comment = comment;
        this.items = items;
        this.address = address;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public FoodItem[] getItems() {
        return items;
    }

    public void setItems(FoodItem[] items) {
        this.items = items;
    }

    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "NewOrder{" +
                "restaurantId=" + restaurantId +
                ", comment='" + comment + '\'' +
                ", address=" + address +
                ", items=" + Arrays.toString(items) +
                '}';
    }
}
