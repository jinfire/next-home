package com.nexthome.backend.region;

import java.util.List;

public record RegionOptionGroup(Long id, String code, String name, List<RegionOption> regions) {
}
