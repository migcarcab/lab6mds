package com.uma.example.springuma.integration;

import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;
import com.uma.example.springuma.model.Informe;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.uma.example.springuma.model.Imagen;

@AutoConfigureMockMvc
public class InformesIntegrationIT extends AbstractIntegration {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCrearYEliminarInformeImagen() throws Exception {
        // Crear médico
        Medico medico = new Medico();
        medico.setDni("55555555X");
        medico.setNombre("Dr. Informe");
        medico.setEspecialidad("Diagnóstico");
        mockMvc.perform(post("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated());

        // Obtener el médico creado
        MvcResult medicoResult = mockMvc.perform(get("/medico/dni/55555555X"))
                .andExpect(status().isOk())
                .andReturn();
        Medico medicoCreado = objectMapper.readValue(medicoResult.getResponse().getContentAsString(), Medico.class);

        // Crear paciente
        Paciente paciente = new Paciente();
        paciente.setNombre("Paciente Informe");
        paciente.setEdad(45);
        paciente.setCita("2025-05-19");
        paciente.setDni("77777777W");
        paciente.setMedico(medicoCreado);
        mockMvc.perform(post("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated());

        // Obtener el paciente creado
        MvcResult pacienteResult = mockMvc.perform(get("/paciente/medico/" + medicoCreado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre", is("Paciente Informe")))
                .andReturn();
        Paciente pacienteCreado = objectMapper.readValue(
                objectMapper.writeValueAsString(
                        objectMapper.readValue(pacienteResult.getResponse().getContentAsString(), Paciente[].class)[0]),
                Paciente.class);

        // Subir imagen
        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/healthy.png"));
        MockMultipartFile imageFile = new MockMultipartFile("image", "healthy.png", "image/png", imageBytes);
        MockMultipartFile pacienteFile = new MockMultipartFile("paciente", "", "application/json",
                objectMapper.writeValueAsBytes(pacienteCreado));
        mockMvc.perform(multipart("/imagen")
                .file(imageFile)
                .file(pacienteFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("file uploaded successfully")));

        // Obtener la imagen subida
        MvcResult imagenesResult = mockMvc.perform(get("/imagen/paciente/" + pacienteCreado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre", is("healthy.png")))
                .andReturn();
        JsonNode imagenesJson = objectMapper.readTree(imagenesResult.getResponse().getContentAsString());
        long imagenId = imagenesJson.get(0).get("id").asLong();

        // Crear informe asociado a la imagen
        Informe informe = new Informe();
        informe.setContenido("Informe de prueba");
        // Solo es necesario setear la imagen con el id
        Imagen imagen = new Imagen();
        imagen.setId(imagenId);
        informe.setImagen(imagen);
        mockMvc.perform(post("/informe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(informe)))
                .andExpect(status().isCreated());

        // Comprobar que el informe está asociado a la imagen
        MvcResult informesResult = mockMvc.perform(get("/informe/imagen/" + imagenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contenido", is("Informe de prueba")))
                .andReturn();
        JsonNode informesJson = objectMapper.readTree(informesResult.getResponse().getContentAsString());
        long informeId = informesJson.get(0).get("id").asLong();

        // Eliminar el informe
        mockMvc.perform(delete("/informe/" + informeId))
                .andExpect(status().isNoContent());

        // Comprobar que el informe ya no existe
        mockMvc.perform(get("/informe/" + informeId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
