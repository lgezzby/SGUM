package com.zjgsu.util;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 《Exploiting Social Tie Structure for Cooperative Wireless Networking: A Social Group Utility Maximization Framework》
 * Distributed Spectrum Access Algorithm For Social Group Utility Maximization
 */
public class DistributedSpectrumAccess {
    /**
     * 传递参数sumSocial;
     */
    private static double sumSocial = 0.00;
    /**
     * Initialization:
     * 1.初始化参数θ = 10^6 和 信道更新率τ(怎么取)
     */
    private static final double theta = 10 * Math.pow(10,6);
    private static final double tau = 0.01;
    /**
     * 2.初始化 a square are of a length of 500m with 8 scattered white-space users
     */
    private static final int spectrumNumber = 5;
    private static final int users = 8;
    /**
     * spectrumSet          用户对应的空闲频谱集合(set of vacant channels)
     * socialSet            社会关系集合(有:1; 无:0)
     * socialEdge           社会权重集合
     * physicalNode         物理信息集合
     * physicalSet          物理关系集合(干扰:1; 无干扰:0)
     * physicalEdge         物理距离集合
     * physicalSocialSet    社会物理关系集合(均有关联:1; 其他:0)
     */
    private static final int[][] spectrumSet = {{2, 3, 4}, {1, 3, 4}, {1, 2, 4}, {1, 2, 3}, {1, 3, 5}, {1, 4, 5}, {1, 2, 5}, {1, 3, 4}};
    private static final int[][] socialSet = {
            {0, 0, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 1},
            {1, 1, 0, 0, 0, 1, 0, 0},
            {1, 0, 0, 0, 0, 0, 1, 1},
            {0, 0, 1, 1, 0, 0, 1, 0},
            {0, 0, 0, 0, 1, 1, 0, 0},
            {0, 0, 1, 0, 1, 0, 0, 0}};
    private static final double[][] socialEdge = {
            {0, 0, 0, 1.0, 1.0, 0, 0, 0},
            {0, 0, 0, 0.9, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0.7, 0, 1.0},
            {1.0, 0.9, 0, 0, 0, 1.0, 0, 0},
            {1.0, 0, 0, 0, 0, 0, 0.9, 1.0},
            {0, 0, 0.7, 1.0, 0, 0, 0.8, 0},
            {0, 0, 0, 0, 0.9, 0.8, 0, 0},
            {0, 0, 1.0, 0, 1.0, 0, 0, 0}};
    private static final int[][] physicalNode = {
            {100, 100}, {400, 100}, {200, 200}, {300, 200}, {200, 300}, {400, 300}, {100, 400}, {400, 400}};
    private int[][] physicalSet = new int[users][users];
    private double[][] physicalEdge = new double[users][users];
    private int[][] physicalSocialSet = new int[users][users];
    /**
     * timerManager     定时器信息
     * updateManager    更新状况信息
     */
    private double[] timerManager = new double[users];
    private int[] updateManager = new int[users];
    /**
     * chooseSet    信道选择
     */
    private int[] chooseSet = new int[users];
    /**
     * utility              用户效益
     * socialGroupUtility   社会群体效益
     */
    private static double[] utility = new double[users];
    private static double[] socialGroupUtility = new double[users];

    /**
     * TODO 单位配平问题:
     * dBm意即分贝毫瓦，可以作为电压或功率的单位。
     * dBm=10*lg(mW) 
     * <p>
     * transmission power 100 mW (same power level)
     * path loss factor α = 4
     * background interference power for each channel is randomly assigned in the interval of [-100,-90] dBm]
     * <p>
     * transmission range δ = 500 m
     */
    private static final int P = 100;
    private static final double alpha = 4;
    private double[] omega = new double[spectrumNumber];
    private static final int delta = 500;

    /**
     * 设定物理干扰半径,计算影响集
     *
     * @param delta 物理干扰半径
     */
    private void computePhysicalEdge(int delta) {
        for (int i = 0; i < users; i++) {
            for (int j = 0; j < users; j++) {
                physicalEdge[i][j] = Math.sqrt(Math.pow((physicalNode[i][0] - physicalNode[j][0]), 2) + Math.pow((physicalNode[i][1] - physicalNode[j][1]), 2));
                if (i == j) {
                    physicalSet[i][j] = 0;
                } else {
                    if (physicalEdge[i][j] <= delta) {
                        physicalSet[i][j] = 1;
                    } else {
                        physicalSet[i][j] = 0;
                    }
                }
            }
        }
    }

