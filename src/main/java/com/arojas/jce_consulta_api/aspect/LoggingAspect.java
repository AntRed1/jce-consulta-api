/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.arojas.jce_consulta_api.entity.LogEntry.LogLevel;
import com.arojas.jce_consulta_api.service.DbLoggerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 *         * Aspecto para logging automático de métodos
 */

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingAspect {

	private final DbLoggerService dbLoggerService;

	@Around("@annotation(logExecution)")
	public Object logExecutionTime(ProceedingJoinPoint joinPoint, LogExecution logExecution) throws Throwable {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		String methodName = joinPoint.getSignature().getName();
		String className = joinPoint.getTarget().getClass().getSimpleName();
		String source = className + "." + methodName;

		try {
			Object result = joinPoint.proceed();
			stopWatch.stop();

			long executionTime = stopWatch.getTotalTimeMillis();

			dbLoggerService.log()
					.level(LogLevel.INFO)
					.source(source)
					.operation(logExecution.operation().isEmpty() ? methodName : logExecution.operation())
					.message("Method executed successfully")
					.executionTime(executionTime)
					.context("parameters", getParameterInfo(joinPoint))
					.context("returnType", result != null ? result.getClass().getSimpleName() : "void")
					.save();

			return result;

		} catch (Exception e) {
			stopWatch.stop();

			dbLoggerService.log()
					.level(LogLevel.ERROR)
					.source(source)
					.operation(logExecution.operation().isEmpty() ? methodName : logExecution.operation())
					.message("Method execution failed")
					.executionTime(stopWatch.getTotalTimeMillis())
					.exception(e)
					.context("parameters", getParameterInfo(joinPoint))
					.save();

			throw e;
		}
	}

	private String getParameterInfo(ProceedingJoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		if (args == null || args.length == 0) {
			return "No parameters";
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i > 0)
				sb.append(", ");
			Object arg = args[i];
			sb.append("param").append(i).append("=")
					.append(arg != null ? arg.getClass().getSimpleName() : "null");
		}
		return sb.toString();
	}
}
