package com.budgettracker.api;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.entity.Role;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest{
    @Test
    void testConstructorWorks(){
        User user = new User("test.email@gmail.com", "testAdmin", Role.ADMIN);

        assertEquals("test.email@gmail.com", user.getEmail());
        assertEquals("testAdmin", user.getDisplayName());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void testUSERIsDefaultRole() {
        User user = new User("test.email@gmail.com", "testUser", null);

        assertEquals(Role.USER, user.getRole());
    }
}