    /**
     * 计算物理社会图
     * 值为1:同时存在物理关联和社会联系
     */
    private void computePhysicalSocialSet() {
        for (int i = 0; i < users; i++) {
            for (int j = 0; j < users; j++) {
                physicalSocialSet[i][j] = physicalSet[i][j] * socialSet[i][j];
            }
        }
    }

    /**
     * 计算ω
     */
    private void computeOmega() {
        /*
         * 单位是dBm
         */
        Random random = new Random();
        for (int i = 0; i < spectrumNumber; i++) {
            omega[i] = -100 + random.nextInt(10);
        }
    }

    private double exponential(Random rng, double mean) {
        return -mean * Math.log(rng.nextDouble());
    }

    /**
     * 计算τ timer
     * 产生指数分布的定时器,定时器的均值为1/τ
     * 并初始化updateManager(均置为1)
     */
    private void computeTimer() {
        Random random = new Random();
        for (int i = 0; i < users; i++) {
            timerManager[i] = exponential(random, 1 / tau);
//            updateManager[i] = 1;
        }
    }

    /**
     * 信道随机选择
     *
     * @param i 需要选择信道的用户i
     * @return 返回选择的信道
     */
    private int chooseSpectrum(int i) {
        Random random = new Random();
        int arrIdx = random.nextInt(spectrumSet[i].length - 1);
        return spectrumSet[i][arrIdx];
    }

    /**
     * 计算势函数 φ = φ1 + φ2
     *
     * @return φ 势函数
     */
    private double computeFai() {
        double fai;
        double fai1 = 0.00; // part fai1
        double fai2 = 0.00; // part fai2
        for (int n = 0; n < users; n++) {
            fai1 = 0.00; // part fai1
            fai2 = 0.00; // part fai2
            /*
             * φ1计算
             */
            fai1 = fai1 + (-Math.pow(10, (omega[chooseSet[n] - 1] / 10)));
            for (int m = 0; m < users; m++) {
                if (physicalSet[n][m] == 1 && chooseSet[n] == chooseSet[m]) {
                    fai1 = fai1 + (-0.5 * P * Math.pow(physicalEdge[n][m], -alpha));
                }
            }
            /*
             * φ2计算
             */
            for (int m = 0; m < users; m++) {
                if (physicalSocialSet[n][m] == 1 && chooseSet[n] == chooseSet[m]) {
                    fai2 = fai2 + (-0.5 * socialEdge[n][m] * P * Math.pow(physicalEdge[n][m], -alpha));
                }
            }

        }
        fai = fai1 + fai2;
        return fai;
    }

    /**
     * 计算Un(an,a-n)
     * U = -γ = - ΣP * d^-α * I - ω^n
     *
     * @param n 用户n
     */
    private double computeUtility(int n) {
        double U = 0.00;
        // U = U - (omega[chooseSet[n] - 1]);
        U = U - Math.pow(10, (omega[chooseSet[n] - 1] / 10));
        for (int m = 0; m < users; m++) {
            if (physicalSet[n][m] == 1 && chooseSet[n] == chooseSet[m]) {
                U = U - P * Math.pow(physicalEdge[n][m], -alpha);
            }
        }
        return U;
    }

    /**
     * 计算Sn(an,a-n)
     *
     * @param n 用户n
     */
    private double computeSocialGroupUtility(int n) {
        double S = utility[n];
        for (int m = 0; m < users; m++) {
            if (socialSet[n][m] == 1) {
                S = S + socialEdge[n][m] * utility[m];
            }
        }
        return S;
    }

    /**
     * Sum social group utility
     */
    private double sumSocialGroupUtility() {
        double sum = 0.00;
        for (int i = 0; i < users; i++) {
            sum = sum + socialGroupUtility[i];
        }
        return sum;
    }

    /**
     * 算法主体
     */

    public void algorithm() {
        /*
         * 设定干扰半径,并计算物理图(physical graph)
         * set interference range
         */
        computePhysicalEdge(delta);
        /*
         * 设定基站干扰
         * set omega
         */
        computeOmega();
        /*
         * 初始化信道选择(各用户随机一个信道)
         * choose a channel randomly for each user n
         */
        for (int i = 0; i < users; i++) {
            chooseSet[i] = chooseSpectrum(i);
        }
        /*
         * 计算物理社会图 physical-social graph
         */
        computePhysicalSocialSet();
        /*
         * 计算timer
         */
        computeTimer();
        /*
         * 计算用户效益
         */
        for (int i = 0; i < users; i++) {
            utility[i] = computeUtility(i);
        }
        /*
         * 计算初始化社会群体效益
         * compute social group utility S(an,a-n)
         */
        for (int i = 0; i < users; i++) {
            socialGroupUtility[i] = computeSocialGroupUtility(i);
        }
        /*
         * 生成定时装置
         * 方法名称schedule()和scheduleAtFixedRate()两者的区别
         * <1>schedule()方法更注重保持间隔时间的稳定：保障每隔period时间可调用一次
         * <2>scheduleAtFixedRate()方法更注重保持执行频率的稳定：保障多次调用的频率趋近于period时间，如果任务执行时间大于period，会在任务执行之后马上执行下一次任务
         */
        while (true) {
            for (int i = 0; i < users; i++) {
                TimerTask task = new SynchroTimerTask(i);
                Timer timer = new Timer();
                timer.schedule(task, 0, (long) timerManager[i] == 0 ? 1 : (long) timerManager[i]);
            }
        }

    }

