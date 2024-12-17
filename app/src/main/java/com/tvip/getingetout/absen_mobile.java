package com.tvip.getingetout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tvip.getingetout.pojos.keteranganmodel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.tvip.getingetout.MainActivity.KEY_NIK;

public class absen_mobile extends AppCompatActivity implements LocationListener {
    TextView tanggal, longitude, lat, nomor, jam, lokasi, keteranganhadir;
    ListView list;
    List<keteranganmodel> keteranganmodels = new ArrayList<>();
    SharedPreferences sharedPreferences;
    Button buttonmasuk, buttonkeluar;
    ProgressDialog pDialog;
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    SweetAlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absen_mobile);
        HttpsTrustManager.allowAllSSL();
        tanggal = findViewById(R.id.tanggal);
        list = findViewById(R.id.list);
        buttonmasuk = findViewById(R.id.buttonmasuk);
        buttonkeluar = findViewById(R.id.buttonkeluar);
        nomor = findViewById(R.id.nomor);
        jam = findViewById(R.id.jam);
        lokasi = findViewById(R.id.lokasi);
        keteranganhadir = findViewById(R.id.keteranganhadir);

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

        longitude = findViewById(R.id.longitude);
        lat = findViewById(R.id.lat);

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        String formattedDate = df.format(c);

        tanggal.setText(formattedDate);
        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }
        getData();
