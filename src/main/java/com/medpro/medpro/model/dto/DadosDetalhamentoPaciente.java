package com.medpro.medpro.model.dto;

import com.medpro.medpro.model.entity.Paciente;

public record DadosDetalhamentoPaciente(
        Long id,
        String nome,
        String email,
        String cpf,
        String telefone,
        DadosEndereco endereco
) {
    public DadosDetalhamentoPaciente(Paciente paciente) {
        this(
                paciente.getId(),
                paciente.getNome(),
                paciente.getEmail(),
                paciente.getCpf(),
                paciente.getTelefone(),
                new DadosEndereco(paciente.getEndereco())
        );
    }
}
