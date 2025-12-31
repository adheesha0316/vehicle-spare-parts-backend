package com.spareparts.spareparts_backend.enums;

import lombok.Getter;

@Getter
public enum SpareItemCategory {
    ENGINE_PARTS("Engine Parts", "එන්ජින් කොටස්"),
    BRAKING_SYSTEM("Braking System", "බ්රේක් පද්ධතිය"),
    SUSPENSION_STEERING("Suspension & Steering", "ෂොක් / රැක් කොටස්"),
    ELECTRICAL_LIGHTING("Electrical & Lighting", "විදුලි සහ ලාම්පු"),
    FILTERS_MAINTENANCE("Filters & Maintenance", "ෆිල්ටර් සහ නඩත්තු"),
    EXTERIOR_BODY("Exterior & Body Parts", "බාහිර කොටස්");

    private final String labelEn;
    private final String labelSi;

    SpareItemCategory(String labelEn, String labelSi) {
        this.labelEn = labelEn;
        this.labelSi = labelSi;
    }

}
