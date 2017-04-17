import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by adamz on 17/04/2017.
 */
public class ass2
{
    private static Map<String,String> _flags = new HashMap<String,String>();
    private static Set _flagsWithValues = new HashSet<String>(Arrays.asList("-a","-c","-t","-k","-v","-o"));
    private static Map<String,String> _key=new HashMap<String,String>();
    private static String _iv;
    private static void runAlgorithem(String algo)throws IOException{
    //run algorithem sub_cbc_10
        if(algo.equals("sub_cbc_10")){
            getIV();
            if(_flags.get("-c").equals("encryption")){
                run_CBC10_EncryptionAction();
                getKey(true);
            }

            else if(_flags.get("-c").equals("decryption"))
                run_CBC10_DecryptionAction();
            else{
                System.out.println("Please enter valid value for action in subs_cbc_10");
            }

        }
    }
    //get the encryptor key
    private static void getKey(boolean TrueEncrypt_FalseDecrypt)throws IOException{
        String keyContent=readFile(_flags.get("-k"));
        Scanner s = new Scanner(keyContent).useDelimiter("\\s*|\\n||\\r");
        while(s.hasNext()){
            String key,value;
            if(TrueEncrypt_FalseDecrypt){
                 key=s.next();
                 value=s.next();
            }
            else{
                 value=s.next();
                 key=s.next();
            }
            _key.put(key,value);
        }
        s.close();
    }
    //uses the key in order to encrypt/decrypt
    private static String useKeyOn(String textToUseWithKey){
        String toReturn="";
        for(int j=0;j<textToUseWithKey.length();j++){
            if(_key.containsKey(textToUseWithKey.charAt(j))){
                toReturn.concat(_key.get(textToUseWithKey.charAt(j)));
            }
            else{
                toReturn.concat(textToUseWithKey.charAt(j)+"");
            }
        }
        return toReturn;
        
        
        
        
        
    }
    
    
    
    
    private static void getIV()throws IOException{
        _iv=readFile(_flags.get("-v"));
    }
    private static void run_CBC10_EncryptionAction() throws IOException {

        String content = readFile(_flags.get("-t"));

    }

    private static void ByteBlock(String s) throws UnsupportedEncodingException {

        s="ABCDQRSTAB";
        String iv = "0000000000";
        byte[] a = iv.getBytes("UTF-8");
        byte[] b= s.getBytes("UTF-8");
        byte[] ABxor= new byte[10];
        for (int i=0 ;i<a.length;i++ ) {
            ABxor[i] = (byte)(a[i] ^ b[i]);
        }
        String result= new String(ABxor,"UTF-8");
        }




    private static String readFile(String path)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get("."+path));
        return new String(encoded, StandardCharsets.UTF_8);
    }


    private static void run_CBC10_DecryptionAction(){
    
    
    
    }
    
    public static void main (String[] args)throws IOException{
        for (int n = 0; n < args.length; n++)
        {
            if (args[n].charAt(0) == '-')
            {
                String name = args[n];
                String value = null;
                if (_flagsWithValues.contains(args[n]) && n < args.length - 1)
                    value = args[++n];
                _flags.put(name, value);
            }
        }
        runAlgorithem(_flags.get("-a"));
        ByteBlock("a");
    }



}
