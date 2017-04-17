import java.io.IOException;
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
    private static void runAlgorithem(String algo)throws IOException{
    //run algorithem sub_cbc_10
        if(algo.equals("sub_cbc_10")){
            if(_flags.get("-c").equals("encryption"))
                run_CBC10_EncryptionAction();
            else if(_flags.get("-c").equals("decryption"))
                run_CBC10_DecryptionAction();
            else{
                System.out.println("Please enter valid value for action in subs_cbc_10");
            }

        }
    }
    private static void run_CBC10_EncryptionAction() throws IOException {

        String content = readFile(_flags.get("-t"));

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
    }
    

}
