package sample.model;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.NoSuchPaddingException;
import java.security.*;


public class RSA {

    // encrypt your message
    public static byte[] enc(String plaintext, String publicKey) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher ci = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        ci.init(Cipher.ENCRYPT_MODE, convertToPublicKey(publicKey));
        return ci.doFinal(plaintext.getBytes());
    }

    // decrypt your message
    public static String dec(String plaintext, String base64PrivateKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        return dec(Base64.getDecoder().decode(plaintext.getBytes()), convertToPrivateKey(base64PrivateKey));
    }

    // helper decrypt method
    private static String dec(byte[] plaintextByteArray, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(plaintextByteArray));
    }

    // helper method
    private static PublicKey convertToPublicKey(String publicKey64){
        PublicKey publicKey;
        try{
            X509EncodedKeySpec ks = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey64.getBytes()));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            publicKey = kf.generatePublic(ks);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    // helper method
    private static PrivateKey convertToPrivateKey(String privateKey64){
        PrivateKey privateKey = null;
        KeyFactory keyFactory = null;
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey64.getBytes()));
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            assert keyFactory != null;
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKey;
    }
}

