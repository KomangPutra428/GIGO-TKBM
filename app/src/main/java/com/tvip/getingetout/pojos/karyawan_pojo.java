package com.tvip.getingetout.pojos;

public class karyawan_pojo {
    String nik, nama_karyawan_struktur, start_date, end_date;


    public karyawan_pojo(String nik, String nama_karyawan_struktur, String start_date, String end_date) {
        this.nik = nik;
        this.nama_karyawan_struktur = nama_karyawan_struktur;
        this.start_date = start_date;
        this.end_date = end_date;
    }

    public String getNik() { return nik; }
    public String getNama_karyawan_struktur() { return nama_karyawan_struktur; }
    public String getStart_date() { return start_date; }
    public String getEnd_date() { return end_date; }

    @Override
    public String toString() {
        return nama_karyawan_struktur;
    }
}
