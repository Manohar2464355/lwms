
package com.example.lwms1.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, RedirectAttributes ra, HttpServletRequest request) {
        ra.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", "Resource not found: " + ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(DataIntegrityViolationException ex, RedirectAttributes ra, HttpServletRequest request) {
        ra.addFlashAttribute("errorMessage", "Database Error: This record is currently in use and cannot be modified.");
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    // Safety Net 1: Handles Security/Permission issues
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", "Access Denied: You do not have permission for this action.");
        return "redirect:/"; // Usually sends them to their respective home/dashboard
    }

    // Safety Net 2: Catch-all for unexpected crashes
    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, RedirectAttributes ra) {
        // Log the error here for the developer!
        ra.addFlashAttribute("errorMessage", "An unexpected system error occurred.");
        return "redirect:/";
    }
}