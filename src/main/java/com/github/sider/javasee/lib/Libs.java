package com.github.sider.javasee.lib;

import com.github.sider.javasee.Exceptions;

public class Libs {
    public static interface SupplierWithException<T> {
        T get() throws Exception;
    }

    public static interface SupplierWithPolymorphicException <T, U extends Exception> {
        T get() throws U;
    }

    public static <T> T wrapException(SupplierWithException<T> block) {
        try {
            return block.get();
        } catch (RuntimeException e) {
            //rethrow
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String ordinalize(int i) {
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];

        }
    }
}
