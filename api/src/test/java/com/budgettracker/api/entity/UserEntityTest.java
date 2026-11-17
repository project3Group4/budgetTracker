package com.budgettracker.api.entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {
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