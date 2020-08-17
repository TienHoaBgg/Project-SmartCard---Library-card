package com.nth.smartcard;

import javacard.framework.*;
import javacard.security.*;

public class Main extends Applet
{
	
	// INS Luu va xuat thong tin sinh vien
	final static byte INS_INSERT_INFO = (byte)0x00;
	final static byte INS_READ_INFO = (byte)0x01;
	
	// INS Luu anh va xuat anh
	final static byte INS_INSERT_IMG = (byte)0x02;
	final static byte INS_READ_IMG = (byte)0x03;
	final static byte INS_READ_SIZE_IMG = (byte)0x04;
	final static byte INS_CLEAN_SIZE_IMG = (byte)0x05;
	
	// INS Xac nhan ma pin
	final static byte INS_VERYFY = (byte)0x06;
	// INS Unblock khi nhap sai ma pin nhieu lan
	final static byte INS_UNBLOCK_CARD = (byte)0x07;
	// INS Thay doi ma pin
	final static byte INS_CHANGE_PIN = (byte)0x08;
	
	// INS Tao chu ky xac thuc
	final static byte INS_SIGN = (byte)0x10;
	// INS doc khoa public key
	final static byte INS_READ_PUBLICKEY_EXP = (byte) 0x11;
	final static byte INS_READ_PUBLICKEY_MOD = (byte) 0x12;
	
	// INS Luu va xuat thong tin sach da muon
	final static byte INS_MUON_SACH = (byte) 0x13;
	final static byte INS_GET_LIST_SACH = (byte) 0x14;
	
	// So lan nhap ma pin sai truoc khi khoa the
	final static byte PIN_TRY_LIMIT =(byte)0x03;
	// maximum size PIN
	final static byte MAX_PIN_SIZE =(byte)0x08;
	
	// SW phan hoi khi nhap ma pin
	final static short SW_VERIFICATION_FAILED = 0x6312;
	final static short SW_PIN_VERIFICATION_REQUIRED = 0x6311;
	
	private static byte[] temp,infomation;
	private static byte[] images,imgLength;
	private static byte[] library;
	private static byte[] pinCard;
	OwnerPIN pin;
	
