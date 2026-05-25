package acceptance.publicapi.menu;

import com.ffresco.totem.common.domain.enums.Currency;
import com.ffresco.totem.common.domain.model.Money;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicMenuCommand;
import com.ffresco.totem.publicapi.menu.application.port.in.GetPublicProductDetailCommand;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicMenuPort;
import com.ffresco.totem.publicapi.menu.application.port.out.LoadPublicTablePort;
import com.ffresco.totem.publicapi.menu.application.service.GetPublicMenuUseCaseImpl;
import com.ffresco.totem.publicapi.menu.application.service.GetPublicProductDetailUseCaseImpl;
import com.ffresco.totem.publicapi.menu.domain.exception.PublicProductNotFoundException;
import com.ffresco.totem.publicapi.menu.domain.exception.PublicTableNotFoundException;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenu;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuCategory;
import com.ffresco.totem.publicapi.menu.domain.model.PublicMenuProduct;
import com.ffresco.totem.publicapi.menu.domain.model.PublicTable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerOpensPublicMenuAcceptanceTest {

    @Test
    void customerScansQrAndSeesAvailableMenuAndProductDetail() {
        // given: a configured branch with a public table and a menu containing
        // one available burger and one unavailable veggie burger.
        var fixtures = givenBranchWithMenu();
        var menuService = new GetPublicMenuUseCaseImpl(fixtures.tables, fixtures.menus);
        var productService = new GetPublicProductDetailUseCaseImpl(fixtures.tables, fixtures.menus);

        // when: the customer opens the menu via the QR-encoded public table id
        var menuView = menuService.execute(new GetPublicMenuCommand("tbl-public-001"));

        // then: only available products are visible, and the table context is
        // resolved so the kiosk knows which table this corresponds to.
        assertThat(menuView.tableId()).isEqualTo("tbl-001");
        assertThat(menuView.menu().branchId()).isEqualTo("branch-001");
        assertThat(menuView.menu().categories()).hasSize(1);
        assertThat(menuView.menu().categories().get(0).products())
                .extracting(PublicMenuProduct::id)
                .containsExactly("prd-burger");

        // when: the customer taps a visible product, the detail is returned
        var detail = productService.execute(
                new GetPublicProductDetailCommand("tbl-public-001", "prd-burger")
        );

        // then: the response carries category context and table context
        assertThat(detail.product().id()).isEqualTo("prd-burger");
        assertThat(detail.categoryId()).isEqualTo("cat-burgers");
        assertThat(detail.categoryName()).isEqualTo("Burgers");
        assertThat(detail.tableId()).isEqualTo("tbl-001");
    }

    @Test
    void customerScanningUnknownQrCodeIsRejected() {
        var fixtures = givenBranchWithMenu();
        var menuService = new GetPublicMenuUseCaseImpl(fixtures.tables, fixtures.menus);

        assertThatThrownBy(() -> menuService.execute(new GetPublicMenuCommand("tbl-unknown")))
                .isInstanceOf(PublicTableNotFoundException.class);
    }

    @Test
    void customerCannotOpenAnUnavailableProduct() {
        var fixtures = givenBranchWithMenu();
        var productService = new GetPublicProductDetailUseCaseImpl(fixtures.tables, fixtures.menus);

        // prd-veggie is in the menu but available=false → public surface treats it as missing
        assertThatThrownBy(() -> productService.execute(
                new GetPublicProductDetailCommand("tbl-public-001", "prd-veggie")
        )).isInstanceOf(PublicProductNotFoundException.class);
    }

    @Test
    void customerCannotOpenAProductThatDoesNotExist() {
        var fixtures = givenBranchWithMenu();
        var productService = new GetPublicProductDetailUseCaseImpl(fixtures.tables, fixtures.menus);

        assertThatThrownBy(() -> productService.execute(
                new GetPublicProductDetailCommand("tbl-public-001", "prd-unknown")
        )).isInstanceOf(PublicProductNotFoundException.class);
    }

    private static Fixtures givenBranchWithMenu() {
        var publicTable = new PublicTable("tenant-001", "branch-001", "tbl-001", "tbl-public-001");
        var menu = new PublicMenu(
                "tenant-001",
                "branch-001",
                Currency.BRL,
                List.of(new PublicMenuCategory("cat-burgers", "Burgers", List.of(
                        new PublicMenuProduct(
                                "prd-burger", "Cheeseburger", "Beef.",
                                new Money(new BigDecimal("12.50"), Currency.BRL), true
                        ),
                        new PublicMenuProduct(
                                "prd-veggie", "Veggie", null,
                                new Money(new BigDecimal("11.00"), Currency.BRL), false
                        )
                )))
        );

        Map<String, PublicTable> tablesByPublicId = new HashMap<>();
        tablesByPublicId.put(publicTable.tablePublicId(), publicTable);
        LoadPublicTablePort tables = tablePublicId -> {
            var found = tablesByPublicId.get(tablePublicId);
            if (found == null) {
                throw new PublicTableNotFoundException(tablePublicId);
            }
            return found;
        };

        LoadPublicMenuPort menus = (tenantId, branchId) -> menu;

        return new Fixtures(tables, menus);
    }

    private record Fixtures(LoadPublicTablePort tables, LoadPublicMenuPort menus) {
    }
}
