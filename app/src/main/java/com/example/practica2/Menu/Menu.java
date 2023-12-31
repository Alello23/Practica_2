package com.example.practica2.Menu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.practica2.Menu.Account.*;
import com.example.practica2.Menu.Chats.ChatFragment;
import com.example.practica2.Menu.Home.HomeFragment;
import com.example.practica2.Menu.WishList.WishListsFragment;
import com.example.practica2.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Menu extends AppCompatActivity {
    private RequestQueue requestQueue;
    private BottomNavigationView bottomNavigationView;
    private String userID;
    private HomeFragment home;
    private ChatFragment chat;
    private WishListsFragment wishlists;
    private AccountFragment account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decodeJWT();
        setContentView(R.layout.activity_menu);
        saveToSharedPrefs(getIntent().getStringExtra("User"));
        requestQueue = Volley.newRequestQueue(Menu.this);
        bottomNavigationView = findViewById(R.id.ME_bottomNavigationView);

        home = new HomeFragment(requestQueue, userID);
        chat = new ChatFragment(requestQueue, userID);
        account = new AccountFragment(requestQueue, userID);
        wishlists = new WishListsFragment(requestQueue, userID);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.ME_fragmentContainerView);

        if (fragment == null){
            fragment = home;
            fm.beginTransaction().add(R.id.ME_fragmentContainerView, fragment).commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home){
                openFragment(home);
                return true;
            }
            if (item.getItemId() == R.id.navigation_chat){
                openFragment(chat);
                return true;
            }
            if (item.getItemId() == R.id.navigation_wishlist){
                openFragment(wishlists);
                return true;
            }
            if (item.getItemId() == R.id.navigation_account){
                openFragment(account);
                return true;
            }
            return false;
        });
        openFragment(home);
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.ME_fragmentContainerView, fragment);
        fragmentTransaction.commit();
    }
    public void saveToSharedPrefs(String token) {
        SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putString("token", token);
        prefsEditor.apply();
    }

    public void saveID(String id) {
        SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putString("user_id", id);
        prefsEditor.apply();
    }

    @Override
    public void onBackPressed() {
        // Disable back button functionality
    }

    public String getFromSharedPrefs() {
        SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);
        String valor = sharedPrefs.getString("token", "default");
        return valor;
    }

    private void decodeJWT() {
        // Decode JWT to get user id
        String[] splitToken = getFromSharedPrefs().split("\\.");
        byte[] decodedBytes = Base64.decode(splitToken[1], Base64.URL_SAFE);
        String jsonBody = new String(decodedBytes, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonBody);
            userID = jsonObject.getString("id");
            saveID(userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
