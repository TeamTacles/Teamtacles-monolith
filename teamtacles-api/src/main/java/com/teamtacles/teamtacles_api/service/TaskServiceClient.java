package com.teamtacles.teamtacles_api.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.teamtacles.teamtacles_api.exception.ServiceUnavailableException;

@Service
public class TaskServiceClient {
    private final RestTemplate restTemplate;

    public TaskServiceClient(@Qualifier("taskServiceRestTemplate") RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    /**
     * Sends a request to the task management service to delete all tasks
     * associated with a specific project ID.
     * This is a bulk operation and requires appropriate permissions on the microservice side.
     *
     * @param projectId The ID of the project whose tasks are to be deleted.
     * @param token The JWT token for authorization.
     * @throws RuntimeException if the task service is unavailable or encounters an internal error.
     * @throws AccessDeniedException if the user does not have permission to perform the action.
     */
    public void deleteAllTasksFromProject(Long projectId, String token) {
        String url = "/api/project/" + projectId + "/tasks";
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.NotFound ex) {
            throw new AccessDeniedException("Permission denied to delete tasks for project " + projectId, ex);
        } catch (HttpServerErrorException.ServiceUnavailable ex) {
            throw new ServiceUnavailableException("The task service is temporarily unavailable.");
        } catch (HttpServerErrorException ex) {
            throw new RuntimeException("The task service failed to complete the deletion request.", ex);
        } catch (RestClientException ex) {
            throw new RuntimeException("Network communication error with the task service.", ex);
        }
    }
}