    /**
     * 同步计时器任务:
     * 1.设置定时器并倒计时
     * 2.重新选择信道(随机选择)
     * 3.计算并比较social group utility((1)大于等于,(2)小于)
     * 4.根据social group utility,以对应概率转移信道选择
     */
    public class SynchroTimerTask extends TimerTask {

        private int user;

        SynchroTimerTask(int n) {
            this.user = n;
        }

        /**
         * 任务
         */
        @Override
        public void run() {
            synchronized (this){
                doTimeTask(this.user);
            }
            this.cancel();
        }

        /**
         * 加锁
         */

    }

    public synchronized void doTimeTask(int user) {
            /*
             * 记录原始状态
             */
        double probabilityRandom;
        double probabilityTransition;
        int oldChooseSet = chooseSet[user];
        int newChooseSet;
        double oldUtility = utility[user];
        double newUtility;
        double oldSocialGroupUtility = socialGroupUtility[user];
        double newSocialGroupUtility;
            /*
             * 重新选择信道(更新信道选择)
             */
        newChooseSet = chooseSpectrum(user);
        while (newChooseSet == oldChooseSet){
            newChooseSet = chooseSpectrum(user);
        }
        chooseSet[user] = newChooseSet;
            /*
             * 计算SGUM
             */
        newUtility = computeUtility(user);
        utility[user] = newUtility;
        newSocialGroupUtility = computeSocialGroupUtility(user);
        socialGroupUtility[user] = newSocialGroupUtility;
            /*
             * 比较S'(an,a-n) -- S(an,a-n)
             */
        if (newSocialGroupUtility >= oldSocialGroupUtility) {
                /*
                 * 转移,同时修改updateManager为1(表示有内容成功更新)
                 */
            chooseSet[user] = newChooseSet;
            utility[user] = newUtility;
            socialGroupUtility[user] = newSocialGroupUtility;
            for (int i = 0; i < users; i++) {
                socialGroupUtility[i] = computeUtility(i);
            }

//            updateManager[user] = 1;
        } else {
                /*
                 * probabalityRandom 返回[0.00,1.00]之间的随机数
                 * 生成转移的概率,概率转移
                 * 同时修改updateManager为0(表示无内容成功更新),且updateManager中内容均为0表示定时器全部需要结束(更新完成)
                 */
            Random random = new Random();
            probabilityRandom = random.nextDouble();
            probabilityTransition = Math.exp(theta * newSocialGroupUtility)
                    / Math.exp(theta * oldSocialGroupUtility);
            if (probabilityRandom <= probabilityTransition) {
                    /*
                     * 若符合概率内,继续转移
                     */
                chooseSet[user] = newChooseSet;
                utility[user] = newUtility;
                socialGroupUtility[user] = newSocialGroupUtility;
                for (int i = 0; i < users; i++) {
                    socialGroupUtility[i] = computeUtility(i);
                }
            } else {
                    /*
                     * 返回原来状态
                     */
                /*newChooseSet = oldChooseSet;
                newUtility = oldUtility;
                newSocialGroupUtility = oldSocialGroupUtility;*/
                chooseSet[user] = oldChooseSet;
                utility[user] = oldUtility;
                socialGroupUtility[user] = oldSocialGroupUtility;
            }
        }



                /*updateManager[user] = 0;
                int sum = 0;
                for (int i = 0; i < user; i++) {
                    sum = sum + updateManager[i];
                }*/

                /*if (sum == 0){
                    this.cancel();
                    System.out.println("定时器[" + this.user + "]:此时社会群体效益总和为-->" + sumSocialGroupUtility());
                }*/
        sumSocial = sumSocialGroupUtility();
        System.out.println("定时器[" + user + "]:此时社会群体效益总和为-->" + sumSocial);
    }

    /**
     * 传递数据
     */
    public static double transitionData() {
        return sumSocial;
    }
}
