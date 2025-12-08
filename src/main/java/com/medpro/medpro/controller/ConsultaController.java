package com.medpro.medpro.controller;

import com.medpro.medpro.model.dto.DadosAgendamentoConsulta;
import com.medpro.medpro.model.dto.DadosCancelamentoConsulta;
import com.medpro.medpro.model.dto.DadosDetalhamentoConsulta;
import com.medpro.medpro.model.dto.DadosListagemConsulta;
import com.medpro.medpro.model.entity.Consulta;
import com.medpro.medpro.repository.ConsultaRepository;
import com.medpro.medpro.repository.MedicoRepository;
import com.medpro.medpro.repository.PacienteRepository;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaRepository consultaRepo;
    private final MedicoRepository medicoRepo;
    private final PacienteRepository pacienteRepo;

    private static final LocalTime HORA_INICIO = LocalTime.of(7, 0);
    private static final LocalTime HORA_FIM = LocalTime.of(18, 0);
    private static final Duration ANTECEDENCIA_MINIMA = Duration.ofHours(24);
    private static final Duration ANTECEDENCIA_CANCELAMENTO = Duration.ofHours(24);

    public ConsultaController(
            ConsultaRepository consultaRepo,
            MedicoRepository medicoRepo,
            PacienteRepository pacienteRepo
    ) {
        this.consultaRepo = consultaRepo;
        this.medicoRepo = medicoRepo;
        this.pacienteRepo = pacienteRepo;
    }

    @PostMapping
    public ResponseEntity<?> agendar(
            @RequestBody @Valid DadosAgendamentoConsulta dados,
            UriComponentsBuilder uriBuilder) {

        var medicoOpt = medicoRepo.findById(dados.medicoId());
        if (medicoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Médico não encontrado.");
        }

        var pacienteOpt = pacienteRepo.findById(dados.pacienteId());
        if (pacienteOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Paciente não encontrado.");
        }

        var medico = medicoOpt.get();
        var paciente = pacienteOpt.get();

        LocalDateTime dataHora = dados.dataHora();
        LocalDateTime agora = LocalDateTime.now();

        if (Duration.between(agora, dataHora).compareTo(ANTECEDENCIA_MINIMA) < 0) {
            return ResponseEntity.badRequest()
                    .body("Agendamento deve ocorrer com pelo menos 24 horas de antecedência.");
        }

        LocalTime hora = dataHora.toLocalTime();
        if (hora.isBefore(HORA_INICIO) || hora.isAfter(HORA_FIM)) {
            return ResponseEntity.badRequest()
                    .body("Horário fora do expediente (07:00 até 18:00).");
        }

        if (consultaRepo.existsByMedicoIdAndDataHora(medico.getId(), dataHora)) {
            return ResponseEntity.badRequest()
                    .body("O médico já possui consulta neste horário.");
        }

        if (consultaRepo.existsByPacienteIdAndDataHora(paciente.getId(), dataHora)) {
            return ResponseEntity.badRequest()
                    .body("O paciente já possui consulta neste horário.");
        }

        var consulta = new Consulta();
        consulta.setMedico(medico);
        consulta.setPaciente(paciente);
        consulta.setDataHora(dataHora);
        consulta.setMotivoConsulta(dados.motivoConsulta());
        consulta.setSituacao("AGENDADA");

        System.out.println(consulta);
        consultaRepo.save(consulta);

        var dto = new DadosDetalhamentoConsulta(consulta);

        var uri = uriBuilder
                .path("/consultas/{id}")
                .buildAndExpand(consulta.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelar(
            @PathVariable Long id,
            @RequestBody(required = false) DadosCancelamentoConsulta body) {

        var consultaOpt = consultaRepo.findById(id);
        if (consultaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var consulta = consultaOpt.get();

        if (!consulta.getSituacao().equals("AGENDADA")) {
            return ResponseEntity.badRequest()
                    .body("Apenas consultas AGENDADAS podem ser canceladas.");
        }

        LocalDateTime agora = LocalDateTime.now();

        if (Duration.between(agora, consulta.getDataHora())
                .compareTo(ANTECEDENCIA_CANCELAMENTO) < 0) {
            return ResponseEntity.badRequest()
                    .body("Cancelamento deve ocorrer com pelo menos 24 horas de antecedência.");
        }

        if (body != null && body.motivoCancelamento() != null && !body.motivoCancelamento().isBlank()) {
            consulta.setMotivoCancelamento(body.motivoCancelamento());
        }

        consulta.cancelar(body != null ? body.motivoCancelamento() : null);
        consultaRepo.save(consulta);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemConsulta>> listar(
            @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {

        var page = consultaRepo
                .findAll(pageable)
                .map(c -> new DadosListagemConsulta(
                        c.getId(),
                        c.getMedico().getId(),
                        c.getMedico().getNome(),
                        c.getPaciente().getId(),
                        c.getPaciente().getNome(),
                        c.getDataHora(),
                        c.getSituacao()
                ));

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoConsulta> buscar(@PathVariable Long id) {
        var consultaOpt = consultaRepo.findById(id);
        return consultaOpt
                .map(DadosDetalhamentoConsulta::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
