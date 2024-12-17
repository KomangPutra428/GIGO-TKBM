package com.tvip.getingetout;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.tvip.getingetout.pojos.karyawan_pojo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class list_karyawan extends AppCompatActivity {
    ListView list_karyawan;
    SharedPreferences sharedPreferences;
    List<karyawan_pojo> karyawanPojos = new ArrayList<>();
    ListViewAdapterKaryawan adapter;
    MaterialToolbar detailAbsen_Bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_karyawan);
        HttpsTrustManager.allowAllSSL();
        list_karyawan = findViewById(R.id.list_karyawan);

        detailAbsen_Bar = (MaterialToolbar) findViewById(R.id.detailAbsen_Bar);
        setSupportActionBar(detailAbsen_Bar);

        list_karyawan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getBaseContext(), list_absensi_karyawan.class);
                String nik = ((karyawan_pojo) parent.getItemAtPosition(position)).getNik();
                i.putExtra("nik", nik);
                startActivity(i);

            }
        });

        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
        String lokasi = sharedPreferences.getString("lokasi", null);

        final SweetAlertDialog pDialog = new SweetAlertDialog(list_karyawan.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Harap Menunggu");
        pDialog.show();
        final StringRequest stringRequest1 = new StringRequest(Request.Method.GET, "https://hrd.tvip.co.id/rest_server/pengajuan/absenmobile/index_karyawantkbm?lokasi="+lokasi,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            final JSONObject object = new JSONObject(response);
                            JSONArray teamArray = object.getJSONArray("data");

                            JSONObject teamObject = null;
                            for (int i = 0; i < teamArray.length(); i++) {

                                teamObject = teamArray.getJSONObject(i);

                                final karyawan_pojo teamItem = new karyawan_pojo(
                                        teamObject.getString("nik"),
                                        teamObject.getString("nama_karyawan_struktur"),
                                        teamObject.getString("start_date"),
                                        teamObject.getString("end_date"));
                                karyawanPojos.add(teamItem);

                                adapter = new ListViewAdapterKaryawan(karyawanPojos, getApplicationContext());
                                list_karyawan.setAdapter(adapter);



                            }


                            Collections.sort(karyawanPojos, new Comparator<karyawan_pojo>() {
                                public int compare(karyawan_pojo o1, karyawan_pojo o2) {
                                    if (o1.getNama_karyawan_struktur() == null || o2.getNama_karyawan_struktur() == null)
                                        return 0;
                                    return o1.getNama_karyawan_struktur().compareTo(o2.getNama_karyawan_struktur());
                                }
                            });

                            pDialog.cancel();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.cancel();
//                        new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE)
//                                .setContentText("Anda Tidak Punya Anggota")
//                                .setConfirmText("OK")
//                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                    @Override
//                                    public void onClick(SweetAlertDialog sDialog) {
//                                        sDialog.dismissWithAnimation();
//                                        finish();
//                                    }
//                                })
//                                .show();
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
        stringRequest1.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        stringRequest1.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest1);


    }

    public static class ListViewAdapterKaryawan extends ArrayAdapter<karyawan_pojo> {

        public List<karyawan_pojo> karyawanPojos;
        private Context context;

        public ListViewAdapterKaryawan(List<karyawan_pojo> karyawanPojos, Context context) {
            super(context, R.layout.list_karyawan, karyawanPojos);
            this.karyawanPojos = karyawanPojos;
            this.context = context;

        }

        @SuppressLint("NewApi")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(context);

            View listViewItem = inflater.inflate(R.layout.list_karyawan, null, true);

            TextView niknama = listViewItem.findViewById(R.id.niknama);
            TextView tanggalawal = listViewItem.findViewById(R.id.tanggalawal);
            TextView tanggalakhir = listViewItem.findViewById(R.id.tanggalakhir);


            final karyawan_pojo movieItem = getItem(position);

            niknama.setText(movieItem.getNik() +" | "+movieItem.getNama_karyawan_struktur());
            tanggalawal.setText(tanggal(movieItem.getStart_date()));
            tanggalakhir.setText(tanggal(movieItem.getEnd_date()));


            return listViewItem;
        }
    }

    private static String tanggal(String inputDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = simpleDateFormat.parse(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return "";
        }
        SimpleDateFormat convetDateFormat = new SimpleDateFormat("dd MMMM yyyy");
        return convetDateFormat.format(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        int id = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.BLACK);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String nextText) {
                adapter.getFilter().filter(nextText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);

    }
}