/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javastego;

import java.awt.List;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
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
    int type;
    BufferedImage buf;
    public String skey;
  /*  public static void main(String[] args) {
        String in = "baboon.bmp";
        String ou = "babun.bmp";
        Stego js = new Stego(in);
        
        js.readHiddenText("test.txt");
        js.setKey(10);
        js.setStego();
        js.Export(ou);
        Stego jb = new Stego(ou);
        jb.setKey(10);
        
        System.out.println(jb.getStego());
        
    }*/
    
    public static void main(String[] args) {
        // TODO code application logic here
        JFrame a = new JFrame();
        View n = new View();
        n.init();
        a.getContentPane().add(n);
        a.pack();
        a.setVisible(true);
    }
    
    public Stego() {
        //buf = null;
        ht = new HiddenText();
    }
    
    public Stego(String filename) {
        BufferedImage buf = null;
        File f = new File(filename);
        //baca file bmp
        try {
            buf = BMPDecoder.read(f);
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //dapetin ukurannya
        height = buf.getHeight();
        width = buf.getWidth();
        
        MatPixel = new ColorStore[height][width];
        
        
        //inisialisasi
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                MatPixel[i][j] = new ColorStore(buf.getRGB(i,j));     
            }
        }
        ht = new HiddenText();
    }
    
    public void Import(String filename) {
        buf = null;
        File f = new File(filename);
        
        try {
            buf = BMPDecoder.read(f);
            type = buf.getType();
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }
        //dapetin ukurannya
        height = buf.getHeight();
        width = buf.getWidth();
        
        MatPixel = new ColorStore[height][width];
        
        
        //inisialisasi
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                MatPixel[i][j] = new ColorStore(buf.getRGB(i,j));     
            }
        }
    }
    
    public void Export(String filename) {
        int px;
        
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                px = MatPixel[i][j].getColor();
                buf.setRGB(i,j,px);               
            }
        }
        File f = new File(filename);
        //tulis file
        try {
            BMPEncoder.write(buf, f);
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void readHiddenText(String filename){
        //baca file teks yang ingin disisipkan
        try {
            byte[] data = Files.readAllBytes(Paths.get(filename));
            ht.setData(data);
        } catch (IOException ex) {
            Logger.getLogger(Stego.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setStego(){
        //membangkitkan bilangan random untuk posisi
        if(type==BufferedImage.TYPE_BYTE_INDEXED||type==BufferedImage.TYPE_BYTE_GRAY){
            Random rng = new Random(key); // Ideally just create one instance globally
            // Note: use LinkedHashSet to maintain insertion order
            ArrayList<Integer> generated = new ArrayList<Integer>();
            for(int i=0;i<height*width*3;i++){
                generated.add(i);
            }
            for(int i=0;i<generated.size();i++){
                int temp = generated.get(i);
                int randtemp = rng.nextInt(height*width*3);
                generated.set(i, generated.get(randtemp));
                generated.set(randtemp, temp);
            }
            String s = Integer.toBinaryString(ht.getLength());
            //masukin ukurannya
            int gencon = 0;
            int i;
            for(i=0;i<64-s.length();i++){
            //    System.out.println(i);
                int randtemp = generated.get(gencon);
                gencon++;
                int x = (randtemp) / height;
                int y = (randtemp) % height;

                MatPixel[x][y].setLSBRed(false);

                MatPixel[x][y].setLSBGreen(false);

                MatPixel[x][y].setLSBBlue(false);
            }
            for(i=64-s.length();i<64;i++){
                int randtemp = generated.get(gencon);
                gencon++;
                int x = (randtemp) / height;
                int y = (randtemp) % height;
                MatPixel[x][y].setLSBRed(s.charAt(i+s.length()-64)=='1');
                MatPixel[x][y].setLSBGreen(s.charAt(i+s.length()-64)=='1');
                MatPixel[x][y].setLSBBlue(s.charAt(i+s.length()-64)=='1');
            }
            //masukin teksnya
            while(ht.isNext()){


                int randtemp = generated.get(gencon);
                gencon++;

                int x = (randtemp) / height;
                int y = (randtemp) % height;
                boolean tempBit = ht.getNextBit();
                MatPixel[x][y].setLSBRed(tempBit);

                MatPixel[x][y].setLSBGreen(tempBit);

                
                MatPixel[x][y].setLSBBlue(tempBit);

                
                i++;
            }
        }
        else{
            Random rng = new Random(key); // Ideally just create one instance globally
            // Note: use LinkedHashSet to maintain insertion order
            ArrayList<Integer> generated = new ArrayList<Integer>();
            for(int i=0;i<height*width*3;i++){
                generated.add(i);
            }
            for(int i=0;i<generated.size();i++){
                int temp = generated.get(i);
                int randtemp = rng.nextInt(height*width*3);
                generated.set(i, generated.get(randtemp));
                generated.set(randtemp, temp);
            }
            String s = Integer.toBinaryString(ht.getLength());
            //masukin ukurannya
            int gencon = 0;
            int i;
            for(i=0;i<64-s.length();i++){
            //    System.out.println(i);
                int randtemp = generated.get(gencon);
                gencon++;
                int x = (randtemp/3) / height;
                int y = (randtemp/3) % height;

                if(randtemp%3==0){
                    MatPixel[x][y].setLSBRed(false);
                }
                else if(randtemp%3==1){
                    MatPixel[x][y].setLSBGreen(false);
                }
                else{
                    MatPixel[x][y].setLSBBlue(false);
                }
            }
            for(i=64-s.length();i<64;i++){
                int randtemp = generated.get(gencon);
                gencon++;
                int x = (randtemp/3) / height;
                int y = (randtemp/3) % height;
                if(randtemp%3==0){
                    MatPixel[x][y].setLSBRed(s.charAt(i+s.length()-64)=='1');
                }
                else if(randtemp%3==1){
                    MatPixel[x][y].setLSBGreen(s.charAt(i+s.length()-64)=='1');
                }
                else{
                    MatPixel[x][y].setLSBBlue(s.charAt(i+s.length()-64)=='1');
                }
            }
            //masukin teksnya
            while(ht.isNext()){


                int randtemp = generated.get(gencon);
                gencon++;

                int x = (randtemp/3) / height;
                int y = (randtemp/3) % height;
                if(randtemp%3==0){
                    MatPixel[x][y].setLSBRed(ht.getNextBit());

                }
                else if(randtemp%3==1){
                    MatPixel[x][y].setLSBGreen(ht.getNextBit());

                }
                else{
                    MatPixel[x][y].setLSBBlue(ht.getNextBit());

                }
                i++;
            }
        }
    }
    
    public String getStego(){
        Random rand = new Random(key);
        Random rng = new Random(key); // Ideally just create one instance globally
        // Note: use LinkedHashSet to maintain insertion order
        if(type==BufferedImage.TYPE_BYTE_INDEXED||type==BufferedImage.TYPE_BYTE_GRAY)
        {
            ArrayList<Integer> generated = new ArrayList<Integer>();
            for(int i=0;i<height*width*3;i++){
                generated.add(i);
            }
            for(int i=0;i<generated.size();i++){
                int temp = generated.get(i);
                int randtemp = rng.nextInt(height*width*3);
                generated.set(i, generated.get(randtemp));
                generated.set(randtemp, temp);
            }
            int i;
            int n = 0;
            int temp;
            int gencon = 0;

            //membaca ukurannya
            for(i=0;i<64;i++){
                n = n << 1;
                int randtemp = generated.get(gencon);
                gencon++;
                int x = (randtemp) / height;
                int y = (randtemp) % height;

                if(MatPixel[x][y].getLSBRed()){
                    n += 1;
                }

            //    System.out.println(n);
            }
            System.out.println(n);
            
            int charCount = 0;
            int tempChar = 0;
            String ret = "";
            for(i=0;i<n;i++){


                tempChar = tempChar<<1  ;
                int randtemp = generated.get(gencon);
                gencon++;
                int x = (randtemp) / height;
                int y = (randtemp) % height;
                //if(i==0 || i==1) System.out.println(randtemp);
                if(MatPixel[x][y].getLSBRed()){
                     tempChar += 1;
                }
                charCount++;
                //ambil perbyte yaitu 8
                if(charCount == 8 ){
                     char a = (char) tempChar;
                    charCount = 0;
                    ret+=a;
                    tempChar = 0;
                }
            }
            int keyIdx = 0;


          /*  for(i=0; i<ret.length(); i++){
                    ret += (char)((ret.charAt(i)+skey.charAt(keyIdx%skey.length()))%256);
                    keyIdx++;
            }*/
            return ret;
        }
        else{
            ArrayList<Integer> generated = new ArrayList<Integer>();
            for(int i=0;i<height*width*3;i++){
                generated.add(i);
            }
            for(int i=0;i<generated.size();i++){
                int temp = generated.get(i);
                int randtemp = rng.nextInt(height*width*3);
                generated.set(i, generated.get(randtemp));
                generated.set(randtemp, temp);
            }
            int i;
            int n = 0;
            int temp;
            int gencon = 0;

            //membaca ukurannya
            for(i=0;i<64;i++){
                n = n << 1;
                int randtemp = generated.get(gencon);
                gencon++;
                int x = (randtemp/3) / height;
                int y = (randtemp/3) % height;
                if(randtemp%3==0){
                    if(MatPixel[x][y].getLSBRed()){
                        n += 1;
                    }



                }
                else if(randtemp%3==1){
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
                int randtemp = generated.get(gencon);
                gencon++;
                int x = (randtemp/3) / height;
                int y = (randtemp/3) % height;
                //if(i==0 || i==1) System.out.println(randtemp);
                if(randtemp%3==0){
                    if(MatPixel[x][y].getLSBRed()){
                        tempChar += 1;
                    }

                }
                else if(randtemp%3==1){
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
                //ambil perbyte yaitu 8
                if(charCount == 8 ){
                     char a = (char) tempChar;
                    charCount = 0;
                    ret+=a;
                    tempChar = 0;
                }
            }
            int keyIdx = 0;


          /*  for(i=0; i<ret.length(); i++){
                    ret += (char)((ret.charAt(i)-skey.charAt(keyIdx%skey.length())));
                    keyIdx++;
            }*/
            return ret;
        }
    }
    
    public void setKey(int k){
        key = k;
    }
    
    public byte[] getAllByte(){
        byte[] a = new byte[height*width*4];
        int count = 0 ;
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                byte[] tempb = ByteBuffer.allocate(4).putInt(MatPixel[i][j].getColor()).array();
                for(int k=0;k<4;k++){
                    a[count] = tempb[k];
                    count++;
                }
            }
        }
        return a;
    }
    
    public int getMaxSize(){
        return height*width*3/1024;
    }
    
    public int getTextSize(){
        return (ht.getLength()/8)/1024;
    }
    
    public void encrypt(String key){
        ht.encrypt(key);
    }
    
    public void decrypt(String key){
        ht.decrypt(key);
    }
    
    
}
