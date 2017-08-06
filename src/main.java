import com.zjgsu.util.DSA.DistributedSpectrumAccess;
import com.zjgsu.util.DSA.SocialGroupUtility;
import com.zjgsu.util.SAPCG.SAPCG;
import org.apache.commons.lang3.ArrayUtils;


public class main {
    public static void main(String[] args){
        /*DistributedSpectrumAccess algorithm = new DistributedSpectrumAccess();
        algorithm.algorithm();

        SocialGroupUtility socialGroupUtility = new SocialGroupUtility(0);
        socialGroupUtility.plot();*/

        SAPCG sapcg = new SAPCG();
        sapcg.algorithm();

    }
}
