package ru.bmstu.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.bmstu.model.User;
import ru.bmstu.model.User.UserRole;

@Aspect
@Component
@Order(1)
public class RoleVerificationAspect {
    // Pointcut выражение
    @Before("execution(* ru.bmstu.service.StudentService.addStudent(..)) && args(user, ..)")
    public void verifyTeacherRoleForAddStudent(User user) {
        if (user.getRole() != UserRole.TEACHER) {
            throw new SecurityException("Only teachers can add students");
        }
    }

    @Before("execution(* ru.bmstu.service.StudentService.removeStudent(..)) && args(.., user)")
    public void verifyTeacherRoleForRemoveStudent(User user) {
        if (user.getRole() != UserRole.TEACHER) {
            throw new SecurityException("Only teachers can remove students");
        }
    }

    @Before("execution(* ru.bmstu.service.StudentService.updateTokens(..)) && args(.., user)")
    public void verifyTeacherRoleForUpdateTokens(User user) {
        if (user.getRole() != UserRole.TEACHER) {
            throw new SecurityException("Only teachers can update tokens");
        }
    }
}