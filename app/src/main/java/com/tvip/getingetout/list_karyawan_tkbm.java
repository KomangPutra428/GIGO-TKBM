package com.tvip.getingetout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class list_karyawan_tkbm extends AppCompatActivity {
    LinearLayout detailabsen, absenmobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_karyawan_tkbm);
        detailabsen = findViewById(R.id.detailabsen);
        absenmobile = findViewById(R.id.absenmobile);

        detailabsen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), list_karyawan.class);
                startActivity(intent);
            }
        });

        absenmobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), absen_mobile_leader.class);
                startActivity(intent);
            }
        });
    }
}