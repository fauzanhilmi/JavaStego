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
import java.util.Random;
import java.util.Scanner;
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
    private static Scanner input = new Scanner(System.in);
    
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
        int height, width;
        String cipher, bitString;
        
        coverFile = new File(coverImage);
        stegoFile = new File(stegoImage);
        
        try {
            buf = BMPDecoder.read(coverFile);
            height = buf.getHeight();
            width = buf.getWidth();
            
            if(buf.getType() == BufferedImage.TYPE_BYTE_GRAY 
                    || buf.getType() == BufferedImage.TYPE_USHORT_GRAY 
                    || buf.getType()==BufferedImage.TYPE_BYTE_INDEXED){
                buf.flush();
                return embedMessageGrayScale(message, key, coverImage, stegoImage);
            }
            if((height*width)<(message.length()*8/kLow + 1))
                return -1;
            
            //menyimpan informasi panjang pesan di akhir gambar
            String intBitString = Integer.toBinaryString(message.length());
            if(intBitString.length() < 32){
                int sisa = 32 - intBitString.length();
                for(int i=0; i< sisa; i++)
                    intBitString = "0"+intBitString;
            }
            for(int j=width-16; j<width; j++){
                int i=height-1;
                int bitStore = convertStringToByte(intBitString.substring(0, 2));
                if(intBitString.length()>3)
                    intBitString = intBitString.substring(2);
                bitStore = (bitStore<<16) + (bitStore<<8) + bitStore;
                buf.setRGB(i, j, (buf.getRGB(i, j)&0xFFFCFCFC)+bitStore);
            }
            cipher = encryptExtended(message, key);
            bitString = convertStringToBitString(cipher);
            for(int i=0; i<height; i+=2){
                for(int j=0; j<width; j+=2){
                    if(i+1 != height && j+1 != width && bitString.length()!=0){
                        //j=width-16 sampai j=width digunakan untuk menyimpan informasi panjang pesang
                        if(i==(height-1) && j>= (width-16))
                            break;
                        int [] y = new int[4];
                        int [] yt = new int[4];
                        int []bitStore = new int[4];
                        int nullifier;
                        
                        y[0] = buf.getRGB(i, j);
                        y[1] = buf.getRGB(i, j+1);
                        y[2] = buf.getRGB(i+1, j);
                        y[3] = buf.getRGB(i+1, j+1);
                        D = calculateD(y[0]&0xFFFFFF, y[1]&0xFFFFFF, y[2]&0xFFFFFF, y[3]&0xFFFFFF);
                        
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
                                for(int a=0; a<sisa; a++){
                                    bitString+="0";
                                }
                            }
                            
                        
                        //menentukan nullifier
                        int nullyTemp = (0xFF<<k)&0xFF;
                        nullifier = ((((nullyTemp<<8)+nullyTemp)<<8)+nullyTemp)+0xFF000000;
                            
                        for(int yIdx = 0; yIdx<4; yIdx++){
                            bitStore[0] = convertStringToByte(bitString.substring(0, k));
                            bitStore[1] = convertStringToByte(bitString.substring(k, 2*k));
                            if(bitString.length()>k)
                                bitStore[2] = convertStringToByte(bitString.substring(2*k, 3*k));
                            else
                                bitStore[2] = convertStringToByte(bitString.substring(2*k));
                            
                            if(bitString.length()>0)
                                bitString = bitString.substring(3*k);
                            
                            bitStore[0]<<=16;
                            bitStore[1]<<=8;
                            bitStore[0] += (bitStore[1]+bitStore[2]);

                            //mengubah y menjadi y'
                            yt[yIdx] = (y[yIdx]&nullifier)+bitStore[0];
                            
                            //mengubah y' menjadi y" dengan modified LSB substitution
                            int min, max, MSB, yTemp;
                            //untuk warna merah
                            if(isModifiable((y[yIdx]&0xFFFFFF)>>16)){
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
                            }
                            //untuk warna hijau
                            if(isModifiable((y[yIdx]&0xFFFF)>>8)){
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
                            }
                            //untuk warna biru
                            if(isModifiable((y[yIdx]&0xFF))){
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
                                int temp;
                                int sign = 1;
                                if(lMin == -1){
                                    lMin = Math.abs(lMin);
                                    sign = -1;
                                }
                                temp = lMin*maxBit;
                                temp = (temp<<16) + (temp<<8) + temp;
                                yx[yIdx] = yt[yIdx] + sign*temp;
                            }
                            D_1 = calculateD(yx[0]&0xFFFFFF, yx[1]&0xFFFFFF, yx[2]&0xFFFFFF, yx[3]&0xFFFFFF);
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
                        
                        //memasukkan nilai berdasarkan nilai l
                        int temp;
                        int sign = 1;
                        if(lMin == -1){
                            lMin = Math.abs(lMin);
                            sign = -1;
                        }
                        temp = lMin*maxBit;
                        temp = (temp<<16) + (temp<<8) + temp;
                        buf.setRGB(i, j, yt[0] + sign*temp);
                        buf.setRGB(i, j+1, yt[1] + sign*temp);
                        buf.setRGB(i+1, j, yt[2] + sign*temp);
                        buf.setRGB(i+1, j+1, yt[3] + sign*temp);
                        
                    }else{
                        break;
                    }
                }
            }
            //menuliskan buf ke dalam stegoFile
            BMPEncoder.write(buf, stegoFile);
            buf.flush();
        } catch (IOException ex) {
            Logger.getLogger(XinLiao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message.length();
    }
    
    private static int embedMessageGrayScale(String message, String key, String coverImage, String stegoImage){
         int messageLength=0;
        float D, D_1;
        File coverFile, stegoFile;
        BufferedImage buf;
        int height, width;
        String cipher, bitString;
        
        coverFile = new File(coverImage);
        stegoFile = new File(stegoImage);
        
        try {
            buf = BMPDecoder.read(coverFile);
            height = buf.getHeight();
            width = buf.getWidth();
            if((height*width)<(message.length()*8/kLow + 1))
                return -1;
            //menyimpan informasi panjang pesan di akhir gambar
            String intBitString = Integer.toBinaryString(message.length());
            if(intBitString.length() < 32){
                int sisa = 32 - intBitString.length();
                for(int i=0; i< sisa; i++)
                    intBitString = "0"+intBitString;
            }
            for(int j=width-16; j<width; j++){
                int i=height-1;
                int bitStore = convertStringToByte(intBitString.substring(0, 2));
                if(intBitString.length()>3)
                    intBitString = intBitString.substring(2);
                bitStore = (bitStore<<16) + (bitStore<<8) + bitStore;
                buf.setRGB(i, j, (buf.getRGB(i, j)&0xFFFCFCFC)+bitStore);
            }
            cipher = encryptExtended(message, key);
            bitString = convertStringToBitString(cipher);
            for(int i=0; i<height; i+=2){
                for(int j=0; j<width; j+=2){
                    if(i+1 != height && j+1 != width && bitString.length()!=0){
                        //j=width-16 sampai j=width digunakan untuk menyimpan panjang pesan
                        if(i==(height-1) && j>= (width-16))
                            break;
                        int [] y = new int[4];
                        int [] yt = new int[4];
                        int []bitStore = new int[4];
                        int nullifier;
                        
                        y[0] = buf.getRGB(i, j)&0xFF;
                        y[1] = buf.getRGB(i, j+1)&0xFF;
                        y[2] = buf.getRGB(i+1, j)&0xFF;
                        y[3] = buf.getRGB(i+1, j+1)&0xFF;
                        D = calculateD(y[0], y[1], y[2], y[3]);
                        
                        //menentukan k
                        if(D>=treshold)
                            k = kLow;
                        else
                            k = kHigh;
                        
                        //tidak menghitung error block dengan asumsi block selalu benar
                        //hal ini dilakukan agar treshold, kLow, dan kHigh bernilai tetap
                        
                        //menggenapkan bitString
                            if((bitString.length()%(4*k)) != 0){
                                int sisa = 4*k - (bitString.length()%(4*k));
                                for(int a=0; a<sisa; a++){
                                    bitString+="0";
                                }
                            }
                            
                        
                        //menentukan nullifier
                        nullifier = (0xFF<<k)&0xFF;
                            
                        for(int yIdx = 0; yIdx<4; yIdx++){
                            bitStore[0] = convertStringToByte(bitString.substring(0, k));
                            
                            if(bitString.length()>0)
                                bitString = bitString.substring(k);

                            //mengubah y menjadi y'
                            yt[yIdx] = (y[yIdx]&nullifier)+bitStore[0];
                            
                            //mengubah y' menjadi y" dengan modified LSB substitution
                            int min, max, MSB, yTemp;
                            //untuk warna merah
                            if(isModifiable(y[yIdx])){
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
                                int temp;
                                int sign = 1;
                                if(lMin == -1){
                                    lMin = Math.abs(lMin);
                                    sign = -1;
                                }
                                temp = lMin*maxBit;
                                temp = (temp<<16) + (temp<<8) + temp;
                                yx[yIdx] = yt[yIdx] + l*maxBit;
                            }
                            D_1 = calculateD(yx[0]&0xFF, yx[1]&0xFF, yx[2]&0xFF, yx[3]&0xFF);
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

                        //memasukkan nilai berdasarkan nilai l
                        int temp;
                        temp = lMin*maxBit;
                        
                        yt[0] = yt[0] + temp;
                        yt[1] = yt[1] + temp;
                        yt[2] = yt[2] + temp;
                        yt[3] = yt[3] + temp;
                        
                        yt[0] = 0xFF000000 + (yt[0]<<16) + (yt[0]<<8) + yt[0];
                        yt[1] = 0xFF000000 + (yt[1]<<16) + (yt[1]<<8) + yt[1];
                        yt[2] = 0xFF000000 + (yt[2]<<16) + (yt[2]<<8) + yt[2];
                        yt[3] = 0xFF000000 + (yt[3]<<16) + (yt[3]<<8) + yt[3];
                        
                        buf.setRGB(i, j, yt[0]);
                        buf.setRGB(i, j+1, yt[1]);
                        buf.setRGB(i+1, j, yt[2]);
                        buf.setRGB(i+1, j+1, yt[3]);
                        
                        
                    }else{
                        break;
                    }
                }
            }
            //menuliskan buf ke dalam stegoFile
            BMPEncoder.write(buf, stegoFile);
            buf.flush();
        } catch (IOException ex) {
            Logger.getLogger(XinLiao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message.length();
    }
    
    public static String extractMessage(String key, String stegoImage){
        String plainText="";
        int messageLength=0;
        float D, D_1;
        File stegoFile;
        BufferedImage buf;
        int height, width;
        String bitString="";
        String [] stream;
        
        stegoFile = new File(stegoImage);
        try {
            buf = BMPDecoder.read(stegoFile);
            height = buf.getHeight();
            width = buf.getWidth();
            //mengambil informasi panjang pesan pada akhir gambar
            String intBitString = "";
            for(int j=width-16; j<width; j++){
                int i = height-1;
                System.out.println("p"+i+j+": "+Integer.toHexString(buf.getRGB(i, j)));
                String inttemp = Integer.toBinaryString(buf.getRGB(i, j)&0x3);
                if(inttemp.length()<2){
                    int sisa = 2 - inttemp.length();
                    for(int si = 0; si<sisa; si++)
                        inttemp = "0" + inttemp;
                }
                System.out.println("inttemp : "+inttemp);
                intBitString += inttemp;
            }
            System.out.println("bs : "+intBitString);
            int [] intStore = new int[4];
            intStore[0] = ((int)convertStringToByte(intBitString.substring(0, 8)))<<24;
            intStore[1] = ((int)convertStringToByte(intBitString.substring(8, 16)))<<16;
            intStore[2] = ((int)convertStringToByte(intBitString.substring(16, 24)))<<8;
            intStore[3] = ((int)convertStringToByte(intBitString.substring(24)));
            messageLength = intStore[0] + intStore[1] + intStore[2] + intStore[3];
            System.out.println("message length : "+messageLength);
            stream = new String[9];
            if(buf.getType() == BufferedImage.TYPE_BYTE_GRAY 
                    || buf.getType() == BufferedImage.TYPE_USHORT_GRAY 
                    || buf.getType()==BufferedImage.TYPE_BYTE_INDEXED){
                buf.flush();
                return extractMessageGrayScale(key, messageLength, stegoImage);
            }
            messageLength *= 8;
            for(int i=0;i<height;i+=2){
                for(int j=0; j<width; j+=2){
                    if(i+1 != height && j+1 != width && messageLength>0){
                        //j=width-16 sampai j=width digunakan untuk menyimpan panjang pesan
                        if(i==(height-1) && j>= (width-16))
                            break;
                        int [] y = new int[4];
                        int [] yt = new int[4];
                        int []bitStore = new int[4];
                        int getter=0;
                        
                        
                        y[0] = buf.getRGB(i, j);
                        y[1] = buf.getRGB(i, j+1);
                        y[2] = buf.getRGB(i+1, j);
                        y[3] = buf.getRGB(i+1, j+1);
                        D = calculateD(y[0]&0xFFFFFF, y[1]&0xFFFFFF, y[2]&0xFFFFFF, y[3]&0xFFFFFF);
                        
                        //menentukan k
                        if(D>=treshold)
                            k = kLow;
                        else
                            k = kHigh;
                        
                        //menentukan getter
                        int getterTemp= 0xFF>>(8-k);
                        getter = ((((getterTemp<<8)+getterTemp)<<8)+getterTemp)+0xFF000000;
                        
                        for(int yi = 0; yi<4; yi++){
                            int R, G, B, tempHasil;
                            String temp="";
                            
                            tempHasil = y[yi]&getter;
                            R = (tempHasil&0xFF0000)>>16;
                            G = (tempHasil&0xFF00)>>8;
                            B = (tempHasil&0xFF);
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
                        messageLength -= (12*k);
                    }else{
                        break;
                    }
                }
            }
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
    
    private static String extractMessageGrayScale(String key, int messageLength, String stegoImage){
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
                    if(i+1 != height && j+1 != width && messageLength>0){
                        //j=width-16 sampai j=width digunakan untuk menyimpan panjang pesan
                        if(i==(height-1) && j>= (width-16))
                            break;
                        int [] y = new int[4];
                        int [] yt = new int[4];
                        int []bitStore = new int[4];
                        int getter=0;
                        
                        
                        y[0] = buf.getRGB(i, j);
                        y[1] = buf.getRGB(i, j+1);
                        y[2] = buf.getRGB(i+1, j);
                        y[3] = buf.getRGB(i+1, j+1);
                        D = calculateD(y[0]&0xFF, y[1]&0xFF, y[2]&0xFF, y[3]&0xFF);
                        
                        //menentukan k
                        if(D>=treshold)
                            k = kLow;
                        else
                            k = kHigh;
                        
                        //menentukan getter
                        getter= 0xFF>>(8-k);
                        
                        for(int yi = 0; yi<4; yi++){
                            int tempHasil;
                            String temp;
                            
                            tempHasil = y[yi]&getter&0xFF;
                            temp = Integer.toBinaryString(tempHasil);
                            if(temp.length() < k){
                                int sisa = k - temp.length();
                                for(int si=0; si<sisa; si++)
                                    temp = "0"+temp;
                            }
                            bitString += temp;
                        }
                        messageLength -= (4*k);
                        
                    }
                }
            }
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
        File coverFile, stegoFile;
        
        if(isGrayscale)
            mode = 1;
        else
            mode = 3;
        coverFile = new File(coverImage);
        stegoFile = new File(stegoImage);
        try {
            cover = BMPDecoder.read(coverFile);
            stego = BMPDecoder.read(stegoFile);
            
            height = cover.getHeight();
            width = cover.getWidth();
            
            for(int i=0; i<height; i++){
                for(int j=0; j<width; j++){
                    if(isGrayscale)
                        sum += Math.pow((stego.getRGB(i, j)&0xFF) - (cover.getRGB(i, j)&0xFF), 2);
                    else
                        sum += Math.pow((stego.getRGB(i, j)&0xFFFFFF) - (cover.getRGB(i, j)&0xffffff), 2);
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
    
    private static String generateNBString(int nb){
        String retval="";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        
        for(int i=0; i<nb; i++){
            byte temp;
            
            //A = 65
            //a = 97
            if(rand.nextBoolean())
                temp = 65;
            else
                temp = 97;
            temp += Math.abs(rand.nextInt()%26);
            sb.append((char)temp);
            
        }
        
        return sb.toString();
    }
    
    public static void main(String [] args){
        int messageLength;
        String message;
        //System.out.print("Masukkan banyaknya input : ");
        //messageLength = input.nextInt();
        //message = generateNBString(messageLength);
        message = "baracudda";
        messageLength = message.length();
        System.out.println("pesan : "+message);
        embedMessage(message, "key", "lena-gray.bmp", "stego_lena-gray.bmp");
        //embedMessage(message, "key", "baboon.bmp", "stego_baboon.bmp");
        //System.out.println("PSNR with mode ("+mode+"): "+PSNR(true, "lena-gray.bmp", "stego_lena-gray.bmp")+" dB");
        //System.out.println("PSNR with mode ("+mode+"): "+PSNR(false, "baboon.bmp", "stego_baboon.bmp")+" dB");
        System.out.println("message : "+extractMessage("key", "stego_lena-gray.bmp"));
        //System.out.println("message : "+extractMessage("key", "stego_baboon.bmp"));
    }
}
