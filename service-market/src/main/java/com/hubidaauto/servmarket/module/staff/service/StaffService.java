package com.hubidaauto.servmarket.module.staff.service;

import com.hubidaauto.servmarket.module.staff.dao.StaffDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Classname StaffService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/27 09:44
 */
@Service
@Transactional(rollbackFor = Throwable.class)
public class StaffService {
    StaffDao staffDao;

    public void setStaffDao(StaffDao staffDao) {
        this.staffDao = staffDao;
    }
}
