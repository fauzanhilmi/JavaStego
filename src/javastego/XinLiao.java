/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javastego;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.image4j.codec.bmp.BMPDecoder;
import net.sf.image4j.codec.bmp.BMPEncoder;


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
    private static int k;
    private static int mode = 3;
    
    protected XinLiao(){
        
    }
    
    public static XinLiao getInstance(){
        if(instance == null){
            instance = new XinLiao();
        }
        return instance;
    }
    
    public static int embedMessage(String message, String key, String coverImage, String stegoImage){
        int messageLength=0;
        float D, D_1;
        File coverFile, stegoFile;
        BufferedImage buf;
        int height, width = 0;
        String cipher, bitString;
        
        coverFile = new File(coverImage);
        stegoFile = new File(stegoImage);
        
        try {
            buf = BMPDecoder.read(coverFile);
            height = buf.getHeight();
            width = buf.getWidth();
            cipher = encryptExtended(message, key);
            bitString = convertStringToBitString(cipher);
            for(int i=0; i<height; i+=2){
                for(int j=0; j<width; j+=2){
                    if(i+1 != height && j+1 != width && bitString.length()!=0){
                        int [] y = new int[4];
                        int [] yt = new int[4];
                        int []bitStore = new int[4];
                        int redLSBnullifier, blueLSBnullifier, greenLSBnullifier;
                        
                        y[0] = buf.getRGB(i, j);
                        y[1] = buf.getRGB(i, j+1);
                        y[2] = buf.getRGB(i+1, j);
                        y[3] = buf.getRGB(i+1, j+1);
                        D = calculateD(y[0], y[1], y[2], y[3]);
                        System.out.println("y[0] : "+Integer.toHexString(y[0]));
                        System.out.println("y[1] : "+Integer.toHexString(y[1]));
                        System.out.println("y[2] : "+Integer.toHexString(y[2]));
                        System.out.println("y[3] : "+Integer.toHexString(y[3]));
                        
                        //menentukan k
                        if(D>=treshold)
                            k = kLow;
                        else
                            k = kHigh;
                        
                        //tidak menghitung error block dengan asumsi block selalu benar
                        //hal ini dilakukan agar treshold, kLow, dan kHigh bernilai tetap
                        
                        //menggenapkan bitString
                            if((bitString.length()%(12*k)) != 0){
                                int sisa = 12*k - (bitString.length()%(12*k));
                                for(int a=0; a<sisa; a++)
                                    bitString+="0";
                            }
                            
                        //menentukan LSBnullifier
                        redLSBnullifier = 0xFFFFFFFF;
                        redLSBnullifier <<= k+16;
                        redLSBnullifier += 0xFFFF;
                        
                        greenLSBnullifier = 0xFFFFFFFF;
                        greenLSBnullifier <<= k+8;
                        greenLSBnullifier += 0xFF;
                        
                        blueLSBnullifier = 0xFFFFFFFF;
                        blueLSBnullifier <<= k;
                        
                        messageLength += bitString.length();
                        
                        for(int yIdx = 0; yIdx<4; yIdx++){
                            int temp;
                            bitStore[0] = convertStringToByte(bitString.substring(0, k));
                            bitStore[1] = convertStringToByte(bitString.substring(k, 2*k));
                            if(bitString.length()>k)
                                bitStore[2] = convertStringToByte(bitString.substring(2*k, 3*k));
                            else
                                bitStore[2] = convertStringToByte(bitString.substring(2*k));
                            
                            if(bitString.length()>0)
                                bitString = bitString.substring(3*k);
                            
                            //untuk warna merah
                            temp = 0xFFFFFFFF << k+16;
                            bitStore[0] <<= 16;
                            bitStore[0] += temp;
                            bitStore[0] += 0xFFFF;
                            
                            //untuk warna hijau
                            temp = 0xFFFFFFFF << k+8;
                            bitStore[1] <<= 8;
                            bitStore[1] += temp;
                            bitStore[1] += 0xFF;
                            
                            //untuk warna biru
                            temp = 0xFFFFFFFF << k;
                            bitStore[2] += temp;
                            
                            //mengubah y menjadi y'
                            yt[yIdx] = y[yIdx] & redLSBnullifier & greenLSBnullifier & blueLSBnullifier & bitStore[0] & bitStore[1] & bitStore[2];
                            
                            //mengubah y' menjadi y" dengan modified LSB substitution
                            if(isModifiable(y[yIdx])){
                                int min, max, MSB, yTemp;
                                
                                //untuk warna merah
                                MSB = 1 << k+16;
                                min = yt[yIdx] - MSB;
                                max = yt[yIdx] + MSB;
                                if(Math.abs(min - y[yIdx]) < Math.abs(max - y[yIdx])){
                                    if(Math.abs(min - y[yIdx]) < Math.abs(yt[yIdx]-y[yIdx]))
                                        yt[yIdx] = min;
                                }else{
                                    if(Math.abs(max - y[yIdx]) < Math.abs(yt[yIdx]-y[yIdx]))
                                        yt[yIdx] = max;
                                }
                                
                                //untuk warna hijau
                                MSB = 1 << k+8;
                                min = yt[yIdx] - MSB;
                                max = yt[yIdx] + MSB;
                                if(Math.abs(min - y[yIdx]) < Math.abs(max - y[yIdx])){
                                    if(Math.abs(min - y[yIdx]) < Math.abs(yt[yIdx]-y[yIdx]))
                                        yt[yIdx] = min;
                                }else{
                                    if(Math.abs(max - y[yIdx]) < Math.abs(yt[yIdx]-y[yIdx]))
                                        yt[yIdx] = max;
                                }
                                
                                //untuk warna biru
                                MSB = 1 << k;
                                min = yt[yIdx] - MSB;
                                max = yt[yIdx] + MSB;
                                if(Math.abs(min - y[yIdx]) < Math.abs(max - y[yIdx])){
                                    if(Math.abs(min - y[yIdx]) < Math.abs(yt[yIdx]-y[yIdx]))
                                        yt[yIdx] = min;
                                }else{
                                    if(Math.abs(max - y[yIdx]) < Math.abs(yt[yIdx]-y[yIdx]))
                                        yt[yIdx] = max;
                                }
                            }                            
                        }
                        
                        //mencari nilai l
                        int lMin = -1;
                        int minimized = Integer.MAX_VALUE;
                        int maxBit = (int)(Math.pow(2, k));
                        for(int l=-1; l<=1; l++){
                            int [] yx = new int[4];
                            
                            for(int yIdx = 0; yIdx<4; yIdx++){
                                yx[yIdx] = yt[yIdx] + l*maxBit;
                            }
                            D_1 = calculateD(yx[0], yx[1], yx[2], yx[3]);
                            if((D>= treshold && D_1 >= treshold 
                                || D<treshold && D_1 < treshold)
                                && !isErrorBlock(D_1, min4(yx[0], yx[1], yx[2], yx[3]), max4(yx[0], yx[1], yx[2], yx[3]))){
                                int sum=0;
                                for(int yIdx = 0; yIdx<4; yIdx++){
                                    sum += (yx[yIdx]-y[yIdx])*(yx[yIdx]-y[yIdx]);
                                }
                                if(sum < minimized){
                                    minimized = sum;
                                    lMin = l;
                                }
                            }
                        }
                        
                        //memasukkan nilai MatPixel berdasarkan nilai l
                        System.out.println("yt[0] : "+Integer.toHexString(yt[0])+ lMin*maxBit);
                        System.out.println("yt[1] : "+Integer.toHexString(yt[1])+ lMin*maxBit);
                        System.out.println("yt[2] : "+Integer.toHexString(yt[2])+ lMin*maxBit);
                        System.out.println("yt[3] : "+Integer.toHexString(yt[3])+ lMin*maxBit);
                        buf.setRGB(i, j, yt[0] + lMin*maxBit);
                        buf.setRGB(i, j+1, yt[1] + lMin*maxBit);
                        buf.setRGB(i+1, j, yt[2] + lMin*maxBit);
                        buf.setRGB(i+1, j+1, yt[3] + lMin*maxBit);
                    }
                }
            }
            //menuliskan buf ke dalam stegoFile
            BMPEncoder.write(buf, stegoFile);
            buf.flush();
        } catch (IOException ex) {
            Logger.getLogger(XinLiao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (messageLength-1)/8;
    }
    
    public static String extractMessage(String key, int messageLength, String stegoImage){
        String plainText="";
         float D, D_1;
        File stegoFile;
        BufferedImage buf;
        int height, width = 0;
        String bitString="";
        String [] stream = new String[messageLength];
        
        stegoFile = new File(stegoImage);
        
        try {
            buf = BMPDecoder.read(stegoFile);
            height = buf.getHeight();
            width = buf.getWidth();
            messageLength *= 8;
            for(int i=0; i<height; i+=2){
                for(int j=0; j<width; j+=2){
                    if(i+1 != height && j+1 != width && messageLength!=0){
                        int [] y = new int[4];
                        int [] yt = new int[4];
                        int []bitStore = new int[4];
                        int getter=0;
                        
                        
                        y[0] = buf.getRGB(i, j);
                        y[1] = buf.getRGB(i, j+1);
                        y[2] = buf.getRGB(i+1, j);
                        y[3] = buf.getRGB(i+1, j+1);
                        D = calculateD(y[0], y[1], y[2], y[3]);
                        
                        //menentukan k
                        if(D>=treshold)
                            k = kLow;
                        else
                            k = kHigh;
                        
                        for(int idx=0; idx<k; idx++){
                            getter <<= 1;
                            getter++;
                        }
                        
                        for(int yi = 0; yi<4; yi++){
                            int R,G,B;
                            String temp="";
                            
                            R = y[yi]&(getter << 16);
                            G = y[yi]&(getter << 8);
                            B = y[yi]&getter;
                            System.out.println("ex-y["+yi+"] : "+Integer.toHexString(y[yi]));
                            //ambil dari warna merah
                            temp = Integer.toBinaryString(R);
                            if(temp.length() < k){
                                int sisa = k - temp.length();
                                for(int si=0; si<sisa; si++)
                                    temp = "0"+temp;
                            }
                            bitString += temp;
                            
                            //ambil dari warna hijau
                            temp = Integer.toBinaryString(G);
                            if(temp.length() < k){
                                int sisa = k - temp.length();
                                for(int si=0; si<sisa; si++)
                                    temp = "0"+temp;
                            }
                            bitString += temp;
                            
                            //ambil dari warna biru
                            temp = Integer.toBinaryString(B);
                            if(temp.length() < k){
                                int sisa = k - temp.length();
                                for(int si=0; si<sisa; si++)
                                    temp = "0"+temp;
                            }
                            bitString += temp;
                        }
                        messageLength -= 12*k;
                    }
                }
            }
            System.out.println(bitString);
            //mengubah bitString jadi String
            for(int i=0; i<stream.length; i++){
                if(bitString.length() >= 8)
                    stream[i] = convertKBitStringToString(8, bitString.substring(0,8));
                else
                    stream[i] = convertKBitStringToString(8, bitString.substring(0));
                if(bitString.length() >= 8)
                    bitString = bitString.substring(8);
                plainText += stream[i];
            }
            buf.flush();
        } catch (IOException ex) {
            Logger.getLogger(XinLiao.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return decryptExtended(plainText, key);
    }
    
    private static float calculateD(int y0, int y1, int y2, int y3){
        float D;
        int yMin = min4(y0, y1, y2, y3);
        int sumY = (y0 - yMin) + (y1 - yMin) + (y2 - yMin) + (y3 - yMin);
        
        D = sumY/3;
        return D;
    }
    
    private static boolean isErrorBlock(float D, int yMin, int yMax){
        return (D<=treshold && (yMax - yMin) > (2*treshold+2));
    }
        
    private static String convertStringToBitString(String message){
        String retval = "";
        String dump = "";
        
        for(int i=0; i<message.length(); i++){
            int sisa;
            String sisaString = "";
            
            dump = Integer.toBinaryString((int)message.charAt(i));
            sisa = 8 - dump.length();
            if(sisa > 0){
                for(int j=0; j<sisa; j++)
                    sisaString += "0";
                dump = sisaString + dump;
            }
            retval += dump;
        }
        
        return retval;
    }
    
    private static byte convertStringToByte(String input){
        byte retval=0;
        
        for(int i=0; i<input.length(); i++){
            retval <<= 1;
            if(input.charAt(i)=='1')
                retval++;
        }
        return retval;
    }
    
    private static String convertKBitStringToString(int kbit, String bitString){
        String retval ="";
        char dump;
        
        for(int i=0; i<bitString.length(); i+=kbit){
            dump= 0;
            for(int j=i; j<i+kbit; j++){
                dump <<= 1;
                dump += bitString.charAt(j)-48;
            }
            retval += dump;
        }
        return retval;
    }
    
    private static int min4(int y0, int y1, int y2, int y3){
        return min(y0, min(y1, min(y2, y3)));
    }
    
    private static int max4(int y0, int y1, int y2, int y3){
        return max(y0, max(y1, max(y1, y3)));
    }
    
    public static double PSNR(boolean isGrayscale, String coverImage, String stegoImage){
        //psnr = 20*log([256 | 16777216]/rms)
        //rms = sqrt(1/MxNx[1 | 3] x sum(pixelA - pixelB))
        
        double psnr=0;
        double rms=0;
        int height=0, width=0;
        double sum = 0;
        BufferedImage cover, stego;
        ColorStore [][] coverMap, stegoMap;
        File coverFile, stegoFile;
        
        if(isGrayscale)
            mode = 1;
        coverFile = new File(coverImage);
        stegoFile = new File(stegoImage);
        try {
            cover = BMPDecoder.read(coverFile);
            stego = BMPDecoder.read(stegoFile);
            
            height = cover.getHeight();
            width = cover.getWidth();
            
            coverMap = new ColorStore[height][width];
            stegoMap = new ColorStore[height][width];
            
            for(int i=0; i<height; i++){
                for(int j=0; j<width; j++){
                    coverMap[i][j] = new ColorStore(cover.getRGB(i, j));
                }
            }
            for(int i=0; i<height; i++){
                for(int j=0; j<width; j++){
                    stegoMap[i][j] = new ColorStore(stego.getRGB(i, j));
                }
            }
            
            for(int i=0; i<height; i++){
                for(int j=0; j<width; j++){
                    sum += Math.pow((stegoMap[i][j].getColor() - coverMap[i][j].getColor()), 2);
                }
            }
            
            rms = Math.sqrt(sum/(height*width));
            psnr = 20 * Math.log10(Math.pow(2, 8*mode)/rms);
            cover.flush();
            stego.flush();
        } catch (IOException ex) {
            Logger.getLogger(XinLiao.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return psnr;
    }
    
    public static String encryptExtended(String plainText, String key){
        String retval="";
        int keyIdx = 0;
        
        
        for(int i=0; i<plainText.length(); i++){
                retval += (char)((plainText.charAt(i)+key.charAt(keyIdx%key.length()))%256);
                keyIdx++;
        }
        return retval;
    }
    
    public static String decryptExtended(String cipherText, String key){
        String retval="";
        int keyIdx=0;
        
        for(int i=0; i<cipherText.length(); i++){
                int relChar = cipherText.charAt(i);
                int relKey = key.charAt(keyIdx%key.length());
                int dif = relChar - relKey;
                
                if(dif < 0) dif+= 256;
                retval += (char)(dif%256);
                keyIdx++;
        }
        return retval;
    }
    
    private static int intPow(int basic, int pow){
        int retval=1;
        
        for(int i=0; i<pow; i++)
            retval *= basic;
        return retval;
    }
    
    private static boolean isModifiable(int y){
        return (y>intPow(2, k-1) && y<(255 - intPow(2, k-1)));
    }
    
    private static int commonLSB(int y, int k, int bits){
        int nully = 0xFF << k;
        int y1 = y&nully;
        return y + bits;
    }
    
    private static int modifiedLSB(int y, int k, int bits){
        int nully = 0xFF << k;
        int y2 = nully&y;
        int min, max;
        int MSB = 1 << k;
        
        y2 += bits;
        min = y2 - MSB;
        max = y2 + MSB;
        if(Math.abs(min - y) > Math.abs(max - y)){
            if(Math.abs(max - y) < Math.abs(y2 - y))
                y2 = max;
        }else{
            if(Math.abs(min-y) < Math.abs(y2 - y))
                y2 = min;
        }
        return y2;
    }
    
    public static void main(String [] args){
        int messageLength = embedMessage("yusuf", "sari", "baboon.bmp", "stego_baboon.bmp");
        System.out.println("messageLength : "+messageLength);
        System.out.println("PSNR with mode ("+mode+"): "+PSNR(false, "baboon.bmp", "stego_baboon.bmp")+" dB");
        System.out.println("message : "+extractMessage("sari", messageLength, "stego_baboon.bmp"));
    }
}
