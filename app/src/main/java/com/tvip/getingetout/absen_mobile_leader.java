package com.tvip.getingetout;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.tvip.getingetout.MainActivity.KEY_NIK;
import static com.tvip.getingetout.menu.keterangan;
import static com.tvip.getingetout.menu.txt_nama;

public class absen_mobile_leader extends AppCompatActivity implements LocationListener {
    AutoCompleteTextView nik;
    EditText karyawan, lokasi;
    TextView longitude, lat;

    // kamera //
    MaterialCardView kamera;
    ImageView uploadkamera;
    TextView textkamera;

    Uri outPutfileUri;

    ContentValues cv;
    Uri imageUri;


    LocationManager locationManager;

    Bitmap bitmap;
    ArrayList<String> nikkaryawan = new ArrayList<>();

    SharedPreferences sharedPreferences;

    Button batal, simpan;

    SweetAlertDialog pDialog;

    TextView jam, depotanggal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absen_mobile_leader);
        nik = findViewById(R.id.nik);
        karyawan = findViewById(R.id.karyawan);
        // deletePictures();


        jam = findViewById(R.id.jam);
        depotanggal = findViewById(R.id.depotanggal);
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

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        String formattedDate = df.format(c);






        getKaryawan();


        batal = findViewById(R.id.batal);
        simpan = findViewById(R.id.simpan);


        simpan.setText("Check In");

        longitude = findViewById(R.id.longitude);
        lat = findViewById(R.id.lat);

        lokasi = findViewById(R.id.lokasi);

        kamera = findViewById(R.id.kamera);
        uploadkamera = findViewById(R.id.uploadgambar2);
        textkamera = findViewById(R.id.textupload2);

        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
        String nik_baru = sharedPreferences.getString(KEY_NIK, null);

        depotanggal.setText("Depo, " + nik_baru + " • " + formattedDate);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://hrd.tvip.co.id/rest_server/pengajuan/absenmobile/index_nik_tkbm?nik_baru=" + nik_baru,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray movieArray = obj.getJSONArray("data");

                            for (int i = 0; i < movieArray.length(); i++) {

                                JSONObject movieObject = movieArray.getJSONObject(i);
                                karyawan.setText(movieObject.getString("nama_karyawan_struktur"));
                                lokasi.setText(movieObject.getString("lokasi"));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        karyawan.setText("");
                        lokasi.setText("");
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
        RequestQueue requestQueue = Volley.newRequestQueue(absen_mobile_leader.this);
        requestQueue.add(stringRequest);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        batal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getLocation();

        simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(longitude.getText().toString().equals("long") && lat.getText().toString().equals("lat")){
                    new SweetAlertDialog(absen_mobile_leader.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Lokasi Masih Belum Ditemukan")
                            .setConfirmText("OK")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .show();
                } else if (nik.getText().toString().length() == 0) {
                    nik.setError("Isi NIK");
                } else if (karyawan.getText().toString().length() == 0) {
                    karyawan.setError("Karyawan Kosong");
                    nik.getText().clear();
                } else if (lokasi.getText().toString().length() == 0) {
                    lokasi.setError("Lokasi Kosong");
                    nik.getText().clear();
                } else if (textkamera.getVisibility() == View.VISIBLE){
                    new SweetAlertDialog(absen_mobile_leader.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Foto Masih Kosong")
                            .setConfirmText("OK")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .show();
                } else {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(absen_mobile_leader.this);
                    alertDialogBuilder.setTitle("Apakah data sudah benar? (Absen Masuk)");
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                public void onClick(DialogInterface dialog, int id) {
                                    pDialog = new SweetAlertDialog(absen_mobile_leader.this, SweetAlertDialog.PROGRESS_TYPE);
                                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                                    pDialog.setTitleText("Harap Menunggu");
                                    pDialog.setCancelable(false);
                                    pDialog.show();

                                    postmasuk();


                                    dialog.cancel();
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
            }
        });

        nik.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    nik.showDropDown();
                }
            }
        });

        nik.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                nik.showDropDown();
                return false;
            }
        });

        nik.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(nik.getText().toString().length() == 0){

                } else {

                    String nikbaru = nik.getText().toString();
                    String[] nik_baru = nikbaru.split(" \\(");
                    nikbaru = nik_baru[0];
                    String nik_new = nikbaru.replace("(", "");

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://hrd.tvip.co.id/rest_server/pengajuan/absenmobile/index_nik_tkbm?nik_baru=" + nik_new.trim(),
                            new com.android.volley.Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {

                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        JSONArray movieArray = obj.getJSONArray("data");

                                        for (int i = 0; i < movieArray.length(); i++) {

                                            JSONObject movieObject = movieArray.getJSONObject(i);
                                            karyawan.setText(movieObject.getString("nama_karyawan_struktur"));
                                            lokasi.setText(movieObject.getString("lokasi"));

//                                            nik.setFocusable(false);
//                                            nik.setLongClickable(false);

                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    karyawan.getText().clear();
                                    lokasi.getText().clear();
//                                    nik.setFocusable(true);
//                                    nik.setLongClickable(true);


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
                    RequestQueue requestQueue = Volley.newRequestQueue(absen_mobile_leader.this);
                    requestQueue.add(stringRequest);
                }
            }
        });

        kamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                cv = new ContentValues();
                cv.put(MediaStore.Images.Media.TITLE, "My Picture");
                cv.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 1);


                //openFrontFacingCameraGingerbread();

