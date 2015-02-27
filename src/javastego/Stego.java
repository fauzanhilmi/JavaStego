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
import java.util.Random;
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
    int key;
    
    /*public static void main(String[] args) {
        String in = "baboon.bmp";
        String ou = "babun.bmp";
        Stego js = new Stego(in);
        js.readHiddenText("test.txt");
        js.setStego();
        System.out.println(js.height+" "+js.width);
        
        js.readHiddenText("test.txt");
        js.setKey(10);
        js.setStego();
        Stego jb = new Stego(ou);
        jb.setKey(10);
        
        System.out.println(jb.getStego());
        js.Export(ou);
    }*/
    
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
        int i;
        for(i=0; i<height&&ht.isNext(); i++) {
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
        Random rand = new Random(key);
        String s = Integer.toBinaryString(ht.getLength());
        
        for(i=0;i<64-s.length();i++){
        //    System.out.println(i);
            int randtemp = rand.nextInt(height*width);
            int x = randtemp / height;
            int y = randtemp % height;
           
            if(i%3==0){
                MatPixel[x][y].setLSBRed(false);
            }
            else if(i%3==1){
                MatPixel[x][y].setLSBGreen(false);
            }
            else{
                MatPixel[x][y].setLSBBlue(false);
            }
        }
        for(i=64-s.length();i<64;i++){
            int randtemp = rand.nextInt(height*width);
            int x = randtemp / height;
            int y = randtemp % height;
            if(i%3==0){
                MatPixel[x][y].setLSBRed(s.charAt(i+s.length()-64)=='1');
            }
            else if(i%3==1){
                MatPixel[x][y].setLSBGreen(s.charAt(i+s.length()-64)=='1');
            }
            else{
                MatPixel[x][y].setLSBBlue(s.charAt(i+s.length()-64)=='1');
            }
        }
        int count = 0;
        while(ht.isNext()){
            
            
            int randtemp = rand.nextInt(height*width);
             
            int x = randtemp / height;
            int y = randtemp % height;
            if(count%3==0){
                MatPixel[x][y].setLSBRed(ht.getNextBit());
                
            }
            else if(count%3==1){
                MatPixel[x][y].setLSBGreen(ht.getNextBit());
                
            }
            else{
                MatPixel[x][y].setLSBBlue(ht.getNextBit());
               
            }
            count++;
            i++;
        }
    }
    }
    
    public String getStego(){    
        Random rand = new Random(key);
        int i;
        int n = 0;
        int temp;
        for(i=0;i<64;i++){
            n = n << 1;
            int randtemp = rand.nextInt(height*width);
            int x = randtemp / height;
            int y = randtemp % height;
            if(i%3==0){
                if(MatPixel[x][y].getLSBRed()){
                    n += 1;
                }
                 
                
                
            }
            else if(i%3==1){
                if(MatPixel[x][y].getLSBGreen()){
                    n += 1;      
                }
                 
            }
            else{
                if(MatPixel[x][y].getLSBBlue()){
                    n += 1;
                }
             
                    
            }
        //    System.out.println(n);
        }
        System.out.println(n);
        int charCount = 0;
        int tempChar = 0;
        String ret = "";
        for(i=0;i<n;i++){
            
            
            tempChar = tempChar<<1  ;
            int randtemp = rand.nextInt(height*width);
            int x = randtemp / height;
            int y = randtemp % height;
            //if(i==0 || i==1) System.out.println(randtemp);
            if(i%3==0){
                if(MatPixel[x][y].getLSBRed()){
                    tempChar += 1;
                }
                
            }
            else if(i%3==1){
                if(MatPixel[x][y].getLSBGreen()){
                    tempChar += 1;
                }
            }
            else{
                if(MatPixel[x][y].getLSBBlue()){
                    tempChar += 1;  
                }
            }
            charCount++;
            
            if(charCount == 8 ){
                 char a = (char) tempChar;
                charCount = 0;
                ret+=a;
                tempChar = 0;
            }
        }
        return ret;
    }
    
    public void setKey(int k){
        key = k;
    }
}
