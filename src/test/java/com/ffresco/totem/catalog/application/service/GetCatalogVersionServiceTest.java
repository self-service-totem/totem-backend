package com.ffresco.totem.catalog.application.service;

import com.ffresco.totem.catalog.application.port.in.GetCatalogVersionCommand;
import com.ffresco.totem.catalog.application.port.out.LoadCatalogVersionPort;
import com.ffresco.totem.catalog.domain.model.CatalogVersion;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetCatalogVersionServiceTest {

    @Test
    void shouldReturnCatalogVersionForBranch() {
        Instant version = Instant.parse("2026-05-20T12:00:00Z");
        LoadCatalogVersionPort loadCatalogVersionPort = branchId -> new CatalogVersion(
                branchId,
                version,
                "default"
        );
        var service = new GetCatalogVersionService(loadCatalogVersionPort);

        var result = service.execute(new GetCatalogVersionCommand("branch-001"));

        assertThat(result.branchId()).isEqualTo("branch-001");
        assertThat(result.catalogVersion()).isEqualTo(version);
        assertThat(result.priceListId()).isEqualTo("default");
    }

    @Test
    void shouldRejectEmptyBranchId() {
        LoadCatalogVersionPort loadCatalogVersionPort = branchId -> new CatalogVersion(
                branchId,
                Instant.parse("2026-05-20T12:00:00Z"),
                "default"
        );
        var service = new GetCatalogVersionService(loadCatalogVersionPort);

        assertThatThrownBy(() -> service.execute(new GetCatalogVersionCommand(" ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("branchId is required");
    }
}
