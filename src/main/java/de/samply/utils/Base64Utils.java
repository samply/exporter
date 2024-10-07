package de.samply.utils;

import java.util.Base64;

public class Base64Utils {

    public static String decode (String element){
        return (element != null) ? new String(Base64.getDecoder().decode(element)) : null;
    }

    public static String decodeIfNecessary(String element) {
        try {
            return (element != null) ? Base64Utils.decode(element) : null;
        } catch (IllegalArgumentException e) {
            return element;
        }
    }

    public static String decode(byte[] element){
        return (element != null) ? new String(Base64.getDecoder().decode(element)) : null;
    }

    public static byte[] encode(String element){
        return (element != null) ? Base64.getEncoder().encode(element.getBytes()) : new byte[0];
    }

}
