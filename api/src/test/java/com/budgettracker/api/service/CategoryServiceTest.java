package com.budgettracker.api.service;

import com.budgettracker.api.dto.CategoryCreate;
import com.budgettracker.api.dto.CategoryResponse;
import com.budgettracker.api.dto.UpdateCategory;
import com.budgettracker.api.entity.Category;
import com.budgettracker.api.entity.Role;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.repository.CategoryRepository;
import com.budgettracker.api.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getCategoriesReturnsAllCategories() {
        PageRequest pageable = PageRequest.of(0, 20);

        Category category1 = new Category(null, "Groceries");
        Category category2 = new Category(null, "Rent");

        Page<Category> categories = new PageImpl<>(
                List.of(category1, category2)
        );

        when(categoryRepository.findAll(pageable))
                .thenReturn(categories);

        Page<CategoryResponse> result =
                categoryService.getCategories(pageable, false);

        assertEquals(2, result.getTotalElements());
        assertEquals("Groceries", result.getContent().get(0).name());
        assertEquals("Rent", result.getContent().get(1).name());

        verify(categoryRepository).findAll(pageable);
        verify(categoryRepository, never())
                .findByIsActiveTrue(pageable);
    }

    @Test
    void getCategoriesReturnsOnlyActiveCategories() {
        PageRequest pageable = PageRequest.of(0, 20);

        Category category = new Category(null, "Transportation");

        Page<Category> categories = new PageImpl<>(
                List.of(category)
        );

        when(categoryRepository.findByIsActiveTrue(pageable))
                .thenReturn(categories);

        Page<CategoryResponse> result =
                categoryService.getCategories(pageable, true);

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Transportation",
                result.getContent().get(0).name()
        );
        assertTrue(result.getContent().get(0).isActive());

        verify(categoryRepository)
                .findByIsActiveTrue(pageable);
        verify(categoryRepository, never())
                .findAll(pageable);
    }

    @Test
    void getCategoryReturnsCategoryById() {
        Category category = new Category(null, "Groceries");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        CategoryResponse result =
                categoryService.getCategory(1L);

        assertEquals("Groceries", result.name());
        assertTrue(result.isActive());

        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryThrowsExceptionWhenCategoryDoesNotExist() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> categoryService.getCategory(99L)
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
        assertEquals(
                "Category not found",
                exception.getReason()
        );
    }

    @Test
    void getAvailableCategoryReturnsActiveCategory() {
        Category category = new Category(null, "Rent");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Category result =
                categoryService.getAvailableCategory(1L);

        assertSame(category, result);
        assertTrue(result.isActive());

        verify(categoryRepository).findById(1L);
    }

    @Test
    void getAvailableCategoryRejectsInactiveCategory() {
        Category category =
                new Category(null, "Old Category", false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> categoryService.getAvailableCategory(1L)
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
        assertEquals(
                "Category is inactive",
                exception.getReason()
        );
    }

    @Test
    void getAvailableCategoryRejectsMissingCategory() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> categoryService.getAvailableCategory(99L)
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void createCategorySucceedsForAdmin() {
        User admin = new User(
                "admin@example.com",
                "Admin",
                Role.ADMIN
        );

        Category savedCategory =
                new Category(admin, "Dining", true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        CategoryResponse result =
                categoryService.createCategory(
                        1L,
                        new CategoryCreate(" Dining ", true)
                );

        assertEquals("Dining", result.name());
        assertTrue(result.isActive());

        verify(userRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategoryRejectsNonAdmin() {
        User user = new User(
                "user@example.com",
                "User",
                Role.USER
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> categoryService.createCategory(
                                1L,
                                new CategoryCreate("Dining", true)
                        )
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );
        assertEquals(
                "Admin access required",
                exception.getReason()
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void createCategoryRejectsMissingUser() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> categoryService.createCategory(
                                99L,
                                new CategoryCreate("Dining", true)
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void updateCategoryChangesActiveStatus() {
        User admin = new User(
                "admin@example.com",
                "Admin",
                Role.ADMIN
        );

        Category category =
                new Category(null, "Dining", true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        when(categoryRepository.findById(7L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(category))
                .thenReturn(category);

        CategoryResponse result =
                categoryService.updateCategory(
                        1L,
                        7L,
                        new UpdateCategory(false)
                );

        assertFalse(result.isActive());
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategoryRejectsNonAdmin() {
        User user = new User(
                "user@example.com",
                "User",
                Role.USER
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> categoryService.updateCategory(
                                1L,
                                7L,
                                new UpdateCategory(false)
                        )
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verify(categoryRepository, never())
                .findById(anyLong());
    }

    @Test
    void deleteCategoryDeletesCategoryForAdmin() {
        User admin = new User(
                "admin@example.com",
                "Admin",
                Role.ADMIN
        );

        Category category =
                new Category(null, "Dining");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        when(categoryRepository.findById(7L))
                .thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L, 7L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategoryRejectsMissingCategory() {
        User admin = new User(
                "admin@example.com",
                "Admin",
                Role.ADMIN
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> categoryService.deleteCategory(1L, 99L)
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(categoryRepository, never())
                .delete(any(Category.class));
    }
}