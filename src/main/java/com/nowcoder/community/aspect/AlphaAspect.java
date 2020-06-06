package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/** @author barea */
//@Component
//@Aspect
public class AlphaAspect {

  @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
  public void pointcut() {}

  @Before("pointcut()")
  public void before() {
    System.out.println("before");
  }

  @After("pointcut()")
  public void after() {
    System.out.println("after");
  }

  @AfterReturning("pointcut()")
  public void afterReturning() {
    System.out.println("afterReturning");
  }

  @AfterThrowing("pointcut()")
  public void afterThrowing() {
    System.out.println("afterThrowing");
  }

  @Around("pointcut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

    System.out.println("around before");
    Object object = joinPoint.proceed();
    System.out.println("around after");
    return object;
  }
}
