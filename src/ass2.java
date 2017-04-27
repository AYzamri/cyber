import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by adamz on 17/04/2017.
 */
public class ass2
{
    private static Map<String,String> _flags = new HashMap<String,String>();
    private static Set _flagsWithValues = new HashSet<String>(Arrays.asList("-a","-c","-t","-k","-v","-o","-kp","-kc"));
    private static Set _dictionary=new HashSet<String>();
    private static Map<Character,Character> _key=new HashMap<Character,Character>();
    private static byte [] _iv;
    private static String _outputPath;
    private static int Block_Size;
    private static boolean cbc52Attack=false;
    private static int indexOfChar =0;
    private static Set<Integer> setOfIndexes = new HashSet<>();
    private static void runAlgorithm(String algo)throws IOException{
    //run algorithem sub_cbc_10
        if(algo.equals("sub_cbc_10") ||algo.equals("sub_cbc_52" )){  // set the block size
            if(algo.equals("sub_cbc_10")) {
                Block_Size = 10;
            }
            else
                Block_Size=8128;
            _iv=readFile_bytes(_flags.get("-v")); // set the IV
            _outputPath="."+_flags.get("-o"); // sey the Out put path
            if(_flags.get("-c").equals("encryption")){
                getKey(true);
                EncryptionAction();
            }

            else if(_flags.get("-c").equals("decryption")){
                getKey(false);
                String toWrite= DecryptionAction(readFile_bytes(_flags.get("-t")));
                writeOutput(toWrite);
            }
            else if(_flags.get("-c").equals("attack")){
                if(algo.equals("sub_cbc_10")) {
                    run_CBC10_AttackAction();
                } else
                    run_CBC52_AttackAction();
            }
            else{
                System.out.println("Please enter valid value for action in subs_cbc_10");
            }

        }
    }

    private static void run_CBC10_AttackAction()  throws IOException
    {
        int Sample_Size= 800;
        LoadDictionary();
        setKeys();
        //read first 500 in given file
        byte[] CipheredText = (readFile_bytes(_flags.get("-t")));
        if(CipheredText.length<Sample_Size){
            Sample_Size=CipheredText.length;
        }
        byte[] PartOfCipheredText=new byte[Sample_Size];
        System.arraycopy(CipheredText,0,PartOfCipheredText,0,Sample_Size);
        Pair<String, Integer> maxMatch=new Pair<String, Integer>("",0);
        HashSet<String> SetOfKeys = new HashSet<String>();
        permutation("abcdefgh",SetOfKeys);
        int counter=0;
        for (String currentKey:SetOfKeys) {
            setDecryptor(currentKey);
            String deCipherToCheck= DecryptionAction(PartOfCipheredText);
            int counterWordsInDict=0;
            int totalWords=0;
            String[] decipheredTextSplited = deCipherToCheck.split("[\\.,\\s!;?:&\"\\[\\]]+");
            int lengthOfSplited=decipheredTextSplited.length;
            while(totalWords <decipheredTextSplited.length )
            {
                String currentWord = decipheredTextSplited[totalWords];
                if(currentWord.matches(".*\\d.*")){ // if number
                    counterWordsInDict++;
                    totalWords++;
                    continue;
                }


                if(totalWords>=0.5*lengthOfSplited&& totalWords > 5*counterWordsInDict){
                    break;
                }
                if(_dictionary.contains(currentWord.toLowerCase())){
                    counterWordsInDict++;
                }
                if(counterWordsInDict>maxMatch.getValue()){
                    maxMatch=new Pair<>(currentKey,counterWordsInDict);
                }
                totalWords++;
        }
            counter++;
        }

        Map<Character,Character> zamrial = getTheKey(maxMatch.getKey());
        WriteKeyToFile(zamrial);

    }


