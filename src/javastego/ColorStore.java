package javastego;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Riady
 */
public class ColorStore {
    private int color;
    
    public ColorStore(){
        color = 0;
    }
    
    public ColorStore(int temp){
        color=temp;
    }
    
    public int getColor(){
        return color;
    }
    
    public void setColor(int temp){
        color = temp;
    }
    
    public void setLSBRed(boolean col){
        if(col){
            int a = 1 << 16;
            color = color | a;
        }
        else{
            int a=0xFFFEFFFF;
            color = color & a;
        }
    }
    
    public void setLSBGreen(boolean col){
        if(col){
            int a = 1 << 8;
            color = color | a;
        }
        else{
            int a=0xFFFFFEFF;
            color = color & a;
        }
    }
    
    public void setLSBBlue(boolean col){
        if(col){
            int a = 1;
            color = color | a;
        }
        else{
            int a=0xFFFFFFFE;
            color = color & a;
        }
    }
    
    public boolean getLSBRed(){
        int a = 1 << 16;
        int col = color & a;
        return(col!=0);
    }
    
    public boolean getLSBGreen(){
        int a = 1 << 8;
        int col = color & a;
        return(col!=0);
    }
    
    public boolean getLSBBlue(){
        int a = 1;
        int col = color & a;
        return(col!=0);
    }
    
}
