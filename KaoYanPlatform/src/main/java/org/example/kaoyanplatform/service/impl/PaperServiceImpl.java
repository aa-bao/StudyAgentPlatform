package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.Paper;
import org.example.kaoyanplatform.mapper.PaperMapper;
import org.example.kaoyanplatform.service.PaperService;
import org.springframework.stereotype.Service;

@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {
}
