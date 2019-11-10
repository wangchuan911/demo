package pers.welisdoon.webserver.service.custom.dao;

import org.springframework.stereotype.Repository;
import pers.welisdoon.webserver.common.dao.IDao;
import pers.welisdoon.webserver.service.custom.entity.PictureVO;
import pers.welisdoon.webserver.service.custom.entity.UserVO;

import java.util.List;

@Repository
public interface PictureDao extends IDao<PictureVO, Integer> {

}
