/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javastego;

import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.image4j.codec.bmp.BMPDecoder;
import net.sf.image4j.codec.bmp.BMPEncoder;

/**
 *
 * @author USER
 */
public class JavaStego {

    /**
     * @param args the command line arguments
     */
    
    //BufferedImage buf;
    int[][] MatPixel;
    int height;
    int width;
    
    public static void main(String[] args) {
        String in = "baboon.bmp";
        String ou = "babun.bmp";
        
        JavaStego js = new JavaStego(in);
        js.iseng();
        js.Export(ou);
    }
    
    public JavaStego() {
        //buf = null;
        MatPixel = new int[0][0];
    }
    
    public JavaStego(String filename) {
        BufferedImage buf = null;
        File f = new File(filename);
        try {
            buf = BMPDecoder.read(f);
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }
        height = buf.getHeight();
        width = buf.getWidth();
        MatPixel = new int[height][width];
        for(int i=0; i<height; i++) {
            for(int j=0; j<height; j++) {
                MatPixel[i][j] = buf.getRGB(i,j);
            }
        }
    }
    
    public void iseng() {
        int temp[][] = new int[height][width];
        for(int i=0; i<height; i++) {
            for(int j=0; j<height; j++) {
                temp[i][j] = MatPixel[i][j];
            }
        }
        
        for(int i=0; i<height; i++) {
            for(int j=0; j<height; j++) {
                MatPixel[i][j] &= ~1;
            }
        }
        
        for(int i=0; i<height; i++) {
            for(int j=0; j<height; j++) {
                System.out.println(MatPixel[i][j]+" "+temp[i][j]);
            }
        }
    }
    
    public void Import(String filename) {
        BufferedImage buf = null;
        File f = new File(filename);
        try {
            buf = BMPDecoder.read(f);
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }
        height = buf.getHeight();
        width = buf.getWidth();
        MatPixel = new int[height][width];
        for(int i=0; i<height; i++) {
            for(int j=0; j<height; j++) {
                MatPixel[i][j] = buf.getRGB(i,j);               
            }
        }
    }
    
    public void Export(String filename) {
        BufferedImage buf = new BufferedImage(height,width,TYPE_INT_RGB);
        int px;
        for(int i=0; i<height; i++) {
            for(int j=0; j<height; j++) {
                px = MatPixel[i][j];
                buf.setRGB(i,j,px);               
            }
        }        
        
        File f = new File(filename);
        try {
            BMPEncoder.write(buf, f);
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
