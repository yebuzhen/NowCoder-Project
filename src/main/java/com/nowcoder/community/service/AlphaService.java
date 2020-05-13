package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    public AlphaService() {
        System.out.println("Constructing AlphaService");
    }

    @PostConstruct
    public void init() {
        System.out.println("Initialising AlphaService");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("Destroying AlphaService");
    }

    public String find() {
        return alphaDao.select();
    }

}
