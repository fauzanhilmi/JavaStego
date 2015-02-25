/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javastego;

import java.awt.image.BufferedImage;
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
    
    BufferedImage buf;
    
    public static void main(String[] args) {
        String in = "kuda.bmp";
        String ou = "kudy.bmp";
        JavaStego js = new JavaStego(in);
        js.Export(ou);
    }
    
    public JavaStego() {
        buf = null;
    }
    
    public JavaStego(String filename) {
        File f = new File(filename);
        try {
            buf = BMPDecoder.read(f);
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void Import(String filename) {
        File f = new File(filename);
        try {
            buf = BMPDecoder.read(f);
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void Export(String filename) {
        File f = new File(filename);
        try {
            BMPEncoder.write(buf, f);
        } catch (IOException ex) {
            Logger.getLogger(JavaStego.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
