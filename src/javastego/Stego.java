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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.image4j.codec.bmp.BMPDecoder;
import net.sf.image4j.codec.bmp.BMPEncoder;

/**
 *
 * @author USER
 */
public class Stego {

    /**
     * @param args the command line arguments
     */
    
    //BufferedImage buf;
    HiddenText ht;
    ColorStore[][] MatPixel;
    int height;
    int width;
    
    public static void main(String[] args) {
        String in = "baboon.bmp";
        String ou = "babun.bmp";
        Stego js = new Stego(in);
        js.readHiddenText("test.txt");
        js.setStego();
        System.out.println(js.height+" "+js.width);
        js.Export(ou);
    }
    
    public Stego() {
        //buf = null;
        MatPixel = new ColorStore[0][0];
        ht = new HiddenText();
    }
    
    public Stego(String filename) {
        BufferedImage buf = null;
        File f = new File(filename);
        try {
            buf = BMPDecoder.read(f);
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }
        height = buf.getHeight();
        width = buf.getWidth();
        
        MatPixel = new ColorStore[height][width];
        
        
        
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                MatPixel[i][j] = new ColorStore(buf.getRGB(i,j));     
            }
        }
        ht = new HiddenText();
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
        MatPixel = new ColorStore[height][width];
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                MatPixel[i][j].setColor(buf.getRGB(i,j));
                               
            }
        }
    }
    
    public void Export(String filename) {
        BufferedImage buf = new BufferedImage(height,width,TYPE_INT_RGB);
        int px;
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                px = MatPixel[i][j].getColor();
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
    
    public void readHiddenText(String filename){
        try {
            byte[] data = Files.readAllBytes(Paths.get(filename));
            ht.setData(data);
        } catch (IOException ex) {
            Logger.getLogger(Stego.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setStego(){
        for(int i=0; i<height&&ht.isNext(); i++) {
            for(int j=0; j<width&&ht.isNext(); j++) {
                if(ht.isNext()){
                    MatPixel[i][j].setLSBRed(ht.getNextBit());
                }
                if(ht.isNext()){
                    MatPixel[i][j].setLSBGreen(ht.getNextBit());
                }
                if(ht.isNext()){
                    MatPixel[i][j].setLSBBlue(ht.getNextBit());
                }
            }
        }
    }
    
    public String getStego(){
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                
            }
        }
        String a = "";
        return a;
    }
    
}
