package com.tvip.getingetout;

import static com.tvip.getingetout.MainActivity.KEY_NIK;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tvip.getingetout.pojos.absen_gigo_pojo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class list_shifting extends AppCompatActivity {

    ListView list_absen;
    ImageButton filter;
    TextView nik, nama_nik, tanggalPeriode;

    List<absen_gigo_pojo> absenGigoPojos;

    ListViewAdapterAbsenGigo adapter;

    SharedPreferences sharedPreferences;

    private Calendar date;
    private SimpleDateFormat dateFormatter;

    SweetAlertDialog pDialog;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_shifting);
        tanggalPeriode = findViewById(R.id.tanggalPeriode);


        filter = findViewById(R.id.filter);
        nik = findViewById(R.id.nik);

        nama_nik = findViewById(R.id.nama_nik);
        list_absen = findViewById(R.id.list_absen);

        nik.setText(menu.txt_nik.getText().toString() + " • " +  menu.text_jabatan.getText().toString());
        nama_nik.setText(menu.txt_nama.getText().toString());

        absenGigoPojos = new ArrayList<>();


        dateFormatter = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog filter = new Dialog(list_shifting.this);
                filter.setContentView(R.layout.filtering);
                filter.setCancelable(false);

                filter.show();

                EditText mulai_tanggal = filter.findViewById(R.id.mulai_tanggal);
                EditText sampai_tanggal = filter.findViewById(R.id.sampai_tanggal);

                Button batal = filter.findViewById(R.id.batal);
                Button lanjutkan = filter.findViewById(R.id.lanjutkan);

                mulai_tanggal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar currentDate = Calendar.getInstance();
                        Calendar twoDaysAgo = (Calendar) currentDate.clone();

                        date = currentDate.getInstance();

                        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                                date.set(year, monthOfYear, dayOfMonth);

                                mulai_tanggal.setText(dateFormatter.format(date.getTime()));
                            }
                        };
                        DatePickerDialog datePickerDialog = new DatePickerDialog(list_shifting.this, dateSetListener, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.show();
                    }
                });

                sampai_tanggal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar currentDate = Calendar.getInstance();

                        date = currentDate.getInstance();

                        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                                date.set(year, monthOfYear, dayOfMonth);

                                sampai_tanggal.setText(dateFormatter.format(date.getTime()));
                            }
                        };
                        DatePickerDialog datePickerDialog = new DatePickerDialog(list_shifting.this, dateSetListener, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.show();
                    }
                });

                batal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        filter.dismiss();
                    }
                });

                lanjutkan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mulai_tanggal.getText().toString().length() == 0){
                            mulai_tanggal.setError("Isi Tanggal Awal");
                        } else {
                            if(sampai_tanggal.getText().toString().length() ==0){
                                filter.dismiss();
                                tanggalPeriode.setText(convertFormat(mulai_tanggal.getText().toString()));
                                getAbsen(mulai_tanggal.getText().toString(), mulai_tanggal.getText().toString());
                            } else {
                                tanggalPeriode.setText(convertFormat(mulai_tanggal.getText().toString()) + " - " + convertFormat(sampai_tanggal.getText().toString()));
                                getAbsen(mulai_tanggal.getText().toString(), sampai_tanggal.getText().toString());
                            }
                            filter.dismiss();
                        }
                    }
                });

            }
        });
    }

    private void getAbsen(String mulai_tanggal, String sampai_tanggal) {
        pDialog = new SweetAlertDialog(list_shifting.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Harap Menunggu");
        pDialog.show();
        pDialog.setCancelable(false);

        absenGigoPojos.clear();

        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
        String nik_baru = sharedPreferences.getString(KEY_NIK, null);


        StringRequest stringRequest = new StringRequest(Request.Method.GET,"https://hrd.tvip.co.id/rest_server/pengajuan/Absenmobile/index_cek_absengigo?tanggal1="+convertFormat2(mulai_tanggal)+"&tanggal2="+convertFormat2(sampai_tanggal)+"&nik=" + nik_baru,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.dismissWithAnimation();
                        list_absen.setVisibility(View.VISIBLE);
                        try {
                            int number = 0;
                            JSONObject obj = new JSONObject(response);
                            final JSONArray movieArray = obj.getJSONArray("data");
                            for (int i = 0; i < movieArray.length(); i++) {
                                final JSONObject movieObject = movieArray.getJSONObject(i);
                                final absen_gigo_pojo movieItem = new absen_gigo_pojo(
                                        movieObject.getString("nama_karyawan_struktur"),
                                        movieObject.getString("shift_day"),
                                        movieObject.getString("ket_absensi"),
                                        movieObject.getString("F1"),
                                        movieObject.getString("F4"),
                                        movieObject.getString("longlat_f1"),
                                        movieObject.getString("longlat_f4"),
                                        movieObject.getString("waktu_masuk"),
                                        movieObject.getString("waktu_keluar"));

                                absenGigoPojos.add(movieItem);

                                adapter = new ListViewAdapterAbsenGigo(absenGigoPojos, getApplicationContext());
                                list_absen.setAdapter(adapter);
                                adapter.notifyDataSetChanged();

                            }


                        } catch(JSONException e){
                            e.printStackTrace();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismissWithAnimation();
                        list_absen.setVisibility(View.GONE);

                        new SweetAlertDialog(list_shifting.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Data Tidak Ditemukan")
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

        stringRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                ));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public class ListViewAdapterAbsenGigo extends ArrayAdapter<absen_gigo_pojo> {

        private class ViewHolder {
            TextView namakaryawan, tanggal, masuk, keluar;
            ImageView down, up;
            LinearLayout detailabsensi;

            TextView lokasi_checkin, lokasi_checkout;
          ImageView image;


        }

        private Context context;

        List<absen_gigo_pojo> absenGigoPojos;


        public ListViewAdapterAbsenGigo(List<absen_gigo_pojo> absenGigoPojos, Context context) {
            super(context, R.layout.listabsensi, absenGigoPojos);
            this.absenGigoPojos = absenGigoPojos;
            this.context = context;

        }

        public int getCount() {
            return absenGigoPojos.size();
        }

        public absen_gigo_pojo getItem(int position) {
            return absenGigoPojos.get(position);
        }

        @Override
        public int getViewTypeCount() {
            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public long getItemId(int position) {
            return 0;
        }


        @SuppressLint("NewApi")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ListViewAdapterAbsenGigo.ViewHolder viewHolder;
            absen_gigo_pojo movieItem = absenGigoPojos.get(position);
            if (convertView == null) {
                viewHolder = new ListViewAdapterAbsenGigo.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());

                convertView = inflater.inflate(R.layout.listabsensi, parent, false);

                viewHolder.namakaryawan = (TextView) convertView.findViewById(R.id.namakaryawan);
                viewHolder.tanggal = (TextView) convertView.findViewById(R.id.tanggal);
                viewHolder.down = (ImageView) convertView.findViewById(R.id.down);
                viewHolder.up = (ImageView) convertView.findViewById(R.id.up);
                viewHolder.masuk = (TextView) convertView.findViewById(R.id.masuk);
                viewHolder.keluar = (TextView) convertView.findViewById(R.id.keluar);

                viewHolder.lokasi_checkin = (TextView) convertView.findViewById(R.id.lokasi_checkin);
                viewHolder.lokasi_checkout = (TextView) convertView.findViewById(R.id.lokasi_checkout);

                viewHolder.detailabsensi = (LinearLayout) convertView.findViewById(R.id.detailabsensi);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.image);


//                viewHolder.listproduk = (ListView) convertView.findViewById(R.id.listproduk);
//                viewHolder.no_bkb = (TextView) convertView.findViewById(R.id.no_bkb);


                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ListViewAdapterAbsenGigo.ViewHolder) convertView.getTag();
            }

            viewHolder.namakaryawan.setText(movieItem.getNama_karyawan_struktur());
            viewHolder.namakaryawan.setVisibility(View.GONE);
            viewHolder.down.setVisibility(View.GONE);

            viewHolder.image.setImageResource(R.drawable.work_shift);

            if(movieItem.getLonglat_f1().equals("null")){
                viewHolder.lokasi_checkin.setText("-");
            } else {
                viewHolder.lokasi_checkin.setText(movieItem.getLonglat_f1());
            }

            if(movieItem.getLonglat_f4().equals("null")){
                viewHolder.lokasi_checkout.setText("-");
            } else {
                viewHolder.lokasi_checkout.setText(movieItem.getLonglat_f4());
            }

            viewHolder.tanggal.setTextColor(Color.parseColor("#0A0A0A"));


            viewHolder.tanggal.setText(convertFormatHour(movieItem.getWaktu_masuk()) + " - " + convertFormatHour(movieItem.getWaktu_keluar()) +  " • " + convertFormatdate(movieItem.getShift_day()));

            viewHolder.down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.down.setVisibility(View.GONE);
                    viewHolder.up.setVisibility(View.VISIBLE);
                    viewHolder.detailabsensi.setVisibility(View.VISIBLE);
                }
            });

            viewHolder.up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.down.setVisibility(View.VISIBLE);
                    viewHolder.up.setVisibility(View.GONE);
                    viewHolder.detailabsensi.setVisibility(View.GONE);
                }
            });

            viewHolder.masuk.setText(convertFormatHour(movieItem.getF1()));
            viewHolder.keluar.setText(convertFormatHour(movieItem.getF4()));
//

            return convertView;
        }
    }

    public static String convertFormat(String inputDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");
        Date date = null;
        try {
            date = simpleDateFormat.parse(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return "";
        }
        SimpleDateFormat convetDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return convetDateFormat.format(date);
    }

    public static String convertFormat2(String inputDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");
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

    public static String convertFormatdate(String inputDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return "";
        }
        SimpleDateFormat convetDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return convetDateFormat.format(date);
    }

    public static String convertFormatHour(String inputDate) {
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
        SimpleDateFormat convetDateFormat = new SimpleDateFormat("HH:mm");
        return convetDateFormat.format(date);
    }
}