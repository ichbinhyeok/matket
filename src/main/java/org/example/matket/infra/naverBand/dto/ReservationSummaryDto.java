package org.example.matket.infra.naverBand.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ReservationSummaryDto {

    private final LocalDate date;
    private final Map<String, Integer> productQuantities;
}