package hhtask2;

/** 
 * Задача 2. Бесконечная последовательность
 * 
 * Возьмём бесконечную цифровую последовательность, образованную склеиванием 
 * последовательных положительных чисел: S = 123456789101112131415...
 * Определите первое вхождение заданной последовательности A в бесконечной 
 * последовательности S (нумерация начинается с 1).
 *
 * Пример входных данных:
 * 6789
 * 111
 *
 * Пример выходных данных:
 * 6
 * 12
 * 
 * --------------------------------------------------------------------------
 * 
 * Выполнение:
 * 
 * java -jar InfiniteSequence.jar [filename]
 * 
 * где filename - файл, содержащий входные данные (по умолчанию используется
 * имя input.txt) 
 * Файлы InfiniteSequence.jar и демонстрационный input.txt находятся 
 * в директории dist проекта
 */

import java.util.*;
import java.math.*;
import java.io.*;

/**
 * Реализует решение задачи о поиске в бесконечной цифровой последовательности, 
 * образованной подряд идущими натуральными числами 
 * 
 * @author Vladimir Trusov
 */

public class InfiniteSequence {
    
    /** Искомая числовая последовательность */
    private BigInteger patternBInt;    
    /** Строковое представление искомой числовой последовательности */
    private String patternStr;
    /** Длина искомой числовой последовательности */
    private int patternLen; 
    
    /** Список элементов бесконечной последовательности, на которых было
     *  найдено совпадение при последнем вызове метода {@link #indexOf} */
    private LinkedList<String> seqNumList = new LinkedList<String>();
    /** Смещение искомой последовательности относительно первого элемента 
     *  бесконечной  последовательности, на котором было найдено совпадение */
    private int patternOffset; 
    
    /**
     * Выполняет поиск первого вхождения числового шаблона в бесконечную 
     * последовательность
     * @param pattern строковое представление искомой числовой последовательности 
     * @return позицию вхождения
     * @throws NumberFormatException 
     */
    public BigInteger indexOf(String pattern) throws NumberFormatException {        
        if (pattern.startsWith("0")) {
            throw new NumberFormatException();
        }   
        patternBInt = new BigInteger(pattern);        
        patternStr = pattern;
        patternLen = pattern.length(); 
        
        BigInteger result = null;
        LinkedList<String> tempNumList = new LinkedList<String>();
        
        for (int exp = 1; exp <= patternLen; exp++) {            
            for (int offset = 0; offset < exp; offset++) { 
                tempNumList.clear();                           
                BigInteger curPos = checkNumSequence(exp, offset, tempNumList);                
                if ((curPos != null) && (result == null || 
                        result != null && result.compareTo(curPos) > 0)) {
                    result = curPos; 
                    seqNumList.clear();
                    seqNumList.addAll(tempNumList);       
                    patternOffset = offset;
                }                            
            }
            if (result != null) break;
        }
        return result;
    }
    
    /**
     * Возвращает форматированную строку, содержащую фрагмент бесконечной 
     * последовательности, на котором было найдено совпадение при последнем 
     * вызове метода {@link #indexOf}.
     * @return форматированную строку, содержащую место совпадения
     */
    public String getFoundFragment() {               
        String output = "[";        
        if (seqNumList.size() > 1) {
            int dgtsCount = 0;
            for (int i = 0; i < seqNumList.size(); i++) {                
                String item = seqNumList.get(i);
                if (i == 0) {
                    if (patternOffset > 0) {
                        dgtsCount = item.length() - patternOffset;
                        output += item.substring(0, dgtsCount) + "->" +
                            item.substring(dgtsCount, item.length());
                        dgtsCount = item.length() - dgtsCount;
                    } else {                    
                        output += "->" + item;
                        dgtsCount = item.length();
                    }                                   
                } else if (i == seqNumList.size() - 1) {
                    dgtsCount += item.length();
                    int suffixLen = dgtsCount - patternLen;
                    output += item.substring(0, item.length() - suffixLen) + "<-" +
                            item.substring(item.length() - suffixLen, item.length()) + "]";   
                } else {
                    output += item;
                    dgtsCount += item.length();
                }  
                if (i < seqNumList.size() - 1) {
                    output += ", ";
                }                
            }
        } else {
            output += "->" + seqNumList.get(0) + "<-]";
        } 
        return output;        
    }
    
    /**
     * Возвращает позицию элемента бесконечной числовой последовательности
     * (нумерация позиций начинается с 1)
     * @param item элемент последовательности (натуральное число)
     * @return позицию числа в последовательности 
     */
    private BigInteger getSequenceItemPos(BigInteger item) {
        BigInteger startPos = BigInteger.ZERO;   
        int itemLen = item.toString().length();
        for (int i = 1; i < itemLen; i++) {            
            startPos = startPos.add(
                    bint(i * 9).multiply(pow10BI(i - 1)));
        }        
        BigInteger offset = item.subtract(pow10BI(itemLen - 1)); 
        return startPos.add(offset.multiply(bint(itemLen))).add(bint(1));                
    }      
    
