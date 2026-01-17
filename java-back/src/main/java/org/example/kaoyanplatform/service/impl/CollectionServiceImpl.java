package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.Collection;
import org.example.kaoyanplatform.mapper.CollectionMapper;
import org.example.kaoyanplatform.service.CollectionService;
import org.springframework.stereotype.Service;

@Service
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, Collection> implements CollectionService {
}
