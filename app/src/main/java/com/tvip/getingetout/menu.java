package com.tvip.getingetout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.tvip.getingetout.MainActivity.KEY_NIK;

public class menu extends AppCompatActivity {

    MaterialCardView rekapabsensi, detailabsen;

    MaterialCardView refresh, refresh2;

    TextView minimumF1, minimumF4;

    TextView checkout, checkin, textlist, widget;
    //jam
    TextView jam, depotanggal, harisekarang;
    static TextView txt_nama, txt_perusahaan;
    static TextView text_jabatan;
    TextView tv_greeting;
    static TextView txt_nik;
    static TextView jabatan;
    SharedPreferences sharedPreferences;

    LinearLayout time;
    ImageView biodata;
    MaterialCardView listkaryawantkbm;

    MaterialCardView buttonmasuk, buttonkeluar, shifting, logout;
    private static final int REQUEST_LOCATION = 1;
    static String keterangan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        HttpsTrustManager.allowAllSSL();
        txt_nik = findViewById(R.id.txt_nik);
        text_jabatan = findViewById(R.id.text_jabatan);
        time = findViewById(R.id.time);
        tv_greeting = findViewById(R.id.tv_greeting);
        txt_nama = findViewById(R.id.txt_nama);
        biodata = findViewById(R.id.biodata);
        buttonmasuk = findViewById(R.id.buttonmasuk);
        logout = findViewById(R.id.logout);
        listkaryawantkbm = findViewById(R.id.listkaryawantkbm);
        buttonkeluar = findViewById(R.id.buttonkeluar);
        depotanggal = findViewById(R.id.depotanggal);
        jam = findViewById(R.id.jam);
        harisekarang = findViewById(R.id.harisekarang);
        checkout = findViewById(R.id.checkout);
        checkin = findViewById(R.id.checkin);
        textlist = findViewById(R.id.textlist);
        shifting = findViewById(R.id.shifting);

        minimumF1 = findViewById(R.id.minimumF1);
        minimumF4 = findViewById(R.id.minimumF4);

        rekapabsensi = findViewById(R.id.rekapabsensi);
        detailabsen = findViewById(R.id.detailabsen);

        widget = findViewById(R.id.widget);
        


        rekapabsensi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), rekap_absensi.class);
                startActivity(intent);
            }
        });

        shifting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), list_shifting.class);
                startActivity(intent);
            }
        });





        refresh = findViewById(R.id.refresh);
        refresh2 = findViewById(R.id.refresh2);

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String currentDateandTime = sdf.format(new Date());
                jam.setText(currentDateandTime);
                handler.postDelayed(this, 1000);

                Calendar c = Calendar.getInstance();
                int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

            }
        };
        runnable.run();

        txt_perusahaan = findViewById(R.id.txt_perusahaan);
        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);

        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        biodata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (menu.this, biodata.class);
                startActivity(intent);
            }
        });

        listkaryawantkbm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent (menu.this, list_absensi_karyawan.class);
                    startActivity(intent);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        menu.this);
                alertDialogBuilder.setTitle("Logout ?");
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.apply();
                                Intent intent = new Intent(menu.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });






        String nik_baru = sharedPreferences.getString(KEY_NIK, null);


        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://hrd.tvip.co.id/rest_server/master/karyawan/index?nik_baru=" + nik_baru,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray movieArray = obj.getJSONArray("data");

                            for (int i = 0; i < movieArray.length(); i++) {

                                JSONObject movieObject = movieArray.getJSONObject(i);
                                txt_nama.setText(movieObject.getString("nama_karyawan_struktur"));
                                txt_nik.setText(movieObject.getString("nik_baru"));

                                text_jabatan.setText(movieObject.getString("jabatan_karyawan"));

                                refresh.setVisibility(View.GONE);
                                textlist.setVisibility(View.VISIBLE);



                                cekAbsen();

                                buttonmasuk.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(menu.this, absen_mobile.class);
                                        startActivity(intent);

                                    }
                                });

                                buttonkeluar.setVisibility(View.GONE);

                                Date c = Calendar.getInstance().getTime();
                                SimpleDateFormat df = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
                                String formattedDate = df.format(c);
                                harisekarang.setText(formattedDate);


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        listkaryawantkbm.setVisibility(View.GONE);
                        refresh2.setVisibility(View.GONE);
                        textlist.setVisibility(View.GONE);
                        shifting.setVisibility(View.GONE);
                        widget.setVisibility(View.GONE);

                        rekapabsensi.setVisibility(View.GONE);
                        detailabsen.setVisibility(View.GONE);


                        buttonmasuk.setVisibility(View.VISIBLE);
                        buttonkeluar.setVisibility(View.VISIBLE);
                        logout.setVisibility(View.VISIBLE);

                        biodata.setVisibility(View.INVISIBLE);
                        tv_greeting.setVisibility(View.INVISIBLE);
                        txt_nama.setVisibility(View.INVISIBLE);
                        text_jabatan.setVisibility(View.INVISIBLE);
                        txt_nik.setVisibility(View.INVISIBLE);

                        Date c = Calendar.getInstance().getTime();
                        SimpleDateFormat df = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
                        String formattedDate = df.format(c);

                        time.setVisibility(View.VISIBLE);
                        depotanggal.setText("Depo, " + nik_baru + " • " + formattedDate);

                        txt_nama.setText("Depo " + nik_baru);
                        text_jabatan.setVisibility(View.INVISIBLE);
                        txt_nik.setVisibility(View.INVISIBLE);

                        buttonmasuk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(menu.this, absen_mobile_leader.class);
                                startActivity(intent);

                            }
                        });

                        buttonkeluar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(menu.this, absen_mobile_out.class);
                                startActivity(intent);

                            }
                        });

                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", "admin", "Databa53");
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        stringRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                ));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void cekAbsen() {
        String nik_baru = sharedPreferences.getString(KEY_NIK, null);

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = df.format(c);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://hrd.tvip.co.id/rest_server/pengajuan/Absenmobile/index_cek_absengigo?tanggal1="+formattedDate+"&tanggal2="+formattedDate+"&nik=" + nik_baru,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray movieArray = obj.getJSONArray("data");

                            for (int i = 0; i < movieArray.length(); i++) {

                                JSONObject movieObject = movieArray.getJSONObject(i);


                                if(movieObject.getString("F1").equals("null")){
                                    checkin.setText("00:00:00");
                                } else {
                                    checkin.setText(convertFormat(movieObject.getString("F1")));
                                }

                                if(movieObject.getString("F4").equals("null")){
                                    checkout.setText("00:00:00");
                                } else {
                                    checkout.setText(convertFormat(movieObject.getString("F4")));
                                }

                                minimumF1.setText("Scheduled : " + convertFormat(movieObject.getString("waktu_masuk")));
                                minimumF4.setText("Scheduled : " + convertFormat(movieObject.getString("waktu_keluar")));



                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        checkin.setText("00:00:00");
                        checkout.setText("00:00:00");

                        minimumF1.setText("Tidak Ada Jadwal");
                        minimumF4.setText("Tidak Ada Jadwal");



                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", "admin", "Databa53");
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        stringRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                ));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle("Anda yakin ingin keluar dari aplikasi ini ?");
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();
                        finish();
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public static String convertFormat(String inputDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return "-";
        }
        SimpleDateFormat convetDateFormat = new SimpleDateFormat("HH:mm:ss");
        return convetDateFormat.format(date);
    }

}