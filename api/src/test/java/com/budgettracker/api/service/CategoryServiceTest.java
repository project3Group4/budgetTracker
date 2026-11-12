package com.budgettracker.api.service;

import com.budgettracker.api.dto.CategoryResponse;
import com.budgettracker.api.entity.Category;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.repository.CategoryRepository;

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

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getCategoriesReturnsAllCategories() {

        // Arrange
        PageRequest pageable = PageRequest.of(0, 20);

        Category category1 = new Category(null, "Groceries");
        Category category2 = new Category(null, "Rent");

        Page<Category> categories = new PageImpl<>(
                List.of(category1, category2)
        );

        when(categoryRepository.findAll(pageable))
                .thenReturn(categories);

        // Act
        Page<CategoryResponse> result =
                categoryService.getCategories(pageable, false);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals("Groceries", result.getContent().get(0).name());
        assertEquals("Rent", result.getContent().get(1).name());

        verify(categoryRepository).findAll(pageable);
        verify(categoryRepository, never()).findByIsActiveTrue(pageable);
    }

    @Test
    void getCategoriesReturnsOnlyActiveCategories() {

        // Arrange
        PageRequest pageable = PageRequest.of(0, 20);

        Category category = new Category(null, "Transportation");

        Page<Category> categories = new PageImpl<>(
                List.of(category)
        );

        when(categoryRepository.findByIsActiveTrue(pageable))
                .thenReturn(categories);

        // Act
        Page<CategoryResponse> result =
                categoryService.getCategories(pageable, true);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Transportation", result.getContent().get(0).name());
        assertTrue(result.getContent().get(0).isActive());

        verify(categoryRepository).findByIsActiveTrue(pageable);
        verify(categoryRepository, never()).findAll(pageable);
    }

    @Test
    void getCategoriesReturnsEmptyPage() {

        // Arrange
        PageRequest pageable = PageRequest.of(0, 20);

        when(categoryRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        // Act
        Page<CategoryResponse> result =
                categoryService.getCategories(pageable, false);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void getAvailableCategoryReturnsActiveCategory() {

        // Arrange
        Category category = new Category(null, "Groceries");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        // Act
        Category result = categoryService.getAvailableCategory(1L);

        // Assert
        assertSame(category, result);
        assertEquals("Groceries", result.getName());
        assertTrue(result.isActive());

        verify(categoryRepository).findById(1L);
    }

    @Test
    void getAvailableCategoryRejectsMissingCategory() {

        // Arrange
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.getAvailableCategory(99L)
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Category not found", exception.getReason());

        verify(categoryRepository).findById(99L);
    }

    @Test
    void getAvailableCategoryRejectsInactiveCategory() {

        // Arrange
        Category category = mock(Category.class);

        when(category.isActive()).thenReturn(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.getAvailableCategory(1L)
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Category is inactive", exception.getReason());

        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoriesReturnsCreatedByUserId() {

        // Arrange
        PageRequest pageable = PageRequest.of(0, 20);

        User admin = mock(User.class);

        when(admin.getId()).thenReturn(1L);

        Category category = new Category(admin, "Groceries");

        Page<Category> categories = new PageImpl<>(
                List.of(category)
        );

        when(categoryRepository.findAll(pageable))
                .thenReturn(categories);

        // Act
        Page<CategoryResponse> result =
                categoryService.getCategories(pageable, false);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Groceries", result.getContent().get(0).name());
        assertEquals(1L, result.getContent().get(0).createdByUserId());

        verify(categoryRepository).findAll(pageable);
    }

}