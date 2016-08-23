package org.span.service.system;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class Encryption {

	public SecretKey key = null;
    public Cipher cipherData = null;
    
    public Encryption() throws NoSuchAlgorithmException, NoSuchPaddingException{
//    	key = KeyGenerator.getInstance("AES").generateKey();
        byte[] encoded = {'A','F','l','J','P','a','M','L','|',31,20,'3','8','5','4','#'};
        key = new SecretKeySpec(encoded, "AES");
        cipherData = Cipher.getInstance("AES");
    }
	
    public String encrypt(String str) throws Exception{

        // Encode the string into bytes using utf-8
        byte[] utf8 = str.getBytes("UTF8");

        cipherData.init(Cipher.ENCRYPT_MODE, key);

        // Encrypt
        byte[] enc = cipherData.doFinal(utf8);

        // Encode bytes to base64 to get a string
//        return new sun.misc.BASE64Encoder().encode(enc);
        return Base64.encodeToString(enc,0);
    }

    public String decrypt(String str) throws Exception{
        // Decode base64 to get bytes
        byte[] dec = Base64.decode(str, 0);

        cipherData.init(Cipher.DECRYPT_MODE, key);

        // Decrypt
        byte[] utf8 = cipherData.doFinal(dec);

        // Decode using utf-8
        return new String(utf8, "UTF8");
    }
    
    public static void pl(String text){
        System.out.print(text);
    }
}










