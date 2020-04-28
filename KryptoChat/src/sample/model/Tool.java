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
}