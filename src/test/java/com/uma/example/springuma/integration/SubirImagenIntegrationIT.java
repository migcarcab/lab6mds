package com.uma.example.springuma.integration;

import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class SubirImagenIntegrationIT extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSubirImagenPacienteCorrectamente() throws Exception {
        // Crear médico necesario para el paciente
        Medico medico = new Medico();
        medico.setDni("87654321B");
        medico.setNombre("Dr. Imagen");
        medico.setEspecialidad("Radiología");
        mockMvc.perform(post("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated());

        // Obtener el médico creado
        MvcResult medicoResult = mockMvc.perform(get("/medico/dni/87654321B"))
                .andExpect(status().isOk())
                .andReturn();
        Medico medicoCreado = objectMapper.readValue(medicoResult.getResponse().getContentAsString(), Medico.class);

        // Crear paciente
        Paciente paciente = new Paciente();
        paciente.setNombre("Paciente Imagen");
        paciente.setEdad(30);
        paciente.setCita("2025-05-19");
        paciente.setDni("99999999Z");
        paciente.setMedico(medicoCreado);
        mockMvc.perform(post("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated());

        // Obtener el paciente creado
        MvcResult pacienteResult = mockMvc.perform(get("/paciente/medico/" + medicoCreado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre", is("Paciente Imagen")))
                .andReturn();
        Paciente pacienteCreado = objectMapper.readValue(
                objectMapper.writeValueAsString(
                        objectMapper.readValue(pacienteResult.getResponse().getContentAsString(), Paciente[].class)[0]),
                Paciente.class);

        // Leer imagen de recursos
        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/healthy.png"));
        MockMultipartFile imageFile = new MockMultipartFile("image", "healthy.png", "image/png", imageBytes);
        MockMultipartFile pacienteFile = new MockMultipartFile("paciente", "", "application/json",
                objectMapper.writeValueAsBytes(pacienteCreado));

        // Subir imagen
        mockMvc.perform(multipart("/imagen")
                .file(imageFile)
                .file(pacienteFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("file uploaded successfully")));

        // Comprobar que la imagen está asociada al paciente
        mockMvc.perform(get("/imagen/paciente/" + pacienteCreado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre", is("healthy.png")))
                .andReturn();
    }
}
