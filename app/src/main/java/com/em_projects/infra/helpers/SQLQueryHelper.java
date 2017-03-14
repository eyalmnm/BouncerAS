package com.em_projects.infra.helpers;

import android.database.Cursor;
import android.util.Log;

import java.util.List;
import java.util.Vector;

public class SQLQueryHelper {
    private static final String TAG = "SQLQueryHelper";

    /**
     * Returns a IN selection part of a query.
     *
     * @param field (String != null) the field.
     * @param args  (String[] != null) the arguments.
     * @return (String != null) the IN part selection.
     */
    public static String createINSelection(String field, String[] args) {
        Log.d(TAG, "createINSelection");
        StringBuffer selectionBuff = new StringBuffer(field).
                append(" IN (\"");

        for (int i = 0; i < args.length - 1; i++) {
            selectionBuff.append(args[i])
                    .append("\",\"");
        }

        selectionBuff
                .append(args[args.length - 1])
                .append("\")");

        return selectionBuff.toString();
    }

    /**
     * Returns a LIKE selection.
     *
     * @param field (String != null) the field.
     * @param args  (String[] != null) the arguments.
     * @return (String != null) the LIKE selection.
     */
    public static String createLikeSelection(String field, String[] args, boolean fromLeft, boolean fromRight) {
        Log.d(TAG, "createLikeSelection");
        String like = " LIKE ";
        StringBuffer selectionBuff = new StringBuffer();

        if (args.length == 1) {
            return selectionBuff
                    .append(" ")
                    .append(field)
                    .append(like)
                    .append("'")
                    .append(fromLeft ? "%" : "")
                    .append(args[0])
                    .append(fromRight ? "%" : "")
                    .append("'").toString();
        }


        for (int i = 0; i < args.length - 1; i++) {
            selectionBuff.append(" (")
                    .append(field).append(like)
                    .append("'")
                    .append(fromLeft ? "%" : "")
                    .append(args[i])
                    .append(fromRight ? "%" : "")
                    .append("'").append(") OR");
        }

        selectionBuff.append(" (")
                .append(field).append(like)
                .append("'")
                .append(fromLeft ? "%" : "")
                .append(args[args.length - 1])
                .append(fromRight ? "%" : "")
                .append("'").append(")");

        return selectionBuff.toString();
    }

    /**
     * <p>Gets given column values as strings.</p>
     * <p>Note: the user is responsible for closing the cursor.</p>
     *
     * @param c          (Cursor) The cursor to read the data from.
     * @param columnName (String != null) The name of the column whose values to retreive.
     * @param isDistinct (boolean) Whether to retreive distinct values or not.
     * @return (List<String>) Columns values as strings collection. The collection might be empty.
     */
    public static List<String> getValuesFromColumn(Cursor c, String columnName, boolean isDistinct) {
        Log.d(TAG, "getValuesFromColumn");
        //holds values
        Vector<String> values = new Vector<String>();

        //in case the cursor has data
        if (c != null && c.moveToFirst()) {
            do {
                //get the column value
                String value = c.getString(c.getColumnIndex(columnName));

                //in case we want distinct values
                if (isDistinct) {
                    //add the value only if it is not already in the collection
                    if (!values.contains(value))
                        values.add(value);
                } else {
                    //add the values to the collection
                    values.add(value);
                }
            }
            while (c.moveToNext());
        }

        //return values
        return values;
    }
}
