package sample.model;

import java.util.*;
import java.util.Map.Entry;

public class Tool
{
    public static Map<Character,Double> freq = new HashMap<>();
    public static Map<Character,Double> fE = new HashMap<>();
    public static char [] arrl = {'E','T','A','O','I','N','S','R','H','D','L','U',
            'C','M','F','Y','W','G','P','B','V','K','X','Q',
            'J','Z'};
    public Tool()
    {
        init();
    }

    private static int compare(Entry<Character, Double> o1, Entry<Character, Double> o2) {
        return ((Comparable) ((Entry) o1).getValue()).compareTo(((Entry) (o2)).getValue());
    }

    public void analyze(String s)
    {
        char[] charArray = s.toCharArray();
        double spaces = 0.0, count;      // Avoiding spaces
        for(int i = 0; i < charArray.length; i++)
            if (charArray[i] != ' ')
                spaces++;
        while(charArray.length > 1)
        {
            /* Reset */
            charArray = s.toCharArray();
            count = 1.0;
            for(int i = 1; i < charArray.length; i++)
                if(charArray[0] == charArray[i])
                    count++;
            if(charArray[0] != ' ')
                freq.put(Character.toUpperCase(charArray[0]), ((count/spaces) * 100));
            s = s.replace("" +charArray[0], "");
            charArray = s.toCharArray();
        }

        for (int i = 0; i < arrl.length; i++)
            if (freq.get(arrl[i]) == null)
                freq.put(arrl[i], 0.0);

    }
    /*Initialize hashmap*/
    public void init()
    {
        double [] arrf = {12.02,9.10,8.12,7.68,7.31,6.95,6.28,6.02,
                5.92,4.32,3.98,2.88,2.71,2.61,2.30,2.11,2.09,
                2.03,1.82,1.49,1.11,0.69,0.17,0.11,0.10,0.07};
        for(int i = 0; i < 26 ;i++)
            fE.put(arrl[i],arrf[i]);
        sort(fE);
    }

    /*Sort the hash map*/
    public String sort(Map<Character, Double> map)
    {
        List<Entry<Character, Double>> list = new LinkedList<>(map.entrySet());
        // Defined Custom Comparator here
        list.sort(Tool::compare);

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        Map<Character, Double> sortedHashMap = new LinkedHashMap<>();

        for (Entry<Character, Double> entry : list) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap.toString();
    }

    public String printMaps()
    {
        StringBuilder s = new StringBuilder();
        s.append(sort(freq));
        String reversed = (((s.reverse()).toString()).replaceAll("[^a-zA-Z,]",""))+",";
        String freqResult = "";
        for(int i = 0; i < arrl.length;i++)
            freqResult += arrl[i] + ":"+String.format("%.2f",freq.get(arrl[i]))+",";
        freqResult += "\b#";
        for (int i = 0; i < arrl.length; i++) {
            reversed = reversed.replaceFirst(",", "="+arrl[i]+"#");
        }
        reversed = reversed.replace("#", ",")+"\b";
        return (freqResult+reversed);
    }

    static class VigenereAttacker
    {
    final static double freq[] = {
            0.08167, 0.01492, 0.02782, 0.04253, 0.12702, 0.02228, 0.02015,
            0.06094, 0.06966, 0.00153, 0.00772, 0.04025, 0.02406, 0.06749,
            0.07507, 0.01929, 0.00095, 0.05987, 0.06327, 0.09056, 0.02758,
            0.00978, 0.02360, 0.00150, 0.01974, 0.00074};

    public static String init (String cipherText, int keyLength)
    {
        //Initialize variables
        int lenghtOfEncodedMessage = cipherText.length();
        char[] cipherTextCharacters = new char [lenghtOfEncodedMessage];
        char[] key =  new char [lenghtOfEncodedMessage];
        char[] keyFinal =  new char [lenghtOfEncodedMessage];
        int count = 0;

        //get characters from cipherText and put them in cipherTextCharacters []
        cipherText.getChars(0, lenghtOfEncodedMessage, cipherTextCharacters, 0);

        int txt[] = new int[lenghtOfEncodedMessage];
        int len = 0, j;
        double fit, best_fit = 1e100;

        for (j = 0; j < lenghtOfEncodedMessage; j++)
            if (Character.isUpperCase(cipherTextCharacters[j]))
                txt[len++] = cipherTextCharacters[j] - 'A';

        for (j = 1; j < 30; j++)
        {
            count++;
            fit = freq_every_nth(txt, len, j, key);
            if (fit < best_fit)
            {
                best_fit = fit;
                if(count == keyLength)
                    for(int i = 0; i < lenghtOfEncodedMessage; i++)
                        keyFinal[i] = key[i];
            }
        }//End of for loop
        String decrypted = decrypt(cipherText, ((Arrays.toString(keyFinal)).replaceAll("[^a-zA-Z]", "")));
        return decrypted;
    }
    static double freq_every_nth(final int []msg, int len, int interval, char[] key)
    {
        double sum, d, ret;
        double  [] accu = new double [26];
        double  [] out = new double [26];
        int i, j, rot;

        for (j = 0; j < interval; j++)
        {
            for (i = 0; i < 26; i++)
            {
                out[i] = 0;
            }
            for (i = j; i < len; i += interval)
            {
                out[msg[i]]++;
            }

            rot = best_match(out, freq);

            try
            {
                key[j] = (char)(rot + 'A');

            }
            catch (Exception e)
            {
                System.out.print(e.getMessage());
            }
            for (i = 0; i < 26; i++)
            {
                accu[i] += out[(i + rot) % 26];
            }

        }

        for (i = 0, sum = 0; i < 26; i++)
        {
            sum += accu[i];
        }


        for (i = 0, ret = 0; i < 26; i++)
        {
            d = accu[i] / sum - freq[i];
            ret += d * d / freq[i];
        }

        key[interval] = '\0';
        return ret;
    }

    static int best_match(final double []a, final double []b)
    {
        double sum = 0, fit, d, best_fit = 1e100;
        int i, rotate, best_rotate = 0;

        for (i = 0; i < 26; i++)
        {
            sum += a[i];
        }

        for (rotate = 0; rotate < 26; rotate++)
        {
            fit = 0;
            for (i = 0; i < 26; i++)
            {
                d = a[(i + rotate) % 26] / sum - b[i];
                fit += d * d / b[i];
            }

            if (fit < best_fit)
            {
                best_fit = fit;
                best_rotate = rotate;
            }
        }

        return best_rotate;
    }
    public static String decrypt(String text, final String key)
    {
        String res = "";
        text = text.toUpperCase();
        for (int i = 0, j = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (c < 'A' || c > 'Z') continue;
            res += (char)((c - key.charAt(j) + 26) % 26 + 'A');
            j = ++j % key.length();
        }
        return res;
    }
}
}

