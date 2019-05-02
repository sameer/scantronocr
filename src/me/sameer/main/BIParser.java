package me.sameer.main;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Sameer Puri (Robotia, Fracica, Flyingeagle)
 */
public class BIParser
{

    public static char[] letters =
    {
        'A', 'B', 'C', 'D', 'E', 'O'
    };

    public static ScantronProperties PROPS = null;

    //FIleName, start question, end question, front/back, threshold
    public static void main(String[] args) throws Exception
    {
        /*
         * BufferedImage bi = ImageIO.read(new File(in.next())); int start =
         * in.nextInt(), end = in.nextInt(); ScanModes side =
         * ScanModes.valueOf(in.next()); boolean id = in.nextBoolean();
         */
        BufferedImage bi = ImageIO.read(new File("test.png"));
        int start = 1, end = 42;
        ScanModes side = ScanModes.FRONT_ONLY;
        boolean id = true;
        Results r = run(bi, start, end, side, id);
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        //Show it on a third of the scale
        frame.getContentPane().add(new JLabel(new ImageIcon(r.redded.getScaledInstance(r.redded.getWidth() / 3, r.redded.getHeight() / 3, Image.SCALE_SMOOTH))));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static double bubble_space;
    public static int threshold;

    public static Results run(BufferedImage bi, int start, int end, ScanModes side, boolean shouldID)
    {
        try
        {
            //PROPS = new ScantronProperties((Scanner.class.getResourceAsStream("/config.yml")));
            PROPS = new ScantronProperties((new FileInputStream("config.yml")));
            bubble_space = PROPS.keybase.get("bubble_space");
            row_division = PROPS.keybase.get("row_division");
            row_one_startx = PROPS.keybase.get("row_one_startx");
            row_one_starty = PROPS.keybase.get("row_one_starty");
            row_two_startx = PROPS.keybase.get("row_two_startx");
            row_two_starty = PROPS.keybase.get("row_two_starty");
            threshold = PROPS.keybase.get("threshold").intValue();
            //150 dpi
            double scale = 1169. / bi.getHeight();
            System.out.println(bi.getHeight() +","+bi.getWidth());
            bi = Util.createResizedCopy(bi, (int) (bi.getWidth() * scale), (int) (bi.getHeight() * scale), false);
            bi = Util.createResizedCopy(bi, (int) (bi.getWidth() * 2), (int) (bi.getHeight() * 2), false);
            System.out.println("scale = " + scale + " new dims = " + bi.getHeight() + "," + bi.getWidth());
            //Set the starting point for back sided
            if (side.equals(ScanModes.BACK_ONLY))
            {
                start = 51;
            }

            //Get the list of answers
            List<Character> answers = new ArrayList<Character>();
            for (int i = start; i <= end; i++)
            {
                System.out.print("#" + i + ":");

                Point2D.Double current_bubble = (getFirstBubble(i));
                for (int current_x_index = 0; current_x_index < 5; current_x_index++)
                {
                    //Compensate for xshift
                    Point2D.Double tocheck = adjust(current_bubble, current_x_index);
                    //Get the avg color at a bubble
                    Color rgbavg = getRGBAvg(bi, tocheck);
                    //Add the answer
                    if (validBubble(rgbavg))
                    {
                        answers.add(letters[current_x_index]);
                        break;
                    }
                    if (current_x_index == 4 && answers.size() < i)
                    {
                        answers.add('O');
                    }
                }
            }
            //Process IDs
            double id_startx = PROPS.keybase.get("id_startx");
            double id_starty = PROPS.keybase.get("id_starty");
            double id_space = PROPS.keybase.get("id_space");
            String ID = "";
            if (shouldID && side.equals(ScanModes.FRONT_ONLY))
            {
                for (int i = 0; i < 9; i++)
                {
                    double xval = id_startx + id_space * i;
                    for (int y = 0; y < 10; y++)
                    {
                        double yval = id_starty + id_space * y;
                        Point2D.Double bubble = new Point2D.Double(xval, yval);
                        Color rgbavg = getRGBAvg(bi, bubble);
                        if (validBubble(rgbavg))
                        {
                            ID += "" + y;
                            break;
                        }
                    }
                }
            }
            return new Results(ID, side, start, end, answers, bi);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean validBubble(Color rgbavg)
    {
        int r = rgbavg.getRed(), g = rgbavg.getGreen(), b = rgbavg.getBlue();
        return r < threshold && g < threshold && b < threshold;
    }

    public static Color getRGBAvg(BufferedImage bi, Point2D.Double p)
    {
        double diameter = PROPS.keybase.get("bubble_radius");
        Ellipse2D.Double circle = new Ellipse2D.Double(p.getX() - diameter / 2, p.getY() - diameter / 2, diameter, diameter);
        double ra = 0, ba = 0, ga = 0;
        int num = 0;
        for (double x = circle.getX(); x < Math.round(circle.getX() + diameter); x++)
        {
            for (double y = circle.getY(); y < Math.round(circle.getY() + diameter); y++)
            {
                if (circle.contains(x, y))
                {
                    Color c = new Color(bi.getRGB((int) Math.round(x), (int) Math.round(y)));
                    ra += c.getRed();
                    ba += c.getBlue();
                    ga += c.getGreen();
                    num++;
                }
            }
        }
        ra /= num;
        ba /= num;
        ga /= num;
        Color rgbavg = new Color((int) ra, (int) ba, (int) ga);

        if (validBubble(rgbavg))
        {
            Graphics2D gd = bi.createGraphics();
            gd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gd.setStroke(new BasicStroke(2));
            //gd.setColor(rgbavg);
            //gd.fill(circle);
            gd.setColor(Color.RED);
            gd.draw(circle);
            gd.dispose();
        }
        return rgbavg;
    }

    public static Point2D.Double adjust(Point2D.Double init, int index)
    {
        return new Point2D.Double(init.getX() + bubble_space * index, init.getY());
    }
    public static double row_division = 0;
    public static double row_one_startx = 0;
    public static double row_one_starty = 0;
    public static double row_two_startx = 0;
    public static double row_two_starty = 0;

    public static Point2D.Double getFirstBubble(int num)
    {
        if (num <= 25)//row1 front
        {
            return new Point2D.Double(row_one_startx, row_division * (num - 1) + row_one_starty);
        }
        else if (num <= 50)//row2 front
        {
            num -= 25;
            return new Point2D.Double(row_two_startx, row_division * (num - 1) + row_two_starty);

        }
        else if (num <= 75)//row 1 back
        {
            num -= 50;
            return new Point2D.Double(117.1875, row_division * (num - 1) + 150);
        }
        else if (num <= 100)//row 2 back
        {
            num -= 75;
            return new Point2D.Double(290.625, row_division * (num - 1) + 173.4375);
        }
        return null;
    }
}
