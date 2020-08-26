/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nth.smartcard.utils;

import com.nth.smartcard.model.Infomation;
import com.nth.smartcard.model.ItemMuonSach;
import java.awt.Image;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 *
 * @author Nguyen Tien Hoa
 */
public class SmartCardWork {

    public static final byte[] AID_APPLET = {0x11, 0x22, 0x33, 0x44, 0x55, 0x01, 0x01};
    private Card card;
    private TerminalFactory factory;
    private CardChannel channel;
    private CardTerminal terminal;
    private List<CardTerminal> terminals;
    private ResponseAPDU response;
    public static int verifyFalse = 0;
    public static boolean checkVeriRSA = false;

    public SmartCardWork() {

    }

    public static void main(String[] args) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter a password:");
        JPasswordField pass = new JPasswordField(10);
        panel.add(label);
        panel.add(pass);
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, "The title",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[1]);
        if (option == 0) // pressing OK button
        {
            char[] password = pass.getPassword();
            System.out.println("Your password is: " + new String(password));
        }

//        JPasswordField pf = new JPasswordField();
//        int okCxl = JOptionPane.showConfirmDialog(null, pf, "Enter Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//
//        if (okCxl == JOptionPane.OK_OPTION) {
//            String password = new String(pf.getPassword());
//            System.err.println("You entered: " + password);
//        }
    }

    public boolean connectCard() {
        try {
            factory = TerminalFactory.getDefault();
            terminals = factory.terminals().list();
            terminal = terminals.get(0);
            card = terminal.connect("T=0");
            channel = card.getBasicChannel();
            if (channel == null) {
                return false;
            }
            response = channel.transmit(new CommandAPDU(0x00, (byte) 0xA4, 0x04, 0x00, AID_APPLET));
            String check = Integer.toHexString(response.getSW());
            if (check.equals("9000")) {
//                JOptionPane.showMessageDialog(null, "Connect thành công ");
                verifyFalse = 0;
                return true;
            } else if (check.equals("6999")) {
                JOptionPane.showMessageDialog(null, "Thẻ đã bị vô hiệu hóa");
                verifyFalse = 3;
                return true;
            } else {
//                JOptionPane.showMessageDialog(null, "Connect không thành công");
                return false;
            }
        } catch (Exception ex) {

//            JOptionPane.showMessageDialog(null, "Đã có lỗi xảy ra");
        }
        return false;
    }

    public boolean disconnect() {
        try {
            card.disconnect(false);
            SmartCardWork.checkVeriRSA = false;
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi :" + e);
        }
        return false;
    }

    public boolean selectApplet() {
        try {
            response = channel.transmit(new CommandAPDU(0x00, (byte) 0xA4, 0x04, 0x00, AID_APPLET));
            String check = Integer.toHexString(response.getSW());
            if (check.equals("9000")) {
//                JOptionPane.showMessageDialog(null, "Connect thành công ");
                return true;
            } else {
//                JOptionPane.showMessageDialog(null, "Connect không thành công");
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean muonSach(List<ItemMuonSach> list) {
        try {
            StringBuilder bulde = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                bulde.append(list.get(i).getIdBook());
                bulde.append("#");
                bulde.append(list.get(i).getNgayMuon());
                bulde.append("@");
            }
            String book = SmartCardWork.stringToHex(bulde.toString());
            byte[] dataBook = SmartCardWork.hexStringToByteArray(book);
            if (dataBook.length > 198) {
                JOptionPane.showMessageDialog(null, "Vượt quá số lượng tài liệu được mượn ");
            } else {
                response = channel.transmit(new CommandAPDU((byte) 0x00, (byte) 0x13, (byte) 0x12, (byte) 0x12, dataBook));
                String check = Integer.toHexString(response.getSW());
                if (checkVerify(check)) {
                    if (check.equals("9000")) {
//                        JOptionPane.showMessageDialog(null, "Mượn tài liệu thành công ");
                        return true;
                    } else {
//                        JOptionPane.showMessageDialog(null, "Không mượn được tài liệu");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Đã có lỗi xảy ra");
        }

        return false;
    }

    public List<ItemMuonSach> getListSach() {
        List<ItemMuonSach> list = new ArrayList<>();
        try {
            response = channel.transmit(new CommandAPDU((byte) 0x00, (byte) 0x14, (byte) 0x26, (byte) 0x04));
            String check = Integer.toHexString(response.getSW());
            if (checkVerify(check)) {
                byte[] dataText = response.getData();
                if (dataText.length != 0) {
                    StringBuilder infoText = new StringBuilder();
                    for (int i = 0; i < dataText.length; i++) {
                        if (dataText[i] != 0) {
                            infoText.append(Character.toString((char) dataText[i]));
                        }
                    }
                    String dTemp = infoText.toString();
                    String[] items = dTemp.split("@");
                    for (int i = 0; i < items.length; i++) {
                        ItemMuonSach itemBook = new ItemMuonSach();
                        String temp = items[i];
                        String[] temps = temp.split("#");
                        itemBook.setIdBook(temps[0]);
                        itemBook.setNgayMuon(temps[1]);
                        list.add(itemBook);
                    }
                    for (int i = 0; i < list.size(); i++) {
                        System.out.println("ID: " + list.get(i).getIdBook());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            JOptionPane.showMessageDialog(null, "Đã có lỗi xảy ra");
        }
        return list;
    }

    private ItemMuonSach getItemFromString(String item) {
        ItemMuonSach itemBook = new ItemMuonSach();
        String[] items = item.split("@");
        for (int i = 0; i < items.length; i++) {
            String temp = items[i];
            String[] temps = temp.split("#");
            itemBook.setIdBook(temps[0]);
            itemBook.setNgayMuon(temps[1]);
            System.out.println("ABC: " + temps[0]);
            System.out.println("AB: " + temps[1]);
        }

        return itemBook;
    }

    public byte[] getAIDApplet() {
        byte[] aid = null;
        try {
            response = channel.transmit(new CommandAPDU(0x00, (byte) 0x09, 0x04, 0x00));
            String check = Integer.toHexString(response.getSW());
            if (check.equals("9000")) {
//                JOptionPane.showMessageDialog(null, "Connect thành công ");
                aid = response.getData();
                return aid;
            }
//            else {
////                JOptionPane.showMessageDialog(null, "Connect không thành công");
//                return false;
//            }
        } catch (Exception e) {
        }
        return null;

    }

    public boolean verifyCard(byte[] pin) {
        try {
            response = channel.transmit(new CommandAPDU(0x00, (byte) 0x06, 0x26, 0x04, pin));
            String check = Integer.toHexString(response.getSW());
            if (check.equals("9000")) {
                return true;
            } else {
                verifyFalse++;
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean unblockCard() {
        try {
            response = channel.transmit(new CommandAPDU(0x00, (byte) 0x07, 0x26, 0x04));
            String check = Integer.toHexString(response.getSW());
            if (check.equals("9000")) {
                verifyFalse = 0;
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean changePin(byte[] newPin) {
        try {
            response = channel.transmit(new CommandAPDU(0x00, (byte) 0x08, 0x26, 0x04, newPin));
            String check = Integer.toHexString(response.getSW());
            if (check.equals("9000")) {
                return true;
            } else if (check.equals("6312")) {
//                JOptionPane.showMessageDialog(null, "Mã pin cũ không đúng");
                return false;
            } else {
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean saveImgToCard(byte[] img) {
        try {
            response = channel.transmit(new CommandAPDU((byte) 0x00, (byte) 0x05, (byte) 0x26, (byte) 0x04));
            int length = img.length;
            int inLeng = length / 250;
            int point = 0;
            byte[] temp = null;
            while (inLeng >= 0) {
                int len = (length - (250 * inLeng));
                temp = new byte[len];
                for (int i = 0; i < len; i++) {
                    temp[i] = img[point];
                    point++;
                }
                length -= len;
                response = channel.transmit(new CommandAPDU((byte) 0x00, (byte) 0x02, (byte) 0x04, (byte) 0x00, temp));
                inLeng--;
            }
            String check = Integer.toHexString(response.getSW());
            if (check.equals("9000")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Đã có lỗi xảy ra");
        }
        return false;
    }

    public byte[] readImg() {
        byte[] images = null;
        try {
            response = channel.transmit(new CommandAPDU((byte) 0x00, (byte) 0x04, (byte) 0x12, (byte) 0x12));
            byte[] res = response.getData();
            int lengthImg = byteArrayToShort(res);
            if (lengthImg == 0) {
                return null;
            }
            images = new byte[lengthImg];
            int inLeng = lengthImg / 250;
            int point = 0;
            int pointer = 0;
            byte[] temp = null;
            byte[] leng = new byte[4];
            while (inLeng >= 0) {
                int len = (lengthImg - (250 * inLeng));
                response = channel.transmit(new CommandAPDU((byte) 0x00, (byte) 0x03, (byte) len, (byte) 0x26, leng));
                temp = response.getData();
                for (int i = 0; i < temp.length; i++) {
                    images[point] = temp[i];
                    point++;
                }
                pointer += len;
                leng = intToByteArray(pointer);
                lengthImg -= len;
                inLeng--;
            }
        } catch (CardException ex) {
            JOptionPane.showMessageDialog(null, "Đã có lỗi xảy ra khi đọc ảnh");
        }
        return images;
    }

    public Infomation readInfo() {
        Infomation info = new Infomation();
        try {
            response = channel.transmit(new CommandAPDU(00, 01, 26, 04));
            String check = Integer.toHexString(response.getSW());
            if (checkVerify(check)) {
                byte[] dataText = response.getData();
                StringBuilder infoText = new StringBuilder();
                for (int i = 0; i < dataText.length; i++) {
                    if (dataText[i] != 0) {
                        infoText.append(Character.toString((char) dataText[i]));
                    }
                }
                info = getInfoInString(infoText.toString());
                info.setAvata(readImg());

                if (check.equals("9000")) {
                    return info;
                } else {
                    JOptionPane.showMessageDialog(null, "Read không thành công");
                    return null;
                }
            }
        } catch (Exception ex) {
//            return null;
//            JOptionPane.showMessageDialog(null, "Đã có lỗi xảy ra");
        }
        return info;
    }

    public boolean saveInfo(Infomation info) {
        try {
            if (info.getAvata().length == 0) {
                return saveTextInfo(info);
            } else {
                if (saveTextInfo(info) && saveImgToCard(info.getAvata())) {
//                    JOptionPane.showMessageDialog(null, "Lưu thông tin thành công ");
                    return true;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Đã có lỗi xảy ra");
            return false;
        }
        return false;
    }

    public boolean saveTextInfo(Infomation info) {
        try {
            String svData = info.getFullName() + "#" + info.getKhoa() + "#" + info.getLop() + "#" + info.getMaSV();
            String temp = SmartCardWork.stringToHex(svData);
            byte[] dataSV = SmartCardWork.hexStringToByteArray(temp);
            response = channel.transmit(new CommandAPDU((byte) 0x00, (byte) 0x00, (byte) 0x12, (byte) 0x12, dataSV));
            String check = Integer.toHexString(response.getSW());
            if (checkVerify(check)) {
                if (check.equals("9000")) {
                    JOptionPane.showMessageDialog(null, "Lưu thông tin thành công ");
                    return true;
                } else {
                    JOptionPane.showMessageDialog(null, "Lưu thông tin không thành công");
                    return false;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Đã có lỗi xảy ra");
        }
        return false;
    }

    public RSAPublicKey getPublicKey() {
        RSAPublicKey rsaPublicKey = null;
        try {
            ResponseAPDU resp_modulus = channel.transmit(new CommandAPDU((byte) 0x00, (byte) 0x12, (byte) 0x12, (byte) 0x12));
            ResponseAPDU resp_exponent = channel.transmit(new CommandAPDU((byte) 0x00, (byte) 0x11, (byte) 0x12, (byte) 0x12));
            byte[] modulus = resp_modulus.getData();
            byte[] exponent = resp_exponent.getData();
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(1, modulus), new BigInteger(1, exponent));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
        }
        return rsaPublicKey;
    }

    public boolean veriApplet(byte[] mess) {
        try {
            ResponseAPDU resp = channel.transmit(new CommandAPDU((byte) 0x00, (byte) 0x10, (byte) 0x12, (byte) 0x12));
            byte[] sigToVerify = resp.getData();
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(getPublicKey());
            sig.update(mess);
            checkVeriRSA = sig.verify(sigToVerify);
            return checkVeriRSA;
        } catch (Exception e) {
        }
        return false;
    }

    private boolean checkVerify(String result) {
        try {
            if (result.trim().equals("6311")) {
                if (verifyFalse < 3) {
                    JPanel panel = new JPanel();
                    JLabel label = new JLabel("<html><p style=\"color:red\">Thẻ sẽ bị khóa sau <b>" + (3 - verifyFalse)
                            + "</b> lần nhập </p>\n Nhập mã pin");
                    JPasswordField pass = new JPasswordField(10);
                    panel.add(label);
                    panel.add(pass);
                    String[] options = new String[]{"OK", "Cancel"};
                    int option = JOptionPane.showOptionDialog(null, panel, "Nhập mật khẩu để xác thực thẻ",
                            JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                            null, options, options[1]);
                    if (option == 0)
                    {
                        String password = new String(pass.getPassword());
                        byte[] pin = SmartCardWork.stringToByteArr(password);
                        if (verifyCard(pin)) {
                            return true;
                        } else {
                            JOptionPane.showMessageDialog(null, "Mã pin sai");
                            return false;
                        }
                    }

//                    String p = JOptionPane.showInputDialog(null, "<html><p style=\"color:red\">Thẻ sẽ bị khóa sau <b>" + (3 - verifyFalse)
//                            + "</b> lần nhập </p>\n Nhập mã pin ", "Mã pin", JOptionPane.QUESTION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Thẻ đã bị khóa do nhập mã pin sai quá số lượt");
                    return false;
                }
            } else if (result.trim().equals("6999")) {
                JOptionPane.showMessageDialog(null, "Thẻ đã bị khóa do nhập mã pin sai quá số lượt");
                verifyFalse = 3;
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private Infomation getInfoInString(String data) {
        Infomation info = new Infomation();
        String[] text = data.split("#");
        info.setFullName(text[0]);
        info.setKhoa(text[1]);
        info.setLop(text[2]);
        info.setMaSV(text[3]);
        return info;
    }

    public static void setImageIcon(byte[] img, JLabel lbImg) {
        try {
            if (img.length == 0) {
                JOptionPane.showMessageDialog(null, "Không có ảnh để hiển thị");
            } else {
                ImageIcon imageIcon = new ImageIcon(img);
                imageIcon.getImage();
                Image pic = imageIcon.getImage();
                Image pic2 = pic.getScaledInstance(lbImg.getWidth(), lbImg.getHeight(), Image.SCALE_SMOOTH);
                ImageIcon im = new ImageIcon(pic2);
                lbImg.setIcon(im);
            }
        } catch (Exception e) {

        }
    }

    public static ImageIcon resizeImageIcon(String imagePath, JLabel lbImg) {

        ImageIcon myImage = new ImageIcon(imagePath);
        Image pic = myImage.getImage();
        Image pic2 = pic.getScaledInstance(lbImg.getWidth(), lbImg.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon im = new ImageIcon(pic2);
        return (im);
    }

    public static byte[] stringToByteArr(String text) {
        String resultHex = SmartCardWork.stringToHex(text);
        return SmartCardWork.hexStringToByteArray(resultHex);
    }

    public static String stringToHex(String str) {
        StringBuilder buf = new StringBuilder(200);
        for (char ch : str.toCharArray()) {
            buf.append(String.format("%02x", (int) ch));
        }
        return buf.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static final short byteArrayToShort(byte[] arrShort) {
        return (short) (arrShort[0] << 24
                | ((arrShort[1] << 24) >>> 8)
                | ((arrShort[2] << 24) >>> 16)
                | ((arrShort[3] << 24) >>> 24));
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[]{
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) value};
    }
}
