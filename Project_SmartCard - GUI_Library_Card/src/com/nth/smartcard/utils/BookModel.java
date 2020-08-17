/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nth.smartcard.utils;

import com.nth.smartcard.model.ItemBook;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Nguyen Tien Hoa
 */
public class BookModel {

    private static List<ItemBook> listBook;

    public BookModel() {

    }

    public static List<ItemBook> getListBook() {
        List<ItemBook> list = new ArrayList<>();
        try {
            File file = new File("files/lib_book.txt");
//            FileReader fileReader = new FileReader(file);
            BufferedReader Buffreader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String strline;
            int check = 0;
            while ((strline = Buffreader.readLine()) != null) {
                ItemBook item = new ItemBook();
                StringTokenizer token = new StringTokenizer(strline, "|");
                if (check != 0) {
                    item.setIdBook(token.nextToken());
                    item.setNameBook(token.nextToken());
                    item.setLoaiSach(token.nextToken());
                    item.setNxb(token.nextToken());
                    list.add(item);
                }
                check++;
            }

            for (int i = 0; i < list.size(); i++) {
                System.out.println("Name + " + list.get(i).getNameBook());
            }

        } catch (Exception e) {
            System.out.println("Lỗi : " + e);
        }
        return list;
    }


}
