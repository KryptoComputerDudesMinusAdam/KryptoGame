package sample.model;

import java.io.IOException;
import java.security.*;
import java.util.Base64;

public class RSAGenerator {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSAGenerator() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair pair = kpg.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        RSAGenerator rsag = new RSAGenerator();
        System.out.println(Base64.getEncoder().encodeToString(rsag.getPublicKey().getEncoded()));
        System.out.println(Base64.getEncoder().encodeToString(rsag.getPrivateKey().getEncoded()));
    }
}
