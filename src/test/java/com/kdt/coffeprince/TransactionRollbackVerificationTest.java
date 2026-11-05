
package com.kdt.coffeprince;

import com.kdt.coffeprince.entity.Category;
import com.kdt.coffeprince.entity.Menu;
import com.kdt.coffeprince.repository.CategoryRepository;
import com.kdt.coffeprince.repository.MenuRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionRollbackVerificationTest {

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    void 실제_DB_자동_롤백_검증() {
        TransactionTemplate transaction =
                new TransactionTemplate(transactionManager);

        AtomicReference<Long> menuId = new AtomicReference<>();
        String name = "AUTO_ROLLBACK_" + UUID.randomUUID();

        assertThrows(IllegalStateException.class, () ->
                transaction.execute(status -> {

                    Category category = categoryRepository.findById(1L)
                            .orElseThrow();

                    Menu menu = new Menu(
                            category,
                            name,
                            1000,
                            "Spring 자동 롤백 검증"
                    );

                    Menu saved = menuRepository.saveAndFlush(menu);
                    menuId.set(saved.getMenuId());

                    assertTrue(menuRepository.existsById(menuId.get()));

                    throw new IllegalStateException("자동 롤백 검증용 예외");
                })
        );

        assertNotNull(menuId.get());
        assertFalse(menuRepository.existsById(menuId.get()));

        System.out.println("자동 롤백 검증 성공! menu_id=" + menuId.get());
    }
}

