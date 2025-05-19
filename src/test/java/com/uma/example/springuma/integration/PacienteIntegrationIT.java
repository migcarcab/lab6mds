package com.uma.example.springuma.integration;

import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
public class PacienteIntegrationIT extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAsociarYEditarPacienteAMedico() throws Exception {
        // Crear médico
        Medico medico = new Medico();
        medico.setDni("87654321B");
        medico.setNombre("Dr. Asociador");
        medico.setEspecialidad("Neurología");
        mockMvc.perform(post("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated());

        // Obtener médico creado
        MvcResult medicoResult = mockMvc.perform(get("/medico/dni/87654321B"))
                .andExpect(status().isOk())
                .andReturn();
        Medico medicoCreado = objectMapper.readValue(medicoResult.getResponse().getContentAsString(), Medico.class);
        Long medicoId = medicoCreado.getId();

        // Crear paciente asociado al médico
        Paciente paciente = new Paciente();
        paciente.setNombre("Paciente Uno");
        paciente.setEdad(30);
        paciente.setCita("2025-05-19");
        paciente.setDni("99999999Z");
        paciente.setMedico(medicoCreado);
        mockMvc.perform(post("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated());

        // Obtener paciente por médico
        MvcResult pacientesResult = mockMvc.perform(get("/paciente/medico/" + medicoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre", is("Paciente Uno")))
                .andReturn();
        Paciente[] pacientes = objectMapper.readValue(pacientesResult.getResponse().getContentAsString(), Paciente[].class);
        Long pacienteId = pacientes[0].getId();

        // Editar paciente
        Paciente pacienteEditado = pacientes[0];
        pacienteEditado.setNombre("Paciente Editado");
        pacienteEditado.setEdad(35);
        mockMvc.perform(put("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pacienteEditado)))
                .andExpect(status().isNoContent());

        // Verificar edición
        mockMvc.perform(get("/paciente/" + pacienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Paciente Editado")))
                .andExpect(jsonPath("$.edad", is(35)));

        // Limpiar: eliminar paciente y médico
        mockMvc.perform(delete("/paciente/" + pacienteId))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/medico/" + medicoId))
                .andExpect(status().isOk());
    }

    @Test
    void testCambiarMedicoDePaciente() throws Exception {
        // Crear primer médico
        Medico medico1 = new Medico();
        medico1.setDni("11111111A");
        medico1.setNombre("Dr. Uno");
        medico1.setEspecialidad("Oncología");
        mockMvc.perform(post("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(medico1)))
                .andExpect(status().isCreated());

        // Crear segundo médico
        Medico medico2 = new Medico();
        medico2.setDni("22222222B");
        medico2.setNombre("Dr. Dos");
        medico2.setEspecialidad("Pediatría");
        mockMvc.perform(post("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(medico2)))
                .andExpect(status().isCreated());

        // Obtener médicos creados
        MvcResult medicoResult1 = mockMvc.perform(get("/medico/dni/11111111A"))
                .andExpect(status().isOk())
                .andReturn();
        Medico medicoCreado1 = objectMapper.readValue(medicoResult1.getResponse().getContentAsString(), Medico.class);
        Long medicoId1 = medicoCreado1.getId();

        MvcResult medicoResult2 = mockMvc.perform(get("/medico/dni/22222222B"))
                .andExpect(status().isOk())
                .andReturn();
        Medico medicoCreado2 = objectMapper.readValue(medicoResult2.getResponse().getContentAsString(), Medico.class);
        Long medicoId2 = medicoCreado2.getId();

        // Crear paciente asociado al primer médico
        Paciente paciente = new Paciente();
        paciente.setNombre("Paciente Cambio");
        paciente.setEdad(40);
        paciente.setCita("2025-06-01");
        paciente.setDni("88888888Z");
        paciente.setMedico(medicoCreado1);
        mockMvc.perform(post("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated());

        // Obtener paciente por primer médico
        MvcResult pacientesResult = mockMvc.perform(get("/paciente/medico/" + medicoId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre", is("Paciente Cambio")))
                .andReturn();
        Paciente[] pacientes = objectMapper.readValue(pacientesResult.getResponse().getContentAsString(), Paciente[].class);
        Long pacienteId = pacientes[0].getId();

        // Cambiar el médico del paciente al segundo médico
        Paciente pacienteEditado = pacientes[0];
        pacienteEditado.setMedico(medicoCreado2);
        mockMvc.perform(put("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pacienteEditado)))
                .andExpect(status().isNoContent());

        // Verificar que el paciente está asociado al segundo médico
        MvcResult pacientesResult2 = mockMvc.perform(get("/paciente/medico/" + medicoId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre", is("Paciente Cambio")))
                .andReturn();
        Paciente[] pacientesMedico2 = objectMapper.readValue(pacientesResult2.getResponse().getContentAsString(), Paciente[].class);
        // El paciente debe estar en la lista del segundo médico
        assert pacientesMedico2.length > 0;
        assert pacientesMedico2[0].getId() == pacienteId;

        // Limpiar: eliminar paciente y médicos
        mockMvc.perform(delete("/paciente/" + pacienteId))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/medico/" + medicoId1))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/medico/" + medicoId2))
                .andExpect(status().isOk());
    }
}
