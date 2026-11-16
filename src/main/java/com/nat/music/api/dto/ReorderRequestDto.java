package com.nat.music.api.dto;

import jakarta.validation.constraints.Min;

public record ReorderRequestDto(

        @Min(value = 0, message = "fromIndex must be greater than or equal to 0")
        int fromIndex,

        @Min(value = 0, message = "toIndex must be greater than or equal to 0")
        int toIndex
) {}
