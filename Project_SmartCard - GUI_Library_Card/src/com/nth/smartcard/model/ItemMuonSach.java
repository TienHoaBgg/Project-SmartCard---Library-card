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
public class ItemMuonSach {
    private String idBook;
    private String ngayMuon;

    public String getIdBook() {
        return idBook;
    }

    public void setIdBook(String idBook) {
        this.idBook = idBook;
    }

    public String getNgayMuon() {
        return ngayMuon;
    }

    public void setNgayMuon(String ngayMuon) {
        this.ngayMuon = ngayMuon;
    }

    public ItemMuonSach(String idBook, String ngayMuon) {
        this.idBook = idBook;
        this.ngayMuon = ngayMuon;
    }

    public ItemMuonSach() {
    }
    
}
