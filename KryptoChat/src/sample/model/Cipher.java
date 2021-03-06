package sample.model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Cipher {

    //STREAM CIPHER
    public static String streamEnc( String key, String plainText){

        return xorOperation(plainText.getBytes(), key.getBytes());
    }

    public static String streamDec( String key, String cipherText)
    {
        String x = new String(xorOperation2(cipherText, key.getBytes()));
        return x;
    }

    public static String xorOperation(byte[] pArr1, byte[] kArr2)
    {
        if(kArr2.length > pArr1.length)
        {
            byte[] temp = new byte[pArr1.length];
            for(int i = 0; i<pArr1.length; i++){
                temp[i] = kArr2[i];
            }
            kArr2 = temp;
        }
        byte[] outputByteArr = new byte[pArr1.length];
        String returnArr = "";
        for (int i = 0; i < pArr1.length; i++) {
            outputByteArr[i] = (byte) (pArr1[i] ^ kArr2[i%kArr2.length]);
            returnArr += outputByteArr[i] + ",";
        }
        return returnArr;
    }

    public static byte[] xorOperation2(String arrx, byte[] arr2)
    {
        StringTokenizer st = new StringTokenizer(arrx, " \t\n\r\f,");
        int length = arrx.length() - arrx.replace(",", "").length();
        byte[] arr1 = new byte[length];
        int y = 0;
        while(st.hasMoreTokens())
        {
            arr1[y] = (byte)Integer.parseInt(st.nextToken());
            y++;
        }
        if(arr2.length > arr1.length)
        {
            byte [] temp = new byte[length];
            for(int i =0; i < length; i ++)
            {
                temp[i] = arr2[i];
            }
            arr2 = temp;
        }
        byte[] outputByteArr = new byte[arr1.length];
        String returnArr = "";
        for (int i = 0; i < arr1.length; i++) {
            outputByteArr[i] = (byte) (arr1[i] ^ arr2[i%arr2.length]);
            returnArr += outputByteArr[i] + "";
        }
        return outputByteArr;
    }

    // VIGENERE CIPHER
    public static String generateBasicKey() {
        String strAlphabet = "abcdefghijklmnopqrstuvwxyz";
        int count = ThreadLocalRandom.current().nextInt(4, 10);
        Random rand = new Random();
        char[] key = new char[count];
        for(int i = 0; i<count; i++){
            key[i] = strAlphabet.charAt(ThreadLocalRandom.current().nextInt(0, strAlphabet.length()));
        }
        return new String(key);
    }

    public static String vigenereEnc(String key, String plaintext) {
        String[] info = getInfo(plaintext, key);
        String p = info[0];
        String k = info[1];
        int[][] table = vigenereTable();
        String encrypted = "";
        for (int i = 0; i < p.length(); i++)
        {
            if (p.charAt(i) < 'A' || p.charAt(i) > 'Z')
            {
                encrypted += p.charAt(i);
                continue;
            }
            if(p.charAt(i) == (char)32 && k.charAt(i) == (char)32)
            {
                encrypted += " ";
            }
            else
            {
                encrypted += (char)table[(int)p.charAt(i)-65][(int)k.charAt(i)-65];
            }
        }
        System.out.println("Encrypted Text: " + encrypted);

        return encrypted;
    }

    public static String vigenereDec(String key, String plaintext) {
        String[] info = getInfo(plaintext, key);
        String p = info[0];
        String k = info[1];
        String decryptedText = "";
        for (int i = 0; i < p.length(); i++)
        {
            if (p.charAt(i) < 'A' || p.charAt(i) > 'Z')
            {
                decryptedText += p.charAt(i);
                continue;
            }
            if(p.charAt(i) == (char)32 && k.charAt(i) == (char)32)
            {
                decryptedText += " ";
            }
            else
            {
                decryptedText += (char)(65 + decryptCounter((int)k.charAt(i), (int)p.charAt(i)));
            }
        }
        System.out.println("Decrypted Text: " + decryptedText);
        return decryptedText;

    }

    private static String[] getInfo(String message, String key) {
        String mapping = "";
        message = message.toUpperCase();
        key = key.toUpperCase();

        for (int i = 0, j = 0; i < message.length(); i++) {
            if(message.charAt(i) == (char)32) {
                mapping += (char)32;
            }
            else {
                if(j < key.length()) {
                    mapping += key.charAt(j);
                    j++;
                }
                else {
                    j = 0;
                    mapping += key.charAt(j);
                    j++;
                }
            }  //end of if-else
        } //end of for loop
        return new String[]{message, mapping};    }

    private static int[][] vigenereTable() {
        //creating table that contains the alphabet
        int[][] table = new int[26][26];

        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 26; j++) {
                int temp;
                if((i + 65) + j > 90) {
                    temp = ((i + 65) + j) - 26;
                    table[i][j] = temp;
                }
                else {
                    temp = (i + 65) + j;
                    table[i][j] = temp;
                }
            }
        }
        return table;
    }

    //keeping count for decryption method
    private static int decryptCounter(int keyValue, int message) {
        int count = 0;
        String value = "";

        for (int i = 0; i < 26; i++) {
            if(keyValue + i > 90) {
                value += (char)(keyValue + (i - 26));
            }
            else {
                value += (char)(keyValue + i);
            }
        }

        for (int i = 0; i < value.length(); i++) {
            //if found break else increment
            if(value.charAt(i) == message) {
                break;
            }
            else {
                count++;
            }
        }
        return count;
    }

    // MONOALPHABETIC CIPHER
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
                if(Character.toLowerCase(alphabets[a]) == Character.toLowerCase(ciphertext[x]))
                {
                    if(Character.isUpperCase(ciphertext[x])){
                        ciphertext[x] = Character.toUpperCase(key.charAt(a));
                    } else if(Character.isLowerCase(ciphertext[x])){
                        ciphertext[x] = Character.toLowerCase(key.charAt(a));
                    } else{
                        ciphertext[x] = key.charAt(a);
                    }
                    break;
                }
            }
        }
        return new String(ciphertext);
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
                if(Character.toLowerCase(plaintext[x]) == Character.toLowerCase(key.charAt(a)))
                {
                    if(Character.isUpperCase(plaintext[x])){
                        plaintext[x] = Character.toUpperCase(alphabets[a]);
                    } else if(Character.isLowerCase(plaintext[x])){
                        plaintext[x] = Character.toLowerCase(alphabets[a]);
                    } else{
                        plaintext[x] = alphabets[a];
                    }
                    break;
                }
            }
        }
        return new String(plaintext);
    }
}
