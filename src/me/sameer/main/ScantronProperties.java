package me.sameer.main;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * @author Sameer Puri
 */
public class ScantronProperties 
{
    public Map<String,Double> keybase = new HashMap<String,Double>();
    
    public ScantronProperties(InputStream s)
    {
        Scanner in = new Scanner(s);
        while(in.hasNextLine())
        {
            keybase.put(in.next().replace(":",""),in.nextDouble());
        }
        for(Entry<String,Double> key : keybase.entrySet())
        {
            System.out.println(key.getKey() +" => " + key.getValue());
        }
    }
}
