/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package javastego;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JFrame;

/**
 *
 * @author Riady
 */
public class ViewImage extends JApplet {
    File f;
    Image img;
   MediaTracker tr;
   public void paint(Graphics g) {
      if (img!=null) {
            g.drawImage(img, 0, 0, this);
        }
   } 
    /**
     * Initialization method that will be called after the applet is loaded into
     * the browser.
     */
   
   public void setFile(File file){
       f=file;
   }
   
    public void init() {
        // TODO start asynchronous download of heavy resources
         try {
            img = ImageIO.read(f);
        } catch(Exception e) {
            // tell us if anything goes wrong!
            e.printStackTrace();
        }   
         BufferedImage a = (BufferedImage) img;
         setSize(a.getWidth(),a.getHeight());
    }
    
  /*  public static void main(String[] args) {
        // TODO code application logic here
        JFrame a = new JFrame();
        ViewImage n = new ViewImage();
        n.init();
        a.getContentPane().add(n);
        a.pack();
        a.setVisible(true);
    }*/
    // TODO overwrite start(), stop() and destroy() methods
}
