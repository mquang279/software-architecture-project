package com.project.movie_reservation_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowDto {
    
    private long id;
    private Long movieId;
    private Long theaterId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Long> seatIds;
    private String movieTitle;    
    private String theaterName;   
    private String theaterLocation; 
}