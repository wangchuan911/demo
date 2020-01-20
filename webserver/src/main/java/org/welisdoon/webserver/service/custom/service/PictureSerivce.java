package org.welisdoon.webserver.service.custom.service;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.StreamUtils;
import org.welisdoon.webserver.service.custom.config.CustomConst;
import org.welisdoon.webserver.service.custom.dao.PictureDao;
import org.welisdoon.webserver.service.custom.dao.UserDao;
import org.welisdoon.webserver.service.custom.entity.PictureVO;
import org.welisdoon.webserver.service.custom.entity.UserVO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class PictureSerivce extends AbstractBaseService {
    @Autowired
    PictureDao pictureDao;

    @Override
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

    public Object uploadFile(Map fileUpload, Map map) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        Object returnObj = null;
        try {
            inputStream = new FileInputStream(new File(MapUtils.getString(fileUpload, "uploadedFileName")));
            outputStream = new ByteArrayOutputStream();
            StreamUtils.writeStream(inputStream, outputStream);
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
        } finally {
            StreamUtils.close(inputStream);
            StreamUtils.close(outputStream);
        }
        return returnObj;
    }
}
