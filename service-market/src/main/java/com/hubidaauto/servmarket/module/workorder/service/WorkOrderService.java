package com.hubidaauto.servmarket.module.workorder.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Classname WorkOrderService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/28 17:09
 */
@DS("shop")
//@Service
@Transactional(rollbackFor = Throwable.class)
public class WorkOrderService {
}
