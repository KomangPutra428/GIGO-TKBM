package com.tvip.getingetout.pojos;

public class absen_gigo_pojo {
    String nama_karyawan_struktur, shift_day, ket_absensi, F1, F4, longlat_f1, longlat_f4, waktu_masuk, waktu_keluar;


    public absen_gigo_pojo(String nama_karyawan_struktur, String shift_day, String ket_absensi, String F1, String F4, String longlat_f1, String longlat_f4, String waktu_masuk, String waktu_keluar) {
        this.nama_karyawan_struktur = nama_karyawan_struktur;
        this.shift_day = shift_day;
        this.ket_absensi = ket_absensi;

        this.F1 = F1;
        this.F4 = F4;

        this.longlat_f1 = longlat_f1;
        this.longlat_f4 = longlat_f4;

        this.waktu_masuk = waktu_masuk;
        this.waktu_keluar = waktu_keluar;
    }

    public String getNama_karyawan_struktur() {
        return nama_karyawan_struktur;
    }

    public String getShift_day() {
        return shift_day;
    }

    public String getKet_absensi() {
        return ket_absensi;
    }

    public String getF1() {
        return F1;
    }

    public String getF4() {
        return F4;
    }

    public String getLonglat_f1() {
        return longlat_f1;
    }

    public String getLonglat_f4() {
        return longlat_f4;
    }

    public String getWaktu_masuk() {
        return waktu_masuk;
    }

    public String getWaktu_keluar() {
        return waktu_keluar;
    }
}
