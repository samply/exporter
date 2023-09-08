package de.samply.utils;

import java.util.Base64;

public class Base64Utils {

    public static String decode (String element){
        return new String(Base64.getDecoder().decode(element));
    }

    public static String decode(byte[] element){
        return new String(Base64.getDecoder().decode(element));
    }

    public static byte[] encode(String element){
        return Base64.getEncoder().encode(element.getBytes());
    }

}
