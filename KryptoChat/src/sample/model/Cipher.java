package sample.model;

import java.util.*;

public class Cipher {
    /*
    TODO:
        Add Mono-Alphabetic Cypher
        in this format
        public static String method_name_Encrypt(your arguments){
            // return a string variable
        }
        public static String method_name_decrypt(your arguments){
            // return a string variable
        }
     */

    public static void main(String[] args){
        //TODO: test your method here
    }


    /*
    TODO:
        mono
        alice and bob have the same key
        nobody else knows the key
        server generates a random dictionary of length 26
        clients expect first message to be from server
        message contains dictionary variable and stored as their key
     */

    public static String generateMonoKey(){
        String strAlphabet = "abcdefghijklmnopqrstuvwxyz";
        List<Character> alphabet = new ArrayList<>();
        for(int i = 0; i < strAlphabet.length(); i++){
            alphabet.add(strAlphabet.charAt(i));
        }
        Collections.shuffle(alphabet);
        StringBuilder strB = new StringBuilder(alphabet.size());
        for(char c : alphabet){
            strB.append(c);
        }
        System.out.println("Generated key: "+strB);
        return strB.toString();
    }

    public static String monoalphabeticEnc(String key, String plaintext){
        int x;
        int a;
        char[] alphabets = {'a', 'b', 'c', 'd', 'e', 'f','g', 'h', 'i', 'j', 'k', 'l','m', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x','y', 'z',};
        char[] ciphertext = plaintext.toCharArray();
        for(x = 0; x < ciphertext.length;x++)
        {
            for(a = 0; a < 26; a++)
            {
                if(alphabets[a] == ciphertext[x])
                {
                    ciphertext[x] = key.charAt(a);
                    break;
                }
            }
        }
        System.out.println("Encrypted text: ");
        System.out.print(ciphertext);
        return Arrays.toString(ciphertext);
    }

    public static String monoalphabeticDec(String key, String ciphertext){
        int x;
        int a;
        char[] alphabets = {'a', 'b', 'c', 'd', 'e', 'f','g', 'h', 'i', 'j', 'k', 'l','m', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x','y', 'z',};
        char[] plaintext = ciphertext.toCharArray();

        for(x = 0; x < plaintext.length; x++)
        {
            for(a = 0; a < 26; a++)
            {
                if(plaintext[x] == key.charAt(a))
                {
                    plaintext[x] = alphabets[a];
                    break;
                }
            }
        }
        System.out.print("Decrypted text: ");
        System.out.print(plaintext);
        return Arrays.toString(plaintext);
    }
}
