/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nth.smartcard.model;

/**
 *
 * @author Nguyen Tien Hoa
 */
public class Infomation {
    private String fullName;
    private String khoa;
    private String lop;
    private String maSV;
    private byte[] avata;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getKhoa() {
        return khoa;
    }

    public void setKhoa(String khoa) {
        this.khoa = khoa;
    }

    public String getLop() {
        return lop;
    }

    public void setLop(String lop) {
        this.lop = lop;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public byte[] getAvata() {
        return avata;
    }

    public void setAvata(byte[] avata) {
        this.avata = avata;
    }

    public Infomation() {
    }

    
    public Infomation(String fullName, String khoa, String lop, String maSV, byte[] avata) {
        this.fullName = fullName;
        this.khoa = khoa;
        this.lop = lop;
        this.maSV = maSV;
        this.avata = avata;
    }
    
    
}
