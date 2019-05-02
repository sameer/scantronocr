package me.sameer.main;

import java.util.*;
import javax.swing.table.AbstractTableModel;

/**
 * @author Sameer Puri
 */
public class StudentResultsTableModel extends AbstractTableModel
{

    public Results r;

    public StudentResultsTableModel(Results r)
    {
        this.r = r;
    }

    @Override
    public int getRowCount()
    {
        return r.answers.size() + 1;
    }

    @Override
    public int getColumnCount()
    {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (r.ID.length() > 0)
        {
            if (rowIndex == 0)
            {
                if (columnIndex == 0)
                {
                    return "ID";
                }
                else
                {
                    return r.ID;
                }
            }
            else if (columnIndex == 0)
            {
                return rowIndex;
            }
            else
            {
                return r.answers.get(rowIndex-1);
            }
        }
        else if (columnIndex == 0)
        {
            return rowIndex+1;
        }
        else
        {
            return r.answers.get(rowIndex);
        }
    }
}
