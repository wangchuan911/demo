package com.hubidaauto.carservice.wxapp.core.service;

import com.hubidaauto.carservice.wxapp.core.config.CustomConst;
import com.hubidaauto.carservice.wxapp.core.dao.PictureDao;
import com.hubidaauto.carservice.wxapp.core.entity.PictureVO;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.welisdoon.common.StreamUtils;
import org.welisdoon.web.common.web.AbstractBaseService;
import org.welisdoon.web.vertx.annotation.VertxWebApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

@Service
@Transactional(rollbackFor = Throwable.class)
public class PictureService extends AbstractBaseService<PictureVO> {
    @Autowired
    PictureDao pictureDao;

    @Override
    @VertxWebApi
    public Object handle(int exeCode, Map params) {
        Object returnObj = null;
        PictureVO pictureVO;
        switch (exeCode) {
            case CustomConst.GET:
                pictureVO = mapToObject(params, PictureVO.class);
                returnObj = pictureDao.get(pictureVO);
                break;
            case CustomConst.DELETE:
                pictureVO = mapToObject(params, PictureVO.class);
                pictureDao.del(pictureVO);
                break;
        }
        return returnObj;
    }

    @VertxWebApi
    public Object uploadFile(Map fileUpload, Map map) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        Object returnObj = null;
        try {
            inputStream = new FileInputStream(new File(MapUtils.getString(fileUpload, "uploadedFileName")));
            outputStream = new ByteArrayOutputStream();
            StreamUtils.write(inputStream, outputStream);
            byte[] bytes = outputStream.toByteArray();
            PictureVO pictureVO = new PictureVO()
                    .setData(bytes)
                    .setName(MapUtils.getString(fileUpload, "fileName"))
                    .setOrderId(MapUtils.getInteger(map, "orderId"))
                    .setTacheId(MapUtils.getInteger(map, "tacheId"));
            pictureDao.add(pictureVO);
            returnObj = pictureVO.setData(null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return returnObj;
    }
}