    private static void run_CBC52_AttackAction()  throws IOException {

        byte[] knownPlainText = (readFile_bytes(_flags.get("-kp")));
        byte[] knownCiphertext = (readFile_bytes(_flags.get("-kc")));
        Map<Character, Character> PartialDecryptKey = new HashMap<>();
        byte[] partialyCiphered = XOR_AB(knownPlainText, _iv);
        for (int i = 0; i < partialyCiphered.length; i++) {
            char value = (char) partialyCiphered[i];
            char key = (char) knownCiphertext[i];
            if (key >= 65 && key <= 90 || key >= 97 && key <= 122) {
                PartialDecryptKey.put(key, value);
            }

        }
        _key = PartialDecryptKey;
        cbc52Attack = true;
        LoadDictionary();
        byte[] partialyDecryptedText_bytes = readFile_bytes(_flags.get("-t"));
        String partialyDecryptedText_string = DecryptionAction(partialyDecryptedText_bytes);
        Set<Character> endOfWordChars = new HashSet<Character>(Arrays.asList(';','[',',',']',')','(','.',':','\n','\r','?','-','/','!',' ','}','{','_'));
        String currentWord = "";
        Set<Character> currentExistingKeysInDecryptor = new HashSet<>(_key.keySet());
        Set<Character> currentExistingValuesInDecryptor = new HashSet<>(getValuesForKey());
        int counterOfUnkownCharsDecrypt = 0;
        int IndexCharToFindInWord=0;
        int countWordLength=0;
        char currentUnknownChar=' ';
        int IndexCharToFindInMainString = 0;
        for(int j = 0 ; j< 1;j++) {
            if (currentExistingKeysInDecryptor.size() == 52)
                break;
            for (int i = 0; i < partialyDecryptedText_string.length(); i++) {
                if (currentExistingKeysInDecryptor.size() == 52)
                    break;
                //check if contains a character that cannot be decrypted by the partialKey -count and save
                if (setOfIndexes.contains(i)) { // the legal range checked in useKey func
                    counterOfUnkownCharsDecrypt++;
                    currentUnknownChar = partialyDecryptedText_string.charAt(i);
                    IndexCharToFindInMainString = i;
                    IndexCharToFindInWord = countWordLength;
                }
                //reached end of word
                if (endOfWordChars.contains(partialyDecryptedText_string.charAt(i))) {
                    //if more than one missing and word is too short continue;
                    if (counterOfUnkownCharsDecrypt != 1 || currentWord.length() < 3 || currentWord.matches(".*\\d.*")) {
                        counterOfUnkownCharsDecrypt = 0;
                        currentWord = "";
                        IndexCharToFindInMainString = 0;
                        IndexCharToFindInWord = 0;
                        countWordLength = 0;
                        continue;
                    } else {
                        // get the char from prev block
                        byte CharFromPrevBlock;
                        if (IndexCharToFindInMainString < 8128) {
                            CharFromPrevBlock = _iv[IndexCharToFindInMainString];
                        } else {
                            CharFromPrevBlock = partialyDecryptedText_bytes[IndexCharToFindInMainString - 8128+1];///+1 ??
                        }
                        //start checking possible options for the char to find.
                        char[] optionalWord = currentWord.toLowerCase().toCharArray();
                        int countCharMatched = 0;
                        char charAfterXorLowerCase=' ';
                        boolean foundCharInUpper=false;
                        char charAfterXorUpper=' ';
                        for (char c = 'a'; c <= 'z'; c++) {
                            if(IndexCharToFindInWord>=optionalWord.length)
                                charAfterXorUpper=' ';
                            optionalWord[IndexCharToFindInWord] = c;
                            String optionalWordAsString = String.valueOf(optionalWord);
                            if (_dictionary.contains(optionalWordAsString)) {
                                byte ByteAfterXorUpperrCase;
                                 charAfterXorUpper = ' ';
                                 foundCharInUpper = false;
                                 byte cb=(byte)(c&0x00FF);
                                byte ByteAfterXorLowerCase = (byte) ((cb) ^ CharFromPrevBlock);
                                charAfterXorLowerCase = (char) ByteAfterXorLowerCase;
                                if(charAfterXorLowerCase=='}'&&currentUnknownChar=='Z'){
                                    int dan=0;
                                }
                                if (IndexCharToFindInWord == 0) {
                                    ByteAfterXorUpperrCase = (byte) ((cb - 32) ^ CharFromPrevBlock);
                                    charAfterXorUpper = (char) ByteAfterXorUpperrCase;
                                    if (!(currentExistingValuesInDecryptor.contains(charAfterXorUpper))&&((charAfterXorUpper >= 65 && charAfterXorUpper <= 90) || (charAfterXorUpper >= 97 && charAfterXorUpper <= 122))) {
                                        countCharMatched++;
                                        foundCharInUpper = true;
                                    }
                                    if(charAfterXorUpper=='}'&&currentUnknownChar=='Z'){
                                        int zamriel=0;
                                    }
                                }
                                if (currentExistingValuesInDecryptor.contains(charAfterXorLowerCase) && foundCharInUpper == false)
                                    continue;
                                else {
                                    if (!(currentExistingValuesInDecryptor.contains(charAfterXorLowerCase))&&((charAfterXorLowerCase >= 65 && charAfterXorLowerCase <= 90) || (charAfterXorLowerCase >= 97 && charAfterXorLowerCase <= 122)))
                                        countCharMatched++;
                                    if (countCharMatched > 1)
                                        break;
                                }

                            }
                        }
                        if (countCharMatched == 1) {
                            if((charAfterXorLowerCase=='}'||charAfterXorUpper=='}')&&currentUnknownChar=='X'){
                                int dan=0;
                            }
                            if (foundCharInUpper) {
                                _key.put(currentUnknownChar, charAfterXorUpper);
                                currentExistingKeysInDecryptor.add(currentUnknownChar);
                                currentExistingValuesInDecryptor.add(charAfterXorUpper);
                            } else {
                                _key.put(currentUnknownChar, charAfterXorLowerCase);
                                currentExistingKeysInDecryptor.add(currentUnknownChar);
                                currentExistingValuesInDecryptor.add(charAfterXorLowerCase);
                            }

                        }
                        countCharMatched=0;
                        counterOfUnkownCharsDecrypt = 0;
                        currentWord = "";
                        IndexCharToFindInMainString = 0;
                        IndexCharToFindInWord = 0;
                        countWordLength = 0;
                        continue;
                    }
                }
                currentWord += partialyDecryptedText_string.charAt(i);
                countWordLength++;
            }
        }
        getTheKey();
        WriteKeyToFile(_key);
    }




