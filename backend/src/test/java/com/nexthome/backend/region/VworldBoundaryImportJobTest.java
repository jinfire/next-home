package com.nexthome.backend.region;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class VworldBoundaryImportJobTest {

    @Test
    void importsOnlyWhenTheDedicatedCommandArgumentIsPresent() {
        VworldBoundaryGateway gateway = mock(VworldBoundaryGateway.class);
        VworldBoundaryImportService importer = mock(VworldBoundaryImportService.class);
        when(gateway.fetchSigungu()).thenReturn("{\"type\":\"FeatureCollection\",\"features\":[]}");
        VworldBoundaryImportJob job = new VworldBoundaryImportJob(gateway, importer);

        job.run(new DefaultApplicationArguments("--import-vworld-boundaries"));

        verify(gateway).fetchSigungu();
        verify(importer).importGeoJson("{\"type\":\"FeatureCollection\",\"features\":[]}");
    }

    @Test
    void ordinaryServerStartDoesNotCallVworld() {
        VworldBoundaryGateway gateway = mock(VworldBoundaryGateway.class);
        VworldBoundaryImportService importer = mock(VworldBoundaryImportService.class);
        VworldBoundaryImportJob job = new VworldBoundaryImportJob(gateway, importer);

        job.run(new DefaultApplicationArguments());

        verify(gateway, never()).fetchSigungu();
        verify(importer, never()).importGeoJson(org.mockito.ArgumentMatchers.anyString());
    }
}