//                Intent camera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                camera.putExtra("android.intent.extras.CAMERA_FACING", 1); // used this line of code to start the intent of camera
//                //The ID is 1 that opens FRONT CAMERA of a device
//                imagePath = getCapturedImageExternal();
//                destImageFile = new File(imagePath);
//                camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destImageFile));
//
//                this.cordova.setActivityResultCallback(PicturePlugin.this);
//                cordova.getActivity().startActivityForResult(camera,IMAGE_TAKEN);

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with deleting the image
                deleteImage(imageUri);
            } else {
                // Permission denied, handle this case accordingly
            }
        }
    }

    private void deletePictures() {
        String[] projection = {MediaStore.Images.Media._ID};
        Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,null, null);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            deleteImage(deleteUri);
        }
        cursor.close();
    }

    private void deleteImage(Uri imageUri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            int deletedRows = contentResolver.delete(imageUri, null, null);
            if (deletedRows > 0) {
                // Image deletion was successful
            } else {
                // Image deletion failed or the image doesn't exist
            }
        } catch (SecurityException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (e instanceof RecoverableSecurityException) {
                    RecoverableSecurityException rse = (RecoverableSecurityException) e;
                    IntentSender intentSender = rse.getUserAction().getActionIntent().getIntentSender();
                    try {
                        startIntentSenderForResult(intentSender, 1,
                                null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                } else {
                    e.printStackTrace();
                    // Other security exceptions or errors occurred, handle them accordingly
                }
            }
        }
    }

    private void getKaryawan() {
        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
        String location = sharedPreferences.getString(KEY_NIK, null);

        StringRequest kecamatan = new StringRequest(Request.Method.GET, "https://hrd.tvip.co.id/rest_server/pengajuan/absenmobile/index_karyawantkbm?lokasi=" + location,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("true")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String nik_baru = jsonObject1.getString("nik");
                                    String nama_karyawan_struktur = jsonObject1.getString("nama_karyawan_struktur");

                                    nikkaryawan.add(nik_baru +  " (" + nama_karyawan_struktur + ")");

                                }
                            }
                            nik.setAdapter(new ArrayAdapter<String>(absen_mobile_leader.this, android.R.layout.simple_expandable_list_item_1, nikkaryawan));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

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
        kecamatan.setRetryPolicy(new DefaultRetryPolicy(
                500000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestkota = Volley.newRequestQueue(absen_mobile_leader.this);
        requestkota.add(kecamatan);
    }

    private void postkeluar() {
        pDialog = new SweetAlertDialog(absen_mobile_leader.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Harap Menunggu");
        pDialog.setCancelable(false);
        pDialog.show();

        String url = "index_gigo_leader";
        StringRequest stringRequest2 = new StringRequest(Request.Method.POST, "https://hrd.tvip.co.id/rest_server/pengajuan/Absenmobile/"+url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        fototkbm();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismissWithAnimation();
                        new SweetAlertDialog(absen_mobile_leader.this, SweetAlertDialog.WARNING_TYPE)
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

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                String tanggal1 = sdf.format(new Date());
                String langitude = lat.getText().toString();
                String lon = longitude.getText().toString();

                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String tanggal2 = sdf2.format(new Date());

                String nikbaru = nik.getText().toString();
                String[] nik_baru = nikbaru.split(" \\(");
                nikbaru = nik_baru[0];
                String nik_new = nikbaru.replace("(", "");

                params.put("nik", nik_new);
                params.put("nama", karyawan.getText().toString());
                params.put("kat","KELUAR");
                params.put("lokasi", lokasi.getText().toString());
                params.put("time", tanggal2);
                params.put("lat",langitude);
                params.put("lon",lon);
                params.put("dokumen", tanggal1 + "_" + nik_new + "_Keluar.jpeg");

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

    private void postmasuk() {


        String url = "index_gigo_leader";
        StringRequest stringRequest2 = new StringRequest(Request.Method.POST, "https://hrd.tvip.co.id/rest_server/pengajuan/Absenmobile/"+url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        fototkbm();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismissWithAnimation();
                        new SweetAlertDialog(absen_mobile_leader.this, SweetAlertDialog.WARNING_TYPE)
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

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                String tanggal1 = sdf.format(new Date());
                String langitude = lat.getText().toString();
                String lon = longitude.getText().toString();

                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String tanggal2 = sdf2.format(new Date());

                String nikbaru = nik.getText().toString();
                String[] nik_baru = nikbaru.split(" \\(");
                nikbaru = nik_baru[0];
                String nik_new = nikbaru.replace("(", "");

                params.put("nik", nik_new);
                params.put("nama", karyawan.getText().toString());
                params.put("kat","MASUK");
                params.put("lokasi", lokasi.getText().toString());
                params.put("time", tanggal2);
                params.put("lat",langitude);
                params.put("lon",lon);
                params.put("dokumen", tanggal1 + "_" + nik_new + "_Masuk.jpeg");

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

    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx<cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("Your_TAG", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        return cam;
    }

    private void fototkbm() {
        StringRequest stringRequest2 = new StringRequest(Request.Method.POST, "https://hrd.tvip.co.id/mobile_eis_2/upload_tkbm.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            String status = json.getString("response");
                            if (status.contains("Success")) {
                                pDialog.cancel();
                                new SweetAlertDialog(absen_mobile_leader.this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Data Sudah Diupload")
                                        .setConfirmText("OK")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                sDialog.dismissWithAnimation();
                                                nik.getText().clear();
                                                karyawan.getText().clear();
                                                lokasi.getText().clear();
                                                lokasi.setText("");

                                                textkamera.setVisibility(View.VISIBLE);
                                                uploadkamera.setImageResource(R.drawable.icon_kamera);
                                                finish();
                                            }
                                        })
                                        .show();
                            } else if (status.contains("Image not uploaded")){
                                pDialog.cancel();
                                new SweetAlertDialog(absen_mobile_leader.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Kesalahan Dalam Sistem")
                                        .setConfirmText("OK")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                sDialog.dismissWithAnimation();
                                            }
                                        })
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.cancel();
                        new SweetAlertDialog(absen_mobile_leader.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Kesalahan Dalam Sistem")
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
                String gambar = ImageToString(bitmap);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                String tanggal1 = sdf.format(new Date());

                String nikbaru = nik.getText().toString();
                String[] nik_baru = nikbaru.split(" \\(");
                nikbaru = nik_baru[0];
                String nik_new = nikbaru.replace("(", "");


                params.put("nama_foto", tanggal1 + "_" + nik_new.trim() + "_Masuk");
                params.put("foto", gambar);


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

        RequestQueue requestQueue2 = Volley.newRequestQueue(absen_mobile_leader.this);
        requestQueue2.add(stringRequest2);
    }

    private String ImageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imageType = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageType, Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1){
            if (resultCode == Activity.RESULT_OK) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);
                    uploadkamera.setImageBitmap(bitmap);
                    textkamera.setVisibility(View.GONE);

                    ViewGroup.LayoutParams paramktp = uploadkamera.getLayoutParams();

                    double sizeInDP = 226;
                    int marginInDp = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, (float) sizeInDP, getResources()
                                    .getDisplayMetrics());

                    double sizeInDP2 = 226;
                    int marginInDp2 = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, (float) sizeInDP2, getResources()
                                    .getDisplayMetrics());

                    paramktp.width = marginInDp;
                    paramktp.height = marginInDp2;
                    uploadkamera.setLayoutParams(paramktp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {

        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,5,absen_mobile_leader.this);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            Geocoder geocoder = new Geocoder(absen_mobile_leader.this, Locale.getDefault());
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
}