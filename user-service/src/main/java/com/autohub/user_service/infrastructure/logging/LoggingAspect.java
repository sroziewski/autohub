package com.autohub.user_service.infrastructure.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspect for logging method execution details.
 * Logs method entry, exit, parameters, return values, and execution time.
 */
@Aspect
@Component
public class LoggingAspect {

    /**
     * Pointcut for all service methods in the application package
     */
    @Pointcut("execution(* com.autohub.user_service.application.service..*.*(..))")
    public void servicePointcut() {
    }

    /**
     * Pointcut for all repository methods in the infrastructure package
     */
    @Pointcut("execution(* com.autohub.user_service.infrastructure.persistence.repository..*.*(..))")
    public void repositoryPointcut() {
    }

    /**
     * Pointcut for all controller methods in the presentation package
     */
    @Pointcut("execution(* com.autohub.user_service.presentation.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * Around advice for service methods
     */
    @Around("servicePointcut()")
    public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "SERVICE");
    }

    /**
     * Around advice for repository methods
     */
    @Around("repositoryPointcut()")
    public Object logAroundRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "REPOSITORY");
    }

    /**
     * Around advice for controller methods
     */
    @Around("controllerPointcut()")
    public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "CONTROLLER");
    }

    /**
     * Log method execution with timing information
     */
    private Object logMethodExecution(ProceedingJoinPoint joinPoint, String componentType) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String arguments = Arrays.toString(joinPoint.getArgs());
        
        // Log method entry
        logger.debug("[{}] Entering {}.{}() with arguments: {}", componentType, className, methodName, arguments);
        
        // Measure execution time
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        Object result;
        try {
            // Execute the method
            result = joinPoint.proceed();
            stopWatch.stop();
            
            // Log method exit with result and timing
            logger.debug("[{}] Exiting {}.{}() with result: {} - execution time: {}ms", 
                    componentType, className, methodName, 
                    result != null ? result.toString() : "null", 
                    stopWatch.getTotalTimeMillis());
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            
            // Log exception with timing
            logger.error("[{}] Exception in {}.{}() with cause: {} - execution time: {}ms", 
                    componentType, className, methodName, 
                    e.getMessage(), 
                    stopWatch.getTotalTimeMillis(), 
                    e);
            
            throw e;
        }
    }
}