//        getMasuk();
//        getKeluar();


            buttonkeluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(longitude.getText().toString().equals("long") && lat.getText().toString().equals("lat")){
                    Toast.makeText(getApplicationContext(), "Maaf, Lokasi belum ditemukan", Toast.LENGTH_SHORT).show();
                } else {
                    IntentIntegrator intentIntegrator = new IntentIntegrator(absen_mobile.this);
                    intentIntegrator.initiateScan();
                }
            }
        });
    }

    private void getMasuk() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = df.format(c);

        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
        String nik_baru = sharedPreferences.getString(KEY_NIK, null);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://hrd.tvip.co.id/rest_server/pengajuan/absenmobile/index_cek_absen_kbkm2?tanggal=" + formattedDate + "&nik_ktp=" + nik_baru,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        getKeluar();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        buttonkeluar.setVisibility(View.GONE);
                    }
                }) {
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

    private void getKeluar() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = df.format(c);

        String untildate= formattedDate;//can take any date in current format
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime( dateFormat.parse(untildate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cal.add( Calendar.DATE, -1 );
        String convertedDate=dateFormat.format(cal.getTime());

        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
        String nik_baru = sharedPreferences.getString(KEY_NIK, null);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://hrd.tvip.co.id/rest_server/pengajuan/absenmobile/index_cek_absen_kbkm?nik_ktp=" + nik_baru,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            final JSONArray movieArray = obj.getJSONArray("data");
                            for (int i = 0; i < movieArray.length(); i++) {
                                final JSONObject movieObject = movieArray.getJSONObject(i);

                                        if(movieObject.getString("kat").equals("MASUK")){
                                            buttonkeluar.setVisibility(View.VISIBLE);
                                        } else  if(movieObject.getString("kat").equals("KELUAR")){
                                            buttonkeluar.setVisibility(View.GONE);
                                        }

                            }



                        } catch(JSONException e){
                            e.printStackTrace();

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        buttonmasuk.setVisibility(View.VISIBLE);

                        }
                }) {
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

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getData() {
        dialog = new SweetAlertDialog(absen_mobile.this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Harap Menunggu");
        dialog.show();

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = df.format(c);


        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
        String nik_baru = sharedPreferences.getString(KEY_NIK, null);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://hrd.tvip.co.id/rest_server/pengajuan/absenmobile/index_data_absen_kbkm?nik_ktp="+nik_baru,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject obj = new JSONObject(response);
                            final JSONArray movieArray = obj.getJSONArray("data");
                            for (int i = 0; i < movieArray.length(); i++) {
                                final JSONObject movieObject = movieArray.getJSONObject(i);
                                final keteranganmodel movieItem = new keteranganmodel(
                                        movieObject.getString("kat"),
                                        movieObject.getString("lokasi"),
                                        movieObject.getString("time"));
                                keteranganmodels.add(movieItem);
                                dialog.dismissWithAnimation();
                            }
                            ListViewAdapter adapter = new ListViewAdapter(keteranganmodels, getApplicationContext());
                            list.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                            Collections.sort(keteranganmodels, new Comparator<keteranganmodel>() {
                                public int compare(keteranganmodel o1, keteranganmodel o2) {
                                    if (o1.getTime() == null || o2.getTime() == null)
                                        return 0;
                                    return o2.getTime().compareTo(o1.getTime());
                                }
                            });


                        } catch(JSONException e){
                            e.printStackTrace();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismissWithAnimation();
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

    private void keluar() {
            String url = "index_gigo";
            StringRequest stringRequest2 = new StringRequest(Request.Method.POST, "https://hrd.tvip.co.id/rest_server/pengajuan/Absenmobile/" + url,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            dialog.dismissWithAnimation();
                            new SweetAlertDialog(absen_mobile.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Data Sudah Diupload")
                                    .setConfirmText("OK")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                            finish();
                                        }
                                    })
                                    .show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.dismissWithAnimation();
                    new SweetAlertDialog(absen_mobile.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Kesalahan Dalam System")
                            .setConfirmText("OK")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .show();
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> params = new HashMap<String, String>();
                    String creds = String.format("%s:%s", "admin", "Databa53");
                    String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                    params.put("Authorization", auth);
                    return params;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);

                    String nik_baru = sharedPreferences.getString(KEY_NIK, null);
                    String nama = menu.txt_nama.getText().toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String tanggal1 = sdf.format(new Date());
                    String langitude = lat.getText().toString();
                    String lon = longitude.getText().toString();

                    params.put("nik", nik_baru);
                    params.put("nama", nama);
                    params.put("kat", "KELUAR");
                    params.put("lokasi", lokasi.getText().toString());
                    params.put("time", tanggal1);
                    params.put("lat", langitude);
                    params.put("lon", lon);

                    return params;
                }

            };
            stringRequest2.setRetryPolicy(
                    new DefaultRetryPolicy(
                            500000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    )
            );
            RequestQueue requestQueue2 = Volley.newRequestQueue(this);
            requestQueue2.add(stringRequest2);

    }

    private void jampulang() {
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, "https://hrd.tvip.co.id/rest_server/pengajuan/Absenmobile/index_manualoutkbkm",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideDialog();
                        Toast.makeText(getApplicationContext(), "sudah di update", Toast.LENGTH_LONG).show();
                        absen_mobile.this.finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "maaf ada kesalahan", Toast.LENGTH_LONG).show();
                    }
                }

        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", "admin", "Databa53");
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            };

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
                String nik_baru = sharedPreferences.getString(KEY_NIK, null);
                Date c = Calendar.getInstance().getTime();
                System.out.println("Current time => " + c);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = df.format(c);

                String untildate= formattedDate;//can take any date in current format
                SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
                Calendar cal = Calendar.getInstance();
                try {
                    cal.setTime( dateFormat.parse(untildate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cal.add( Calendar.DATE, -1 );
                String convertedDate=dateFormat.format(cal.getTime());

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String in_manual = sdf.format(new Date());

                params.put("nik_ktp", nik_baru);
                if(keteranganhadir.getText().toString().equals("Kemarin sudah absen")){
                    params.put("shift_day", formattedDate);
                } else if(keteranganhadir.getText().toString().equals("Absen Hari Ini")){
                    params.put("shift_day", formattedDate);
                } else if(keteranganhadir.getText().toString().equals("Absen Hari Ini")){
                    params.put("shift_day", convertedDate);
                }

                params.put("out", in_manual);

                return params;
            }

        };

        stringRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    private void masuk() {
        pDialog = new ProgressDialog(absen_mobile.this);
        showDialog();
        pDialog.setContentView(R.layout.progress_dialog);
        pDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );

            String url = "index_gigo";
            StringRequest stringRequest2 = new StringRequest(Request.Method.POST, "https://hrd.tvip.co.id/rest_server/pengajuan/Absenmobile/"+url,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            hideDialog();
                            Toast.makeText(getApplicationContext(), "sudah di update", Toast.LENGTH_LONG).show();
                            absen_mobile.this.finish();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideDialog();
                            Toast.makeText(getApplicationContext(), "Maaf ada kesalahan", Toast.LENGTH_LONG).show();
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
                };

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);

                    String nik_baru = sharedPreferences.getString(KEY_NIK ,null);
                    String nama = menu.txt_nama.getText().toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String tanggal1 = sdf.format(new Date());
                    String langitude = lat.getText().toString();
                    String lon = longitude.getText().toString();

                    params.put("nik", nik_baru);
                    params.put("nama",nama);
                    params.put("kat","MASUK");
                    params.put("lokasi", lokasi.getText().toString());
                    params.put("time", tanggal1);
                    params.put("lat",langitude);
                    params.put("lon",lon);

                    return params;
                }



            };
            stringRequest2.setRetryPolicy(
                    new DefaultRetryPolicy(
                            500000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    )
            );
            RequestQueue requestQueue2 = Volley.newRequestQueue(this);
            requestQueue2.add(stringRequest2);
    }

    private void absensimasuk() {
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, "https://hrd.tvip.co.id/rest_server/pengajuan/Absenmobile/index_manualinkbkm",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideDialog();
                        Toast.makeText(getApplicationContext(), "sudah di update", Toast.LENGTH_LONG).show();
                        absen_mobile.this.finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "maaf ada kesalahan", Toast.LENGTH_LONG).show();
                    }
                }

        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", "admin", "Databa53");
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            };

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
                String nik_baru = sharedPreferences.getString(KEY_NIK, null);
                Date c = Calendar.getInstance().getTime();
                System.out.println("Current time => " + c);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = df.format(c);
                
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String in_manual = sdf.format(new Date());

                params.put("nik_ktp", nik_baru);
                params.put("shift_day", formattedDate);
                params.put("in", in_manual);

                return params;
            }

        };

        stringRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public class ListViewAdapter extends ArrayAdapter<keteranganmodel> {

        List<keteranganmodel> movieItemList;

        private Context context;

        public ListViewAdapter(List<keteranganmodel> movieItemList, Context context) {
            super(context, R.layout.list_absensi, movieItemList);
            this.movieItemList = movieItemList;
            this.context = context;
        }

        @SuppressLint("NewApi")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(context);

            View listViewItem = inflater.inflate(R.layout.list_absensi, null, true);


            TextView tanggalid = listViewItem.findViewById(R.id.tanggalid);
            TextView kat = listViewItem.findViewById(R.id.kat);
            TextView jam = listViewItem.findViewById(R.id.jam);
            TextView lokasi = listViewItem.findViewById(R.id.lokasi);

            keteranganmodel movieItem = getItem(position);

            tanggalid.setText(convertFormat(movieItem.getTime()));
            kat.setText(movieItem.getKat());
            jam.setText(jam(movieItem.getTime()));
            lokasi.setText(movieItem.getLokasi());

            return listViewItem;
        }
    }

        public static String tanggal23(String inputDate) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = null;
            try {
                date = simpleDateFormat.parse(inputDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date == null) {
                return "";
            }
            SimpleDateFormat convetDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return convetDateFormat.format(date);
        }

        public static String convertFormat(String inputDate) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = null;
            try {
                date = simpleDateFormat.parse(inputDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date == null) {
                return "";
            }
            SimpleDateFormat convetDateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy");
            return convetDateFormat.format(date);
        }

        public static String jam(String inputDate) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = null;
            try {
                date = simpleDateFormat.parse(inputDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date == null) {
                return "";
            }
            SimpleDateFormat convetDateFormat = new SimpleDateFormat("kk:mm:ss");
            return convetDateFormat.format(date);
        }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
    public static String convertFormat2(String inputDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
        Date date = null;
        try {
            date = simpleDateFormat.parse(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return "";
        }
        SimpleDateFormat convetDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return convetDateFormat.format(date);
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {

        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,5,absen_mobile.this);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            Geocoder geocoder = new Geocoder(absen_mobile.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

            longitude.setText(String.valueOf(location.getLongitude()));
            lat.setText(String.valueOf(location.getLatitude()));


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            if (result.getContents() == null){
                Toast.makeText(this, "Hasil tidak ditemukan", Toast.LENGTH_SHORT).show();
            }else{
                try{
                    JSONObject object = new JSONObject(result.getContents());
                    System.out.println(object.getString("nik"));
                }catch (JSONException e){
                    e.printStackTrace();

                    final String id = result.getContents();
                    dialog = new SweetAlertDialog(absen_mobile.this, SweetAlertDialog.PROGRESS_TYPE);
                    dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    dialog.setTitleText("Harap Menunggu");
                    dialog.show();

                    StringRequest stringRequest2 = new StringRequest(Request.Method.GET, "https://hrd.tvip.co.id/rest_server/master/lokasi/index_kodenikdepo?kode_dms=" + result.getContents(),
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        JSONArray movieArray = obj.getJSONArray("data");

                                        for (int i = 0; i < movieArray.length(); i++) {

                                            JSONObject movieObject = movieArray.getJSONObject(i);

                                            lokasi.setText(movieObject.getString("depo_nama"));

                                            keluar();



                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    dialog.dismissWithAnimation();
                                    new SweetAlertDialog(absen_mobile.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Lokasi Tidak Sesuai")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sDialog) {
                                                    sDialog.dismissWithAnimation();
                                                }
                                            })
                                            .show();
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
                    stringRequest2.setRetryPolicy(
                            new DefaultRetryPolicy(
                                    500000,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                            )
                    );

                    RequestQueue requestQueue2 = Volley.newRequestQueue(this);
                    requestQueue2.add(stringRequest2);
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
