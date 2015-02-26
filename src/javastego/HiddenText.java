/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package javastego;

/**
 *
 * @author Riady
 */
public class HiddenText {
    private byte[] data;
    int bitCount = 0;
    int byteCount = 0;
    
    public HiddenText(){
    
    }
    public HiddenText(byte[] _data){
        data=_data;
    }
    
    public boolean getNextBit(){
        
        byte temp = data[byteCount];
        boolean tempbool = (temp & (1 << (7-bitCount))) != 0;
        bitCount++;
        if(bitCount > 7){
            byteCount++;
            bitCount = 0;
        }
        return tempbool;
        
    }
    
    public boolean isNext(){
        return (byteCount != data.length);
    }
    
    /*public static void main(String[] args) {
        byte[] data = new byte[2];
        data[0] = (byte) 0xaf;
        data[1] = 0x5;
        HiddenText ht = new HiddenText(data);
        while(ht.isNext()){
            if(ht.getNextBit()){
                System.out.print('1');
            }
            else{
                System.out.print('0');
            }
        }
        
    }*/
}
