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
public class ItemBookReturn {

    private String idBook;
    private String nameBook;
    private String loaiSach;
    private String ngayMuon;

    public String getIdBook() {
        return idBook;
    }

    public void setIdBook(String idBook) {
        this.idBook = idBook;
    }

    public String getNameBook() {
        return nameBook;
    }

    public void setNameBook(String nameBook) {
        this.nameBook = nameBook;
    }

    public String getLoaiSach() {
        return loaiSach;
    }

    public void setLoaiSach(String loaiSach) {
        this.loaiSach = loaiSach;
    }

    public String getNgayMuon() {
        return ngayMuon;
    }

    public void setNgayMuon(String ngayMuon) {
        this.ngayMuon = ngayMuon;
    }

    public ItemBookReturn(String idBook, String nameBook, String loaiSach, String ngayMuon) {
        this.idBook = idBook;
        this.nameBook = nameBook;
        this.loaiSach = loaiSach;
        this.ngayMuon = ngayMuon;
    }

    public ItemBookReturn() {
    }
    
}