    /**
     * Дополняет незавершенное число в конце фрагмента совпадения цифрами, 
     * на основе анализа соответствующих цифр предыдущего числа в 
     * бесконечной последовательности
     * @param unCmplNumStr незавершенное число бесконечной последовательности 
     * в конце фрагмента совпадения
     * @param prevNumLastDgts соответствующие цифры младших разрядов 
     * предыдущего числа в последовательности
     * @return завершенное число, дополненное цифрами младших разрядов
     */
    private BigInteger completeLastNum(String unCmplNumStr, 
                                        String prevNumLastDgts) {        
        BigInteger pow10 = pow10BI(prevNumLastDgts.length());
        BigInteger unCmplNum = new BigInteger(unCmplNumStr);
        BigInteger lastDgts = new BigInteger(prevNumLastDgts);
        return lastDgts.compareTo(pow10.subtract(BigInteger.ONE)) == -1 ? // if lastDgts < 999...
                unCmplNum.multiply(pow10).add(lastDgts.add(BigInteger.ONE)) :
                unCmplNum.multiply(pow10);
    }
    
    /**
     * Выполняет проверку на совпадение для искомой последовательности среди 
     * элементов бесконечной последовательности <em>заданного порядка</em> 
     * (т.е. последовательных натуральных чисел определеного порядка) 
     * @param numExp начальный порядок элементов бесконечной последовательности
     * @param offset смещение искомой последовательности относительно элементов 
     * бесконечной последовательности
     * @param numStrList список для сохранения элементов бесконечной 
     * последовательности, на которых найдено совпадение
     * @return возвращает позицию в бесконечной последовательности, на которой
     * найдено совпадение
     */
    private BigInteger checkNumSequence(int numExp, int offset, 
                                        LinkedList<String> numStrList) {
        String seqOf9 = repeatStr("9", numExp);       
        String curNumStr = null;
        BigInteger curNum = null;
        BigInteger prevNum = null;
        BigInteger firstNum = null;        
        
        int i = offset;
        while(i < patternLen) {    
            prevNum = curNum;
            if (i + numExp <= patternLen) { 
                curNumStr = patternStr.substring(i, i + numExp);
                if (curNumStr.startsWith("0")) {
                    return null;
                }
                curNum = new BigInteger(curNumStr);
            } else {
                int unkDgtCount = i + numExp - patternLen;     
                String lastNumStr = 
                        patternStr.substring(i, i + numExp - unkDgtCount); 
                if (lastNumStr.startsWith("0")) {
                    return null;
                }
                if (i > offset) {                    
                    curNum = completeLastNum(lastNumStr,
                        curNumStr.substring(curNumStr.length() - unkDgtCount, 
                                            curNumStr.length()));                
                } else {                    
                    curNum = completeLastNum(lastNumStr,
                        patternStr.substring(offset - unkDgtCount, offset));                     
                }                
                curNumStr = String.valueOf(curNum);           
            }
            
            numStrList.add(curNumStr);
            
            if (i > offset && curNum.subtract(prevNum).intValue() != 1) {
                return null;                
            } 
            
            if (i == offset) { // 1st iteration
                firstNum = curNum;   
                if (offset > 0) { 
                    String offsetDgts = patternStr.substring(0, offset);
                    String testNumStr = curNum.subtract(BigInteger.ONE).toString();
                    int lastDgtsStart = testNumStr.length() - offsetDgts.length();                    
                    if (! offsetDgts.equals(testNumStr.substring(
                            lastDgtsStart < 0 ? 0 : lastDgtsStart, 
                            testNumStr.length()))) {
                        return null;
                    }
                    numStrList.addFirst(testNumStr);
                }                
            }            
            
            i += numExp;
            if (curNumStr.equals(seqOf9)) {                             
                numExp++;                
                seqOf9 += "9";
            }
        }
        
        return getSequenceItemPos(firstNum).subtract(bint(offset));
    }
    
    /**
     * Вычисляет степень 10  
     * @param pow показатель степени
     * @return возвращает степень 10
     */
    private static BigInteger pow10BI(int pow) {
        BigInteger biPow = BigInteger.TEN;        
        return biPow.pow(pow);
    }
    
    /**
     * Преобразует целое число типа long в {@link BigInteger}
     * <p>Замечание: метод введен для сокращения записи</p>
     * @param value целое число типа long
     * @return целое число типа {@link BigInteger}
     */
    private static BigInteger bint(long value) {
        return BigInteger.valueOf(value);
    }
    
    /**
     * Выполняет конкатенацию (повторением) строки заданное число раз
     * @param s строка, для которой выполнятся конкатенация 
     * @param n число операций конкатенации, применяемых к строке
     * @return результат конкатенации
     */
    private static String repeatStr(String s, int n) {
        String result = s;
        for (int i = 1; i < n; i++) {
            result += s;
        }
        return result;
    }
    
    public static void main(String[] args) {        
        
        String inFileName = args.length > 0 ? args[0] : "input.txt";         
        File inFile = new File(inFileName);        
        
        try {
            BufferedReader bufReader = 
                    new BufferedReader(new FileReader(inFile));
            
            InfiniteSequence infSeq = new InfiniteSequence();                        
            
            String numStr;  
            while((numStr = bufReader.readLine()) != null) {                
                System.out.println("A: " + numStr);
                try {
                     System.out.println("Позиция A в S: " + infSeq.indexOf(numStr));
                     System.out.println("Фрагмент S: " + infSeq.getFoundFragment());
                } catch (NumberFormatException e) {
                    System.out.println("Недопустимый формат записи "
                            + "числовой последовательности: " + numStr);
                }
                System.out.println("--------------------------");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Отсутствует файл со входными данными");            
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла");
        }
    }    
}