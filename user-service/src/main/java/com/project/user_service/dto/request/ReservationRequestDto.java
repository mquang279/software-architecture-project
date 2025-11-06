package com.project.user_service.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReservationRequestDto {

    private long showId;
    private List<Long> seatIdsToReserve;
    private double amount;

}
