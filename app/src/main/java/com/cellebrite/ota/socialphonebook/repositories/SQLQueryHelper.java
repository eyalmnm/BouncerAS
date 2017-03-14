package com.cellebrite.ota.socialphonebook.repositories;

public class SQLQueryHelper {
    /**
     * Holds a constant for a descending sort order.
     */
    public static final String DESC_SORT_ORDER = "DESC";

    /**
     * Holds a constant for a ascending sort order.
     */
    public static final String ASC_SORT_ORDER = "ASC";

    /**
     * Returns a IN selection part of a query.
     *
     * @param field (String != null) the field.
     * @param args  (String[] != null) the arguments.
     * @return (String != null) the IN part selection.
     */
    public static String createINSelection(String field, String[] args) {
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
}
