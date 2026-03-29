package W15;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;

public class StringSort {

    private static int charAt( String str, int position ) {
        if( str.length() <= position ) // if str does not have char at position
            return 0;
        else
            return (int)str.charAt(position)+1;
    }
    
    public static int[] sort( String[] array, int position, int lo, int hi, String[] aux ) {
        int[] count = new int[0x10000+2];

        for( int i = lo; i <= hi; i ++ )
            count[charAt(array[i], position)+1] ++;

        for( int i = 1; i < count.length; i ++ )
            count[i] += count[i-1];

        for( int i = 0; i < hi-lo+1; i ++ )
            aux[count[charAt(array[i+lo], position)]++] = array[i+lo];

        if (hi - lo + 1 >= 0) System.arraycopy(aux, 0, array, lo, hi - lo + 1);
        return count;
    }

    public static void lsdSort( String[] array, int len ) {
        String[] aux = new String[array.length];
        for( int i = len-1; i >= 0; i -- )
            sort(array, i, 0, array.length-1, aux);
    }

    private static void msdSort( String[] array, int position, int lo, int hi, String[] aux ) {
        int[] count = sort(array, position, lo, hi, aux);
        for( int i = 1; i < count.length-1; i ++ )
            if( count[i]-count[i-1] > 1 )
                msdSort(array, position+1, lo+count[i-1], lo+count[i]-1, aux);
    }

    public static void msdSort( String[] array ) {
        msdSort(array, 0, 0, array.length-1, new String[array.length]);
    }

    public static void main(String[] args) throws Exception {
         String[] array = { "hzllo", "hello", "world", "dsaa", "b", "helly", "worle", "dsaab", "daaab" };

        Scanner input = new Scanner(System.in);
        LinkedList<String> list = new LinkedList<>();
        Collections.addAll(list, array);

        input.close();

        String[] array2 = array.clone();
        String[] array3 = array.clone();

        Arrays.sort(array);
        lsdSort(array2, list.stream().map(String::length).max(Integer::compare).get());
        msdSort(array3);

        System.out.println(Arrays.equals(array, array2));
        System.out.println(Arrays.equals(array, array3));

        for( String s : array3 )
           System.out.println(s);
    }
}
