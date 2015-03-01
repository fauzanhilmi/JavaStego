/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javastego;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
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
    
    boolean isColored; //true kalo RGB, false kalo grayscale
    int height;
    int width;
    
   public static void main(String[] args) {
        String in = "lena-gray-s.bmp";
        String ou = "tes.bmp";
        
        JavaStego js = new JavaStego(in);
        for(int i=0; i<js.height; i++) {
            for(int j=0; j<js.width; j++) {
                //System.out.println(Integer.toString(js.MatPixel[i][j],2));
                System.out.println(String.format("%8s", Integer.toBinaryString(js.MatPixel[i][j])).replace(' ', '0'));
            }
            System.out.println("");
        }
        //js.iseng();
        js.Export(ou);
    }
    
    public JavaStego() {
        //buf = null;
        MatPixel = new int[0][0];
        height = 0;
        width = 0;
        isColored = false;
    }
    
    public JavaStego(String filename) {
        Import(filename);
    }
    
    public int setMark(int n, int level) {
        int ans = n;
        if(level==0 || level==1) { 
            ans &= ~(1<<1);
            if(level==0) ans &= ~1;
            else ans |= 1;
        }
        else {
            ans |= (1<<1);
            if(level==3) ans &= ~1;
            else ans |= 1;
        }
        return ans;
    }
    
    public int getLevel(int d) {
        int level;
        if(d<=7) level = 0;
        else if(d>=8 && d<=15) level = 1;
        else if(d>=16 && d<=31) level = 2;
        else level = 3;
        return level;
    }
    
    public int Initialize() {
        int PixArR[], PixArB[], PixArG[];
        int capacity=0;
        for(int row=0; row<height; row+=3) {
            for(int col=0; col<width; col+=3) {
                if(row+3<height && col+3<width) {
                    PixArR = new int [9];
                    PixArG = new int [9];
                    PixArB = new int [9]; //cuma ini yg dipake kalo grayscale
                    
                    int xminB = MatPixel[row][col] & 0x000000FF;
                    int xminG = (MatPixel[row][col] >> 8) & 0x000000FF;
                    int xminR = (MatPixel[row][col] >> 16) & 0x000000FF;
                    int idx=0;
                    
                    for(int i=row; i<row+3; i++) {
                        for(int j=col; j<col+3; j++) {
                            PixArB[idx] = MatPixel[i][j] & 0x000000FF;
                            PixArG[idx] = (MatPixel[i][j] >> 8) & 0x000000FF;
                            PixArR[idx] = (MatPixel[i][j] >> 16) & 0x000000FF;      
                            
                            if(PixArB[idx]<xminB) xminB = PixArB[idx];
                            if(PixArG[idx]<xminG) xminG = PixArG[idx];
                            if(PixArR[idx]<xminR) xminR = PixArR[idx];
                            idx++;
                        }
                    }
                    
                    int dB = 0, dG=0, dR=0;
                    for(int i=0; i<9; i++) {
                        dB += (PixArB[i] - xminB);
                        dG += (PixArG[i] - xminG);
                        dR += (PixArR[i] - xminR);
                    }
                    dB /= 8;
                    dG /= 8;
                    dR /= 8;
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    /*int idx = 0;
                    int xmin = MatPixel[row][col];
                    for(int i=row; i<row+3; i++) {
                        for(int j=col; j<col+3; j++) {
                            PixAr[idx] = MatPixel[i][j];
                            idx++;
                            
                            if(MatPixel[i][j]<xmin)
                                xmin = MatPixel[i][j];
                        }
                    }
                    
                    int d = 0;
                    for(int i=0; i<9; i++) {
                        d += (PixAr[i]-xmin);
                    }
                    d /= 8;
                    
                    int level; //0-3
                    if(d<=7) level = 0;
                    else if(d>=8 && d<=15) level = 1;
                    else if(d>=16 && d<=31) level = 2;
                    else level = 3;*/
                    
                    //belum sisip bit tanda
                    //belum hitung kapasitas
                }
            }
        }
        return capacity;
    }
    
    
    public void Import(String filename) {
        BufferedImage buf = null;
        File f = new File(filename);
        try {
            buf = BMPDecoder.read(f);
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(buf.getType()==1) isColored = true;
        else if(buf.getType()==13) isColored = false;
        else System.out.println("Error : Filetype not recognized");
        //System.out.println(buf.getType());
        height = buf.getHeight();
        width = buf.getWidth();
        MatPixel = new int[height][width];
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                MatPixel[i][j] = buf.getRGB(i,j);               
                    //MatPixel[i][j] = buf.getRGB(i,j)&0xFF;               
            }
        }       
    }
    
    public void Export(String filename) {
        BufferedImage buf = null;
        buf = new BufferedImage(height,width,TYPE_INT_RGB);
        /*if(isColored)
             buf = new BufferedImage(height,width,TYPE_INT_RGB);
        else
        {
            buf = new BufferedImage(height,width,TYPE_INT_RGB);
            // buf = new BufferedImage(height,width,TYPE_BYTE_GRAY);
        }*/
        int px;
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
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
        
        /*Graphics g = buf.getGraphics();
        try {
            ImageIO.write(buf,"BMP", f);
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
}
