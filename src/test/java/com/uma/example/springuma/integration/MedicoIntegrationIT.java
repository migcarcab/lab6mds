package com.uma.example.springuma.integration;

import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
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
public class MedicoIntegrationIT extends AbstractIntegration {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void testCrearObtenerActualizarEliminarMedico() throws Exception {
                // Crear médico
                Medico medico = new Medico();
                medico.setDni("12345678A");
                medico.setNombre("Dr. Prueba");
                medico.setEspecialidad("Cardiología");

                // Crear
                mockMvc.perform(post("/medico")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(medico)))
                                .andExpect(status().isCreated())
                                .andReturn();

                // Obtener por DNI
                // Obtener por DNI
                MvcResult getResult = mockMvc.perform(get("/medico/dni/12345678A"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nombre", is("Dr. Prueba")))
                                .andExpect(jsonPath("$.especialidad", is("Cardiología")))
                                .andReturn();

                Medico medicoCreado = objectMapper.readValue(getResult.getResponse().getContentAsString(),
                                Medico.class);
                Long id = medicoCreado.getId();

                // Actualizar
                medicoCreado.setNombre("Dr. Modificado");
                mockMvc.perform(put("/medico")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(medicoCreado)))
                                .andExpect(status().isNoContent());

                // Verificar actualización
                mockMvc.perform(get("/medico/" + id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nombre", is("Dr. Modificado")));

                // Eliminar
                mockMvc.perform(delete("/medico/" + id))
                                .andExpect(status().isOk());

                // Verificar eliminación
                mockMvc.perform(get("/medico/" + id))
                                .andExpect(status().isInternalServerError());
        }



}
