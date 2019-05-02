package me.sameer.main;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author Sameer Puri
 */
public class Results 
{
    public String ID;
    public ScanModes side;
    public List<Character> answers;
    public int start,end;
    public BufferedImage redded;
    public Results(String ID,ScanModes side, int start, int end, List<Character> answers,BufferedImage redded)
    {
        this.ID = ID;
        this.side = side;
        this.start = start;
        this.end = end;
        this.answers = answers;
        this.redded = redded;//.getSubimage(0, 0, 900, 1700);
    }
}