    private static Set<Character> getValuesForKey()
    {
       Object[] valuesArray=_key.values().toArray();
       Set<Character> values=new HashSet<Character>();
       for(int j=0;j<valuesArray.length;j++){
           values.add((char)valuesArray[j]);
       }
       return values;
       
    }
    
    private static void WriteKeyToFile (Map<Character,Character> keyToWrite) throws IOException {
        String sringush="";
        for (Character c:keyToWrite.keySet()) {
            sringush=sringush+ (c + " " + keyToWrite.get(c) + "\n");
        }
        writeOutput(sringush);
    }

    private static Map<Character,Character> getTheKey(String s){

        setDecryptor(s);
        Map<Character,Character> KeyMap = new HashMap<>();
        for (Character c:_key.keySet()) KeyMap.put(_key.get(c), c);
        return KeyMap;
    }

    private static void getTheKey(){


        Map<Character,Character> KeyMap = new HashMap<>();
        for (Character c:_key.keySet()) KeyMap.put(_key.get(c), c);
        _key=KeyMap;
    }


    private static void setKeys (){

        _key.put('a',' ');
        _key.put('b',' ');
        _key.put('c',' ');
        _key.put('d',' ');
        _key.put('e',' ');
        _key.put('f',' ');
        _key.put('g',' ');
        _key.put('h',' ');
}
    private static void setDecryptor(String currentKey)
    {
        _key.replace('a',currentKey.charAt(0));
        _key.replace('b',currentKey.charAt(1));
        _key.replace('c',currentKey.charAt(2));
        _key.replace('d',currentKey.charAt(3));
        _key.replace('e',currentKey.charAt(4));
        _key.replace('f',currentKey.charAt(5));
        _key.replace('g',currentKey.charAt(6));
        _key.replace('h',currentKey.charAt(7));
    }


