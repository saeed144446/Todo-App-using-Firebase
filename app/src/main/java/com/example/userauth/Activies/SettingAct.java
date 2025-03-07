package com.example.userauth.Activies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.userauth.R;
import com.example.userauth.databinding.ActivityMainBinding;
import com.example.userauth.databinding.ActivitySettingBinding;

public class SettingAct extends AppCompatActivity {

  private ActivitySettingBinding bindings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindings=ActivitySettingBinding.inflate(getLayoutInflater());
        View view=bindings.getRoot();
        setContentView(view);
        bindings.toolbartext.setText("Setting");

        bindings.backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        bindings.profileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingAct.this, ProfileAct.class));
            }
        });

    }
}