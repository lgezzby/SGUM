package com.zjgsu.util.SAPCG;


import com.sun.deploy.util.ArrayUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * 《Personalized Location Privacy in Mobile Networks:A Social Group Utility Approach》
 * Compute the Pareto-optimal SNE for SA-PCG
 */
public class SAPCG {

    /**
     * user number
     */
    private final static int users = 8;

    /**
     * physicalSet  物理关系集合
     * physicalCost 物理代价集合
     * socialSet    社会关系集合
     * socialEdge   社会关系权重
     */
    private final static int[][] physicalSet = {
            {0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 1, 1, 0, 0},
            {0, 1, 0, 0, 0, 0, 1, 0},
            {0, 0, 1, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 1, 0, 0},
            {0, 1, 0, 0, 0, 0, 1, 0},
            {0, 0, 1, 0, 0, 1, 0, 1},
            {0, 0, 0, 0, 0, 0, 0, 0}
    };
    private final static double[] physicalCost = {1.2, 0.6, 2.5, 1.2, 1.5, 0.8, 2.2, 0.8};
    private final static int[][] socialSet = {
            {0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0},
            {0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 1, 0, 1},
            {0, 0, 0, 0, 0, 0, 0, 0}
    };
    private final static double[][] socialEdge = {
            {0, 0.5, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0.3, 0, 0, 0},
            {0, 0.8, 0, 0, 0, 0, 0, 0},
            {0, 0, 0.6, 0, 0, 0, 0, 0.8},
            {0.2, 0, 0, 0, 0, 0.6, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0.3, 0, 0, 0.5, 0, 0.2},
            {0, 0, 0, 0, 0, 0, 0, 0}
    };
    /**
     *
     */
    private int[] chooseSet = new int[users];

    /**
     * N    原始集合
     * N_   未确定集合 undetermined users
     * N1   Phase I
     * N2   Phase II
     * N_now    已经确定的边集合
     */
    private int[] N = new int[] {1,2,3,4,5,6,7,8};
    private int[] N_;
    private int[] N1;
    private int[] N2;
    private int[] N_now;

    /**
     * 计算fi(1,a-i) - fi(0,a-i)
     * @param i 用户i
     * @return fi(1,a-i) - fi(0,a-i)
     */
    private double computeUtility(int i) {
        double utility = 0.00;
        for (int j = 0; j < users; j++) {
            if (physicalSet[j][i] == 1 && ArrayUtils.contains(N_now,j+1)) {
                utility = utility + chooseSet[j];
            }
        }
        utility = utility - physicalCost[i];
        for (int j = 0; j < users; j++) {
            if (socialSet[i][j] == 1 && ArrayUtils.contains(N_now,j+1)) {
                utility = utility + socialEdge[i][j] * chooseSet[j];
            }
        }
        return utility;
    }

    public void algorithm(){
        N_ = N;
        /*
         * repeat until N1 ∪ N2 = ∅
         */
        do {
            /*
             * 初始化N_集合中的chooseSet为1
             */
            for (int i = 0; i < N_.length; i++) {
                chooseSet[N_[i] - 1] = 1;
            }
            N1 = N_;
            N_now = N1;
            /*
             * Phase I
             * {N_[i]}倾向于不参与PCG
             */
            for (int i = 0; i < N_.length; i++) {
                if (computeUtility(N_[i] - 1) < 0) {
                    chooseSet[N_[i] - 1] = 0;
                    N1 = ArrayUtils.removeElement(N1, N_[i]);
                    N_now = ArrayUtils.removeElement(N_now, N_[i]);
                }
            }

            /*
             * Phase II
             * {N_[i]}倾向于参与PCG
             */
            for (int i = 0; i < N1.length; i++) {
                N_ = ArrayUtils.removeElement(N_, N1[i]);
            }
            N2 = null;

            for (int i = 0; i < N_.length; i++) {
                if (computeUtility(N_[i] - 1) >= 0) {
                    chooseSet[N_[i] - 1] = 1;
                    N_ = ArrayUtils.removeElement(N_, N_[i]);
                    N2 = ArrayUtils.add(N2, N_[i]);
                    N_now = ArrayUtils.add(N_now, N_[i]);
                }
            }
        }while (N1 == null && N2 == null);

        System.out.println(Arrays.toString(chooseSet));
    }

}
