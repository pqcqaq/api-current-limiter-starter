package online.zust.qcqcqc.utils.entity;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author pqcmm
 */
public class Limiter implements Serializable {
    @Serial
    private static final long serialVersionUID = 8795320155945399005L;
    private int limitNum;
    private int seconds;
    private String key;
    private boolean limitByUser;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int limitNum;
        private int seconds;
        private String key;
        private boolean limitByUser;

        public Builder() {
        }

        public Builder limitNum(int limitNum) {
            this.limitNum = limitNum;
            return this;
        }

        public Builder seconds(int seconds) {
            this.seconds = seconds;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder limitByUser(boolean limitByUser) {
            this.limitByUser = limitByUser;
            return this;
        }

        public Limiter build() {
            return new Limiter(limitNum, seconds, key, limitByUser);
        }
    }

    public Limiter(int limitNum, int seconds, String key, boolean limitByUser) {
        this.limitNum = limitNum;
        this.seconds = seconds;
        this.key = key;
        this.limitByUser = limitByUser;
    }

    /**
     * 获取
     *
     * @return limitNum
     */
    public int getLimitNum() {
        return limitNum;
    }

    /**
     * 设置
     *
     * @param limitNum 限流次数
     */
    public void setLimitNum(int limitNum) {
        this.limitNum = limitNum;
    }

    /**
     * 获取
     *
     * @return seconds
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * 设置
     *
     * @param seconds 限流时间
     */
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    /**
     * 获取
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置
     *
     * @param key 限流的key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 获取
     *
     * @return limitByUser
     */
    public boolean isLimitByUser() {
        return limitByUser;
    }

    /**
     * 设置
     *
     * @param limitByUser 是否根据用户限流
     */
    public void setLimitByUser(boolean limitByUser) {
        this.limitByUser = limitByUser;
    }

    @Override
    public String toString() {
        return "Limiter{limitNum = " + limitNum + ", seconds = " + seconds + ", key = " + key + ", limitByUser = " + limitByUser + "}";
    }
}