	//========= Khao bao bien lien quan xac thuc RSA ========
	private static byte[] dataVeriRSA,sig_buffer;
	private RSAPrivateKey rsaPrivKey;
	private RSAPublicKey rsaPubKey;
	private Signature rsaSign;
	private short sigLen;
	
	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new Main(bArray,bOffset,bLength);
		
	}
	
	protected Main(byte[] bArray, short bOffset, byte bLength) {

		byte aIDLen = bArray[bOffset]; 
		if(aIDLen == 0){
			register();
		}else{
			register(bArray, (short)(bOffset+1), aIDLen);
		}

		pin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);
		pinCard = new byte[]{0x31,0x32,0x33,0x34};
		pin.update(pinCard, (short) 0, (byte)pinCard.length);
		
		infomation = new byte[120];
		images = new byte[32000];
		imgLength = new byte[]{0x00,0x00,0x00,0x00};
		library = new byte[199];
		
		bOffset = (short) (bOffset+aIDLen+1);
		byte cLen = bArray[bOffset]; 
		bOffset = (short) (bOffset+cLen+1);
		byte aLen = bArray[bOffset]; 
		bOffset = (short) (bOffset + 1);
		
		if (aLen != 0)
		{
			dataVeriRSA = new byte[aLen];
			Util.arrayCopy(bArray, bOffset, dataVeriRSA, (byte)0,aLen);
			bOffset += aLen;
		}else{
			dataVeriRSA = new byte[] {'T', 'H', 'E',' ','T','H','U',' ','V','I','E', 'N'};
		}
		
		sigLen = (short)(KeyBuilder.LENGTH_RSA_1024/8);
		sig_buffer = new byte[sigLen];
		rsaSign = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1,false);
		KeyPair keyPair = new KeyPair(KeyPair.ALG_RSA,(short)(8*sigLen));
		keyPair.genKeyPair();
		rsaPrivKey = (RSAPrivateKey)keyPair.getPrivate();
		rsaPubKey = (RSAPublicKey)keyPair.getPublic();
		rsaSign.init(rsaPrivKey, Signature.MODE_SIGN);
		rsaSign.sign(dataVeriRSA, (short)0, (short)(dataVeriRSA.length), sig_buffer, (short)0);
		
    }
	
	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}

		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS])
		{
		case INS_INSERT_INFO:
			insertInfo(apdu);
			break;
		case INS_READ_INFO:
			readInfo(apdu);
			break;
		case INS_INSERT_IMG:
			insertIMG(apdu);
			break;
		case INS_READ_IMG:
			readIMG(apdu);
			break;
		case INS_READ_SIZE_IMG:
			readSizeIMG(apdu);
			break;
		case INS_CLEAN_SIZE_IMG:
			cleanSizeIMG(apdu);
			break;
		case INS_UNBLOCK_CARD:
			pin.resetAndUnblock();
			break;
		case INS_VERYFY:
			verify(apdu);
			break;
		case INS_CHANGE_PIN:
			changePin(apdu);
			break;
		case INS_SIGN:
			initRSASign(apdu);
			break;
		case INS_READ_PUBLICKEY_EXP:
			getPublicKeyExp(apdu);
			break;
		case INS_READ_PUBLICKEY_MOD:
			getPublicKeyMod(apdu);
			break;
		case INS_MUON_SACH:
			muonSach(apdu);
			break;
		case INS_GET_LIST_SACH:
			getListSach(apdu);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
	
	private void insertInfo(APDU apdu){
		if (!pin.isValidated()){
			ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
		} 
		byte[] buffer = apdu.getBuffer();
		short dLen = (short)(buffer[ISO7816.OFFSET_LC]&0xFF);
		temp = new byte[dLen];
		short byteRead = (short)(apdu.setIncomingAndReceive());
		short pointer = 0;
		while ( dLen > 0){
			Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA,temp, pointer, byteRead);
			pointer += byteRead;
			dLen -= byteRead;
			byteRead = apdu.receiveBytes (ISO7816.OFFSET_CDATA );
		}
		short len = (short)temp.length;
		infomation[0] = (byte) len;
		for(short i = 0 ; i < len ; i ++){
			infomation[(short)(i+1)] = temp[i];
		}
	}
	
	private void readInfo(APDU apdu){
		if (!pin.isValidated()){
			ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
		}
		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		short lenInfo = (short) infomation[0];
		Util.arrayCopy(infomation,(short)1,buffer,(short)0,lenInfo);
		apdu.setOutgoingAndSend((short)0,lenInfo);
	}
	
	private void insertIMG(APDU apdu){
		if (!pin.isValidated()){
			ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
		}
		byte[] buffet = apdu.getBuffer();
		short dataLen = (short)(buffet[ISO7816.OFFSET_LC]&0xff);
		short byteRead = (short)(apdu.setIncomingAndReceive());
		short point = byteArrayToShort(imgLength);
		while(dataLen > 0 ){
			Util.arrayCopy(buffet,ISO7816.OFFSET_CDATA,images,point,byteRead);
			point +=byteRead;
			dataLen -=byteRead;
			byteRead = apdu.receiveBytes(ISO7816.OFFSET_CDATA);
		}
		imgLength = shortToByteArray(point);
		
	}
	
	private void readIMG(APDU apdu){
		if (!pin.isValidated()){
			ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
		}
		byte[] buffe =  apdu.getBuffer();
		short byteRead = (short)(apdu.setIncomingAndReceive());
	
		temp = new byte[4];
		Util.arrayCopy(buffe,ISO7816.OFFSET_CDATA,temp,(short)0,byteRead);
		
		short pointer = byteArrayToShort(temp);
		short leng = (short)(buffe[ISO7816.OFFSET_P1]&0xff);
		
		Util.arrayCopy(images,pointer,buffe,(short)0,leng);
		apdu.setOutgoingAndSend((short)0,leng);
		
	}
	
	private void muonSach(APDU apdu){
		if (!pin.isValidated()){
			ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
		} 
		byte[] buffer = apdu.getBuffer();
		short dLen = (short)(buffer[ISO7816.OFFSET_LC]&0xFF);
		temp = new byte[dLen];
		short byteRead = (short)(apdu.setIncomingAndReceive());
		short pointer = 0;
		while ( dLen > 0){
			Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA,temp, pointer, byteRead);
			pointer += byteRead;
			dLen -= byteRead;
			byteRead = apdu.receiveBytes (ISO7816.OFFSET_CDATA );
		}
		short len = (short)temp.length;
		library[0] = (byte) len;
		for(short i = 0 ; i < len ; i ++){
			library[(short)(i+1)] = temp[i];
		}
		
	}
	
	private void getListSach(APDU apdu){
		if (!pin.isValidated()){
			ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
		}
		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		short lenLib = (short) (library[0]&0xFF);
		Util.arrayCopy(library,(short)1,buffer,(short)0,lenLib);
		apdu.setOutgoingAndSend((short)0,lenLib);

	}
	
	private void readSizeIMG(APDU apdu){
		byte[] buffe =  apdu.getBuffer();
		apdu.setIncomingAndReceive();
		Util.arrayCopy(imgLength,(short)0,buffe,(short)0,(short)4);
		apdu.setOutgoingAndSend((short)0,(short)4);
		
	}
	
	private void cleanSizeIMG(APDU apdu){
		byte[] buffe =  apdu.getBuffer();
		apdu.setIncomingAndReceive();
		for(short i = 0 ; i < imgLength.length;i++){
			imgLength[i]=(byte)0x00;
		}
	}
	
	private void initRSASign(APDU apdu){
		byte[] buf = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		rsaSign.init(rsaPrivKey, Signature.MODE_SIGN);
		rsaSign.sign(dataVeriRSA, (short)0, (short)(dataVeriRSA.length), sig_buffer, (short)0);
		apdu.setOutgoing();
		apdu.setOutgoingLength(sigLen);
		apdu.sendBytesLong(sig_buffer, (short)0, sigLen);
	}
	
	private void getPublicKeyExp(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		short length = rsaPubKey.getExponent(buffer, (short) 0);
		apdu.setOutgoingAndSend((short) 0, length);
	}

	private void getPublicKeyMod(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		short length = rsaPubKey.getModulus(buffer, (short) 0);
		apdu.setOutgoingAndSend((short) 0, length);
	}
	
	
	private void verify(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		byte byteRead = (byte)(apdu.setIncomingAndReceive());
		if ( pin.check(buffer, ISO7816.OFFSET_CDATA,byteRead) == false ){
			ISOException.throwIt(SW_VERIFICATION_FAILED);
		}
	}
	
	private void changePin(APDU apdu){
		byte[] buffer = apdu.getBuffer();
		byte byteRead = (byte)(apdu.setIncomingAndReceive()&0xFF);
		temp = new byte[byteRead];
		Util.arrayCopy(buffer,ISO7816.OFFSET_CDATA,temp,(short)0,byteRead);
		byte[] pinOld = new byte[pinCard.length];
		for(short i = 0; i < pinOld.length;i++){
			pinOld[i] = temp[i];
		}
		short index = (short)0;
		for(short j = (short) pinOld.length ; j < temp.length;j++ ){
			pinCard[index] = temp[j];
			index++;
		}
		
		if ( pin.check(pinOld, (short)0,(byte)pinOld.length) == false ){
			ISOException.throwIt(SW_VERIFICATION_FAILED);
		}else{
			pin.update(pinCard, (short) 0, (byte)pinCard.length);
		}
	}
	
	public boolean select() {
        if ( pin.getTriesRemaining() == 0 ) 
        	return false;
		return true;
	}
   
	public void deselect() {
		pin.reset();
	}
	
	private static final short byteArrayToShort(byte[] arrShort) {
		return (short)( arrShort[0]<<24 |
						((arrShort[1]<<24)>>>8 ) | 
						((arrShort[2]<<24)>>>16) | 
						((arrShort[3]<<24)>>>24));
    }

	private static final byte[] shortToByteArray(short value) {
		return new byte[] {
			(byte)(value >>> 24),
			(byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value};
    }

}
