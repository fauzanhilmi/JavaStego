package vigenerecipher;

import java.util.ArrayList;

public class VigenereCipher {
    public String Text; //Text input yang akan di-cipher/decipher
    public String Key;  //Key input untuk melakukan cipher/decipher
    
    //Konstruktor dengan text dan key untuk cipher/decipher
    public VigenereCipher(String InputText, String InputKey) {
        Text = InputText;
        Key = InputKey;
    }
    
    //Mengembalikan string hasil cipher standard
    public String getStdCipherText(boolean IsAutoKey, int CipherTextStyle) {        
        ArrayList<Integer> ListofSpace = new ArrayList<>();
        for(int i=0; i<Text.length(); i++)
        {
            if(Text.charAt(i)==' ')
            {
                ListofSpace.add(i);
            }
        }

        String tempText = Text.replace(" ",""); //Hilangkan semua spasi pada text
        Text = tempText;
        String tempKey = Key.replace(" ",""); //Hilangkan semua spasi pada text
        Key = tempKey;
        
        Text = Text.toUpperCase();
        Key = Key.toUpperCase();
        
        String CipherText = "";
        this.KeyLengthMatching(IsAutoKey);
        for(int i=0; i<Text.length(); i++)
        {
            char c = (char) ('A'+((Text.charAt(i)+Key.charAt(i)-'A'-'A')%26));
            CipherText += c;
        }
        if(CipherTextStyle==1)  //Output apa adanya, tanpa spasi
        {
            //do nothing
        }
        else if(CipherTextStyle==2) //Output dipisahkan spasi sesuai plaintext
        {
            StringBuilder Sb = new StringBuilder(CipherText);
            for(int i=0; i<ListofSpace.size(); i++)
            {
                int offset = ListofSpace.get(i);
                Sb.insert(offset,' ');
            }
            CipherText = Sb.toString();
        }
        else if(CipherTextStyle==3 && CipherText.length()>5)    //Output dipisah per 5 huruf
        {
            StringBuilder Sb = new StringBuilder(CipherText);
            for(int i=5; i<Sb.length(); i+=6)
            {
                Sb.insert(i,' ');
            }
            CipherText = Sb.toString();
        }
        return CipherText;
    }
    
    //Mengembalikan string hasil decipher standar 
    public String getStdDecipherText(boolean IsAutoKey, int CipherTextStyle) {
        ArrayList<Integer> ListofSpace = new ArrayList<>();
        for(int i=0; i<Text.length(); i++)
        {
            if(Text.charAt(i)==' ')
            {
                ListofSpace.add(i);
            }
        }
        
        String tempText = Text.replace(" ",""); //Hilangkan semua spasi pada text
        Text = tempText;
        String tempKey = Key.replace(" ",""); //Hilangkan semua spasi pada key
        Key = tempKey;
        
        Text = Text.toUpperCase();
        Key = Key.toUpperCase();
        
        String PlainText = "";
        if(!IsAutoKey)
        {
            this.KeyLengthMatching(false);
            for(int i=0; i<Text.length(); i++)
            {
                int x = ((Text.charAt(i)-Key.charAt(i)-'A'-'A')%26);
                if(x<0) x+=26;
                char c = (char) ('A'+x);
                PlainText += c;
            }
        }
        else
        {
            int LengthDif = Text.length()-Key.length();
            int counter = 0;
            for(int i=0; i<Text.length(); i++)
            {
                int x = (Text.charAt(i)-Key.charAt(i))%26;
                if(x<0) x+=26;
                char c = (char) ('A'+x);
                PlainText += c;
                if(counter<LengthDif)
                {
                    Key += c;
                    counter++;
                }
            }
        }
        
        if(CipherTextStyle==1)  //Output apa adanya, tanpa spasi
        {
            //do nothing
        }
        else if(CipherTextStyle==2) //Output dipisahkan spasi sesuai plaintext
        {
            StringBuilder Sb = new StringBuilder(PlainText);
            for(int i=0; i<ListofSpace.size(); i++)
            {
                int offset = ListofSpace.get(i);
                Sb.insert(offset,' ');
            }
            PlainText = Sb.toString();
        }
        else if(CipherTextStyle==3 && PlainText.length()>5)    //Output dipisah per 5 huruf
        {
            StringBuilder Sb = new StringBuilder(PlainText);
            for(int i=5; i<Sb.length(); i+=6)
            {
                Sb.insert(i,' ');
            }
            PlainText = Sb.toString();
        }
        return PlainText;
    }
    
    //Mengembalikan string hasil cipher extended
    public String getExtCipherText(boolean IsAutoKey) {
        String CipherText = "";
        this.KeyLengthMatching(IsAutoKey);
        
        for(int i=0; i<Text.length(); i++)
        {
            char c = (char) (((Text.charAt(i)+Key.charAt(i))%256));
            CipherText += c;
        }
        return CipherText;
    }
    
    //Mengembalikan string hasil decipher extended
    public String getExtDecipherText(boolean isAutoKey) {
        String PlainText = "";
        if(!isAutoKey)
        {
            this.KeyLengthMatching(false);
            for(int i=0; i<Text.length(); i++)
            {
                int x = (Text.charAt(i)-Key.charAt(i))%256;
                if(x<0) x+=256;
                char c = (char) x;
                PlainText += c;
            }
        }
        else
        {
            int LengthDif = Text.length()-Key.length();
            int counter = 0;
            for(int i=0; i<Text.length(); i++)
            {
                int x = (Text.charAt(i)-Key.charAt(i))%255;
                if(x<0) x+=256;
                char c = (char) x;
                PlainText += c;
                if(counter<LengthDif)
                {
                    Key += c;
                    counter++;
                }
            }
        }
        return PlainText;
    }
    
    //Menyesuaikan key agar panjangnya sama seperti text sebelum dilakukan
    //cipher/decipher
    public void KeyLengthMatching(boolean IsAutoKey) {
        if(Key.length()<Text.length())
        {
            int LengthDif = Text.length()-Key.length();
            if(IsAutoKey)
            {
                int j=0;
                for(int i=0; i<LengthDif; i++)
                {
                    Key += Text.charAt(j);
                    j++;
                }
            }
            else
            {
                String tempKey = Key;
                int j=0;
                for(int i=0; i<LengthDif; i++)
                {
                    if(j>tempKey.length()-1)
                    {
                        j = 0;
                    }
                    Key += tempKey.charAt(j);
                    j++;
                }
            }
        }
        else if(Key.length()>Text.length())
        {
            Key = Key.substring(0,Text.length());
        }
        //else Key.length()==Text.length(), tidak ada yang perlu dilakukan
    }
}