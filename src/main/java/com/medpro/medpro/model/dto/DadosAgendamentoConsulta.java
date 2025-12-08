package com.medpro.medpro.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record DadosAgendamentoConsulta(
        @NotNull Long medicoId,
        @NotNull Long pacienteId,
        @NotNull LocalDateTime dataHora,
        @Size(max = 255) String motivoConsulta
) {}
