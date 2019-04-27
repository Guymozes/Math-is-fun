package com.example.guy.projectapp1;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.Arrays;
import java.util.List;


public class LoginPage extends Utils {
    private static final int REQUEST_CODE = 8448;
    List<AuthUI.IdpConfig> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button switch_to_single_user_btn = (Button) findViewById(R.id.switchToSingleUserBtn);
        switch_to_single_user_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.mode = SINGLE_MODE;
                saveUser(user);
                Intent intent = new Intent(LoginPage.this, MainActivity.class);
                intent.putExtra("new_connection",true);
                startActivity(intent);
            }
        });

        providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        Button btn_sign_in = (Button) findViewById(R.id.signInBtn);
        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.mode = MULTI_MODE;
                showSignInOptions();
            }
        });
    }

    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.ThemeForFirebase)
                .build(), REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK){
                FirebaseUser user_loggin = FirebaseAuth.getInstance().getCurrentUser();
                String user_id = user_loggin.getUid();
                user.id_data_base = user_id;
                Toast.makeText(this,""+user_loggin.getEmail(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginPage.this, MainActivity.class));
                finish();
            }
            else{
                Toast.makeText(this,""+response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(LoginPage.this, FirstAppPage.class));
        finish();
    }
}