package com.uma.example.springuma.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PacienteService {

    @Autowired
    RepositoryPaciente repositoryPaciente; // Cambio el nombre y tipo del repositorio

    public List<Paciente> getAllPacientes(){
        return repositoryPaciente.findAll(); // Cambio el método para devolver una lista de Pacientes
    }

    public Paciente getPaciente(Long id){
        return repositoryPaciente.getReferenceById(id); // Cambio el tipo de retorno a Paciente
    }

    public Paciente addPaciente(Paciente p){
        return repositoryPaciente.saveAndFlush(p); // Cambio el tipo de argumento y retorno a Paciente
    }

    public void updatePaciente(Paciente p){
        /*Paciente paciente = repositoryPaciente.getReferenceById(p.getId());
        // Aquí podrías actualizar los campos específicos del paciente
        paciente.setNombre(p.getNombre());
        paciente.setEdad(p.getEdad());
        paciente.setCita(p.getCita());
        paciente.setDni(p.getDni());
        paciente.setMedico(p.getMedico());*/
        repositoryPaciente.save(p); // Guardamos el paciente actualizado
    }

    public void removePaciente(Paciente p){
        repositoryPaciente.delete(p); // Cambio a eliminar un Paciente
    }

    public void removePacienteID(Long id){
        repositoryPaciente.deleteById(id); // Cambio a eliminar un Paciente por ID
    }

    public List<Paciente> getPacientesMedico(Long id) {
        return repositoryPaciente.findByMedicoId(id);
    }
}
