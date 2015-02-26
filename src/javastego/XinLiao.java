/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javastego;

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
    
    public static void embedMessage(String cipher, String inputFilename, String outputFilename){
        float D, D_1, D_2, D_3;
    }
    
    private static float calculateD(int y0, int y1, int y2, int y3){
        float D=1;
        return D;
    }
    
    private static int min4(int y0, int y1, int y2, int y3){
        int ymin=0;
        return ymin;
    }
    
    private static int max4(int y0, int y1, int y2, int y3){
        int ymax = 0;
        return ymax;
    }
}
