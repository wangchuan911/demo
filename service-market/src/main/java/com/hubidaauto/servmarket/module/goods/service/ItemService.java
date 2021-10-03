package com.hubidaauto.servmarket.module.goods.service;

import com.hubidaauto.servmarket.module.common.dao.TextContentDao;
import com.hubidaauto.servmarket.module.goods.dao.AddedValueDao;
import com.hubidaauto.servmarket.module.goods.dao.ItemDao;
import com.hubidaauto.servmarket.module.goods.dao.ItemTypeDao;
import com.hubidaauto.servmarket.module.goods.entity.AddedValueVO;
import com.hubidaauto.servmarket.module.goods.entity.ItemCondition;
import com.hubidaauto.servmarket.module.goods.entity.ItemTypeVO;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname ItemService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/4 21:52
 */
@Service
public class ItemService {
    ItemDao itemDao;
    ItemTypeDao itemTypeDao;
    ItemService itemService;
    TextContentDao itemDetailDao;
    AddedValueDao addedValueDao;

    @Autowired
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    @Autowired
    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }

    @Autowired
    public void setItemTypeDao(ItemTypeDao itemTypeDao) {
        this.itemTypeDao = itemTypeDao;
    }

    @Autowired
    public void setItemDetailDao(TextContentDao itemDetailDao) {
        this.itemDetailDao = itemDetailDao;
    }

    @Autowired
    public void setAddedValueDao(AddedValueDao addedValueDao) {
        this.addedValueDao = addedValueDao;
    }

    public ItemVO getItem(Long id) {
        ItemVO itemVO = itemDao.get(id);
        itemVO.setDetail(itemDetailDao.get(itemVO.getContentId()));
        return itemVO;
    }

    public List<ItemVO> listItem(ItemCondition itemCondition) {
        return itemDao.list(itemCondition);
    }

    public List<ItemTypeVO> listTypes(ItemCondition itemCondition) {
        return itemTypeDao.list(itemCondition);
    }

    public List<AddedValueVO> listAddedValue(AddedValueVO addedValueVO) {
        Set<String> group = new HashSet<>();
        return addedValueDao.list(addedValueVO).stream().filter(addedValueVO1 -> {
            if (group.contains(addedValueVO1.getGroup())) {
                if (addedValueVO1.getGroup().startsWith("LIST_")) {
                    return true;
                } else if (addedValueVO1.getGroup().startsWith("ONCE_")) {
                    return false;
                }
            }
            group.add(addedValueVO1.getGroup());
            return true;

        }).collect(Collectors.toList());
    }
}
