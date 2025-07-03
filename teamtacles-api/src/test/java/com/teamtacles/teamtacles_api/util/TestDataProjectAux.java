package com.teamtacles.teamtacles_api.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.teamtacles.teamtacles_api.model.Project;
import com.teamtacles.teamtacles_api.model.User;
import com.teamtacles.teamtacles_api.repository.ProjectRepository;

@Component
public class TestDataProjectAux {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestDataAux testDataAux;

    private Project project;

    public void setUpTestProject() {
        projectRepository.deleteAll();

        Project project = new Project();
        project.setTitle("API Project");
        project.setDescription("Team task management API");
        project.setTeam(List.of(testDataAux.getNormalUser()));
        project.setCreator(testDataAux.getNormalUser());
        this.project = projectRepository.save(project);
    }

    public Project getProject(){
        if(project == null){
            throw new IllegalStateException("Project not set up yet. Call createTestProject() first");
        }

        return project;
    }

}