    //get the encryptor key
    private static void getKey(boolean TrueEncrypt_FalseDecrypt)throws IOException{
        String keyContent=readFile_string(_flags.get("-k"));
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
            _key.put(key.charAt(0),value.charAt(0));
        }
        s.close();
    }
    //uses the key in order to encrypt/decrypt
    private static byte[] useKeyOn(byte[] textToUseWithKey, int Block_Size){
        byte[] toReturn=new byte[Block_Size];
        byte currentByte=0;
        for(int j=0;j<textToUseWithKey.length;j++){
            if(_key.containsKey((char)textToUseWithKey[j])){
                currentByte=(byte)(_key.get((char)textToUseWithKey[j])&0x00FF);
            }
            else{
                currentByte=textToUseWithKey[j];
                char currentChar = (char)currentByte;
                if(cbc52Attack && (currentChar >= 65 && currentChar <= 90) || (currentChar >= 97 && currentChar <= 122)){
                    setOfIndexes.add(indexOfChar);
                }
            }
            toReturn[j]=currentByte;
            indexOfChar++;
        }
        return toReturn;
    }
    private static void EncryptionAction() throws IOException {

        byte [] PlainText = readFile_bytes(_flags.get("-t"));
        byte [] currentBlock=new byte[Block_Size] ;
        byte [] cipherTextBlock=new byte[Block_Size] ;
        byte [] PlainTextAfterXor;
        byte [] FullPlainText;
        String CipheredText= "";
        int check= PlainText.length%Block_Size;
        if(check>0){
           FullPlainText=new byte[PlainText.length+(Block_Size-check)];
            for (int i=0 ; i<FullPlainText.length;i++)
            {
                if(i<PlainText.length){
                    FullPlainText[i]=PlainText[i];
                }
                else
                    FullPlainText[i]=(byte)0&0x00FF;
            }
        }
        else
            FullPlainText=PlainText;
        for (int i = 0 ; i<FullPlainText.length;i=i+Block_Size){
                 System.arraycopy(FullPlainText,i,currentBlock,0,Block_Size);
                if(i==0){
                    PlainTextAfterXor =XOR_AB(currentBlock,_iv);
                }
                else
                    PlainTextAfterXor= XOR_AB(currentBlock,cipherTextBlock);
            cipherTextBlock= useKeyOn(PlainTextAfterXor,Block_Size);
            String currentBlockString=new String(cipherTextBlock,"ASCII");
            CipheredText=CipheredText.concat(currentBlockString);
        }
         writeOutput(CipheredText);
    }

    private static void writeOutput(String textToWrite)throws IOException{
        File file = new File(_outputPath);
        file.getParentFile().mkdirs(); // Will create parent directories if not exists
        file.createNewFile();
        //FileOutputStream s = new FileOutputStream(file,true);
        PrintWriter out = new PrintWriter( _outputPath );
        out.write(textToWrite);
        out.close();

    }

    private static byte[] XOR_AB(byte[] A, byte[] B) throws UnsupportedEncodingException {


        int min = Math.min(A.length,B.length);
        byte[] ABxor= new byte[min];
        for (int i=0 ;i<min;i++ ) {
            if(cbc52Attack==false)
                ABxor[i] = (byte)(A[i] ^ B[i]);
            else{
                char checkC=(char)A[i];
                if(!_key.containsKey(checkC)&&checkC>= 65&& checkC<=90||checkC>= 97&& checkC<=122)
                    ABxor[i]=A[i];
                else{
                    ABxor[i] = (byte)(A[i] ^ B[i]);
                }
            }
        }
       return  ABxor;
        }




    private static byte[] readFile_bytes(String path)
            throws IOException
    {
        Path filePath = Paths.get("."+path);
        byte[] encoded = Files.readAllBytes(filePath);
        return  encoded;
    }

    private static String readFile_string(String path)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get("."+path));
        return new String(encoded, "ASCII");

    }




    private static void LoadDictionary()throws IOException{
        String DictContent=readFile_string("\\Dict\\dictionary.txt");
        Scanner scan = new Scanner(DictContent);
        while(scan.hasNext()){
            _dictionary.add(scan.next());
        }
        scan.close();
    }


    private static String DecryptionAction(byte [] to_decipher)throws IOException {
        byte [] currentBlock=new byte[Block_Size] ;
        byte [] decipheredTextBlock ;
        byte [] Prev_undecipheredBlock=new byte[Block_Size];
        byte [] PlainTextAfterXor;
        byte [] FullCipheredText;
        String deCipheredText= "";
        int check= to_decipher.length%Block_Size;
        if(check>0){
            FullCipheredText=new byte[to_decipher.length+(Block_Size-check)];
            for (int i=0 ; i<FullCipheredText.length;i++)
            {
                if(i<to_decipher.length){
                    FullCipheredText[i]=to_decipher[i];
                }
                else
                    FullCipheredText[i]=(byte)0&0x00FF;
            }
        }
        else
            FullCipheredText=to_decipher;

        for (int i = 0 ; i<FullCipheredText.length;i=i+Block_Size){
            System.arraycopy(FullCipheredText,i,currentBlock,0,Block_Size);
            decipheredTextBlock= useKeyOn(currentBlock,Block_Size);
             if(i==0){
                PlainTextAfterXor =XOR_AB(decipheredTextBlock,_iv);
            }
            else{
                  PlainTextAfterXor= XOR_AB(Prev_undecipheredBlock,decipheredTextBlock);
                }

            System.arraycopy(currentBlock,0,Prev_undecipheredBlock,0,Block_Size);
            deCipheredText=deCipheredText.concat(new String(PlainTextAfterXor,"ASCII"));
        }
        return deCipheredText;
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

/**********************/
public static void permutation(String str, HashSet<String> s) {
    permutation("", str, s);
}

    private static void permutation(String prefix, String str, HashSet<String> s) {
        int n = str.length();
        if (n == 0 && !s.contains(prefix))
            s.add(prefix);
        else {
            for (int i = 0; i < n; i++)
                permutation(prefix + str.charAt(i), str.substring(0, i) + str.substring(i+1, n),s);
        }
    }
}
