package com.medpro.medpro.model.dto;

import jakarta.validation.Valid; // <--- Não esqueça de importar isso!
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroPaciente(
        @NotBlank String nome,
        @NotBlank String email,
        @NotBlank String cpf,
        @NotBlank String telefone,
        
        @NotNull
        @Valid // <--- ADICIONE ESTA ANOTAÇÃO
        DadosEndereco endereco
) {}