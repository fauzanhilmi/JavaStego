/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javastego;

import static java.lang.Integer.max;
import static java.lang.Integer.min;


/**
 *
 * @author Yusuf
 */
public class XinLiao {
    
    /**
     * mengikuti design pattern singleton
     */
    private static XinLiao instance = null;
    private static int treshold = 7;
    private static int kLow = 2;
    private static int kHigh = 3;
    
    
    protected XinLiao(){
        
    }
    
    public static XinLiao getInstance(){
        if(instance == null){
            instance = new XinLiao();
        }
        return instance;
    }
    
    public static void embedMessage(String message, String key, String coverImage, String stegoImage){
        float D, D_1, D_2, D_3;
    }
    
    public static String extractMessage(String key, String stegoImage, String coverImage){
        String plainText="";
        
        return plainText;
    }
    
    private static float calculateD(int y0, int y1, int y2, int y3){
        float D;
        int yMin = min4(y0, y1, y2, y3);
        int sumY = (y0 - yMin) + (y1 - yMin) + (y2 - yMin) + (y3 - yMin);
        
        D = sumY/3;
        return D;
    }
    
    private static boolean isErrorBlock(float D, int yMin, int yMax){
        boolean retval=false;
        
        return retval;
    }
    
    private static char[] addBitLSB(String message, int k){
        int size = message.length() * 8;
        char [] retval = new char[size];
        return retval;
    }
    
    private static int min4(int y0, int y1, int y2, int y3){
        return min(y0, min(y1, min(y2, y3)));
    }
    
    private static int max4(int y0, int y1, int y2, int y3){
        return max(y0, max(y1, max(y1, y3)));
    }
}
