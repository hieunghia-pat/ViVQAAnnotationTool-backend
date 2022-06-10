package uit.spring.annotation.services;
import java.util.Arrays;

public class FleissKappa {
    public static final boolean DEBUG = false ;

    private static int checkEachLineCount(Long[][] annArray)
    {
        int n = 0 ;
        boolean firstLine = true ;
        
        for(Long[] line : annArray)
        {
            int count = 0 ;
            for(Long cell : line)
                count += cell ;
            if(firstLine)
            {
                n = count ;
                firstLine = false ;
            }
            if(n != count)
                throw new IllegalArgumentException("Line count != "+n+" (n value).") ;
        }
        return n ;
    }
    
    public static float compute(Long[][] annArray){
        final int n = checkEachLineCount(annArray) ;  // PRE : every line count must be equal to n
        final int N = annArray.length ;
        final int k = annArray[0].length ;
        
        if(DEBUG) System.out.println(n+" raters.") ;
        if(DEBUG) System.out.println(N+" subjects.") ;
        if(DEBUG) System.out.println(k+" categories.") ;
        
        // Computing p[]
        float[] p = new float[k] ;
        for(int j=0 ; j<k ; j++)
        {
            p[j] = 0 ;
            for(int i=0 ; i<N ; i++)
                p[j] += annArray[i][j] ;
            p[j] /= N*n ;
        }
        if(DEBUG) System.out.println("p = "+Arrays.toString(p)) ;
        
        // Computing P[]    
        float[] P = new float[N] ;
        for(int i=0 ; i<N ; i++)
        {
            P[i] = 0 ;
            for(int j=0 ; j<k ; j++)
                P[i] += annArray[i][j] * annArray[i][j] ;
            P[i] = (P[i] - n) / (n * (n - 1)) ;
        }
        if(DEBUG) System.out.println("P = "+Arrays.toString(P)) ;
        
        // Computing Pbar
        float Pbar = 0 ;
        for(float Pi : P)
            Pbar += Pi ;
        Pbar /= N ;
        if(DEBUG) System.out.println("Pbar = "+Pbar) ;
        
        // Computing PbarE
        float PbarE = 0 ;
        for(float pj : p)
            PbarE += pj * pj ;
        if(DEBUG) System.out.println("PbarE = "+PbarE) ;
        
        final float kappa = (Pbar - PbarE)/(1 - PbarE) ;
        if(DEBUG) System.out.println("kappa = "+kappa) ;
        
        return kappa ;
    }
}
