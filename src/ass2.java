import java.io.*;
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
    private static String _outputPath;
    private static void runAlgorithm(String algo)throws IOException{
    //run algorithem sub_cbc_10
        if(algo.equals("sub_cbc_10")){
            _iv=readFile(_flags.get("-v"));
            _outputPath="."+_flags.get("-o");
            if(_flags.get("-c").equals("encryption")){
                getKey(true);
                run_CBC10_EncryptionAction();
            }

            else if(_flags.get("-c").equals("decryption")){
                getKey(false);
                run_CBC10_DecryptionAction();
            }
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
            if(_key.containsKey(textToUseWithKey.charAt(j)+"")){
                toReturn=toReturn.concat(_key.get(textToUseWithKey.charAt(j)+""));
            }
            else{
                toReturn= toReturn.concat(textToUseWithKey.charAt(j)+"");
            }
        }
        return toReturn;
    }
    private static void run_CBC10_EncryptionAction() throws IOException {

        String PlainText = readFile(_flags.get("-t"));
        String currentBlock ;
        String cipherTextBlock="" ;
        String PlainTextAfterXor;
        String CipheredText= "";
        int check= PlainText.length()%10;
        if(check>0){
            for (int i=0 ; i<(10-check);i++)
            {
                char zero = (char)0;
                PlainText=PlainText+zero;
            }
        }
        for (int i = 0 ; i<PlainText.length();i=i+10){
                currentBlock= PlainText.substring(i, i + 10);
                if(i==0){
                    PlainTextAfterXor =XOR_AB(currentBlock,_iv);
                }
                else
                    PlainTextAfterXor= XOR_AB(currentBlock,cipherTextBlock);
            cipherTextBlock= useKeyOn(PlainTextAfterXor);
            CipheredText=CipheredText.concat(cipherTextBlock);
        }

         writeOutput(CipheredText);
    }
    private static void writeOutput(String textToWrite)throws IOException{
        File file = new File(_outputPath);
        file.getParentFile().mkdirs(); // Will create parent directories if not exists
        file.createNewFile();
        FileOutputStream s = new FileOutputStream(file,true);
        PrintWriter out = new PrintWriter( _outputPath );
        out.write(textToWrite);
        out.close();
   
    }

    private static String XOR_AB(String A, String B) throws UnsupportedEncodingException {

        byte[] a = A.getBytes("UTF-8");
        byte[] b= B.getBytes("UTF-8");
        byte[] ABxor= new byte[10];
        for (int i=0 ;i<a.length;i++ ) {
            ABxor[i] = (byte)(a[i] ^ b[i]);
        }
       return new String(ABxor,"UTF-8");
        }




    private static String readFile(String path)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get("."+path));
        return new String(encoded, StandardCharsets.UTF_8);
    }


    private static void run_CBC10_DecryptionAction()throws IOException {
        String to_decipher = readFile(_flags.get("-t"));
        String currentBlock ;
        String decipheredTextBlock="" ;
        String Prev_undecipheredBlock="";
        String PlainTextAfterXor;
        String deCipheredText= "";
        int check= to_decipher.length()%10;
        if(check>0){
            for (int i=0 ; i<(10-check);i++)
            {
                char zero = (char)0;
                to_decipher=to_decipher+zero;
            }
        }
        for (int i = 0 ; i<to_decipher.length();i=i+10){
            currentBlock= to_decipher.substring(i, i + 10);
            decipheredTextBlock= useKeyOn(currentBlock);
            if(i==0){
                PlainTextAfterXor =XOR_AB(decipheredTextBlock,_iv);
            }
            else{
                PlainTextAfterXor= XOR_AB(Prev_undecipheredBlock,decipheredTextBlock);
            }
            Prev_undecipheredBlock=new String(currentBlock);
            deCipheredText=deCipheredText.concat(PlainTextAfterXor);
        }
        writeOutput(deCipheredText);
    
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
        runAlgorithm(_flags.get("-a"));

    }



}
