package utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    private String preSharedKey;

    public Encryption(String preSharedKey) {
        this.preSharedKey = preSharedKey;
    }


    /**
     * Returns encrypted string
     * @return the string to encrypt
     */
    public String encrypt(String value) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException,
            InvalidParameterSpecException, UnsupportedEncodingException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(preSharedKey.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

        IvParameterSpec ivSpec = cipher.getParameters().getParameterSpec(IvParameterSpec.class);

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

        byte[] iv = ivSpec.getIV();
        byte[] encrypted = cipher.doFinal(value.getBytes());
        byte[] enc = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, enc, 0, iv.length);
        System.arraycopy(encrypted, 0, enc, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(enc);
    }

    /**
     * Returns decrypted string
     * @return the string to decrypt
     */
     public String decrypt(String value) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
             BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException,
             InvalidParameterSpecException, UnsupportedEncodingException {
        SecretKeySpec secret = new SecretKeySpec(preSharedKey.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

        byte[] decoded = Base64.getDecoder().decode(value);
        byte[] iv = new byte[16];
        byte[] decrypt = new byte[decoded.length - 16];
        System.arraycopy(decoded, 0, iv, 0, 16);
        System.arraycopy(decoded, 16, decrypt, 0, decrypt.length);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec);

        byte[] original = cipher.doFinal(decrypt);

        return new String(original);
    }
}
