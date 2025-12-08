package com.medpro.medpro.repository;

import com.medpro.medpro.model.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Verificar se médico tem consulta no mesmo horário
    boolean existsByMedicoIdAndDataHora(Long medicoId, LocalDateTime dataHora);

    // Verificar se paciente tem consulta no mesmo horário
    boolean existsByPacienteIdAndDataHora(Long pacienteId, LocalDateTime dataHora);

    List<Consulta> findAllByMedicoIdAndDataHoraBetween(Long medicoId, LocalDateTime start, LocalDateTime end);

    Optional<Consulta> findByIdAndSituacao(Long id, String situacao);
}
