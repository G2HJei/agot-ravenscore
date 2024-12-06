package xyz.zlatanov.ravenscore.web.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ErrorHandlerController {

	@ExceptionHandler(Exception.class)
	public String unknownException(HttpServletRequest request, Exception e) {
		return "error";
	}
}
