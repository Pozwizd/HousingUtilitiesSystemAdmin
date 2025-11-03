package org.spacelab.housingutilitiessystemadmin.config.security;

import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Утилита для ручной передачи SecurityContext в async потоки
 */
public class SecurityContextUtils {

    /**
     * Оборачивает Supplier для передачи SecurityContext
     */
    public static <T> Supplier<T> wrapWithSecurityContext(Supplier<T> supplier) {
        // Захватываем контекст в текущем потоке
        final SecurityContext securityContext = SecurityContextHolder.getContext();
        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // Устанавливаем в новом потоке
                SecurityContextHolder.setContext(securityContext);
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }

                // Выполняем операцию
                return supplier.get();

            } finally {
                // Очищаем
                SecurityContextHolder.clearContext();
                MDC.clear();
            }
        };
    }

    /**
     * Оборачивает Runnable для передачи SecurityContext
     */
    public static Runnable wrapWithSecurityContext(Runnable runnable) {
        final SecurityContext securityContext = SecurityContextHolder.getContext();
        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return () -> {
            try {
                SecurityContextHolder.setContext(securityContext);
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }
                runnable.run();
            } finally {
                SecurityContextHolder.clearContext();
                MDC.clear();
            }
        };
    }
}
