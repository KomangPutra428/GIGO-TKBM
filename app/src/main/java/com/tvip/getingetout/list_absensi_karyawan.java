package com.tvip.getingetout;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class list_absensi_karyawan extends AppCompatActivity {
    ListView list_absensi;
    List<keteranganmodel> keteranganmodels = new ArrayList<>();
    ListViewAdapterAbsensi adapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_absensi_karyawan);
        HttpsTrustManager.allowAllSSL();
        list_absensi = findViewById(R.id.list_absensi);

        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
        String nik_baru = sharedPreferences.getString(KEY_NIK, null);

        final SweetAlertDialog pDialog = new SweetAlertDialog(list_absensi_karyawan.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Harap Menunggu");
        pDialog.show();
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
                            }
                            pDialog.cancel();
                            adapter = new ListViewAdapterAbsensi(keteranganmodels, getApplicationContext());
                            list_absensi.setAdapter(adapter);
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
                        pDialog.cancel();
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

    public class ListViewAdapterAbsensi extends ArrayAdapter<keteranganmodel> {

        List<keteranganmodel> movieItemList;

        private Context context;

        public ListViewAdapterAbsensi(List<keteranganmodel> movieItemList, Context context) {
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
}