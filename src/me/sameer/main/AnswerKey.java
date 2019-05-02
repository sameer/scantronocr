package me.sameer.main;

import java.util.*;

/**
 * @author Sameer Puri
 */
public class AnswerKey 
{
    public List<Character> answers;
    public AnswerKey(List<String> as)
    {
        List<Character> bas = new ArrayList<Character>();
        for(String s : as)
        {
            s = s.trim();
            bas.add(s.charAt(0));
            
        }
        this.answers= bas;
    }
    @Override
    public String toString()
    {
        return answers.toString();
    }
}
