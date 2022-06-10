package uit.spring.annotation.services;

public class PercentAgreement {
    public static float compute(Long[][] annArray){
        final int N = annArray.length;          
        final int k = annArray[0].length;
        
        int allAgree=0;
        for (int i=0; i<N; i++){
            int zerosCount = 0;
            for (int j=0; j<k; j++)
                if (annArray[i][j]==0)
                    zerosCount+=1;
            if (zerosCount==k-1) allAgree+=1;
        }
        return (float)allAgree/ (float)N;
    }
}
