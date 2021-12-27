package com.hubidaauto.servmarket.module.popularize.model;

/**
 * @Classname RebateConfig
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/27 22:30
 */
public class RebateConfig {
    Integer max, min;
    String rebate;

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public String getRebate() {
        return rebate;
    }

    public void setRebate(String rebate) {
        this.rebate = rebate;
    }

    public Integer compute(int originPrice) {
        switch (this.rebate.charAt(this.rebate.length() - 1)) {
            case '%':
                try {
                    originPrice = originPrice * Integer.valueOf(this.rebate.substring(0, this.rebate.length() - 1));
                    StringBuilder s = new StringBuilder(originPrice);
                    if (s.length() > 2) {
                        s.delete(s.length() - 2, s.length());
                        originPrice = Integer.valueOf(s.toString());

                        if (this.max != null && originPrice > this.max) {
                            originPrice = this.max;
                            break;
                        }
                    } else {
                        originPrice = 0;
                    }
                    if (this.min != null && originPrice < this.min) {
                        originPrice = this.min;
                        break;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    originPrice = 0;
                }
                break;
            default:
                originPrice = Integer.valueOf(this.rebate);
                break;
        }
        return originPrice;
    }
}
