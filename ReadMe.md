# Task Management System Documentation

## Spring Boot Application Package Structure

The following table outlines the main packages inside the `com.taskmanager.taskmanager` root package along with their purposes, key Spring annotations used, and example classes.

| Package Name   | Purpose         | Key Annotations                | Example Class                            |
|----------------|-----------------|-------------------------------|------------------------------------------|
| controller     | HTTP endpoints  | `@RestController`, `@RequestMapping` | `TaskController.java`                    |
| service        | Business logic  | `@Service`                    | `TaskService.java`                       |
| repository     | Database access | `@Repository`, `@Query`       | `TaskRepository.java`     |
| model/entity   | Data models     | `@Entity`, `@Table`           | `Task.java`                    |
| dto            | API payloads    | Plain POJOs                   | `TaskDto.java`           |
| config         | Spring configs  | `@Configuration`              | `SecurityConfig.java`    |
| exception      | Error handling  | `@ControllerAdvice`           | `GlobalExceptionHandler.java`  |
| security       | Auth configs    | `@EnableWebSecurity`          | `SecurityConfig.java`            |
| util           | Helpers         | Plain classes                 | `DateUtil.java`            |

## Priority System

**Key Behavior:**
- **Max 3 prioritized tasks per scope** (priorities: 1, 2, 3)
- Tasks 4+ get `priority = null` (deprioritized)
- **Separate priority scopes** - Global vs Project-specific
- Global Tasks Priority (projectId IS NULL):
- Project Tasks Priority (same ProjectId)

## Annotations Reference

| Annotation                           | Usage                                           |
|------------------------------------|------------------------------------------------|
| `@Entity`                          | Marks class as a JPA entity                     |
| `@Table(name = "tasks")`           | Maps the entity class to the `tasks` database table |
| `@Id`                             | Marks the primary key field                      |
| `@GeneratedValue(strategy = GenerationType.IDENTITY)` | Specifies that the primary key is auto-incremented by the database |
| `@Getter`                         | Lombok annotation that generates getters for all fields |
| `@Setter`                         | Lombok annotation that generates setters for all fields |
| `@NoArgsConstructor`              | Lombok annotation to generate a no-argument constructor, required by JPA |
| `@AllArgsConstructor`             | Lombok annotation to generate a constructor with all fields |
| `@Builder`                       | Lombok annotation to provide fluent builder pattern support |
| `@CreationTimestamp`             | Hibernate annotation that automatically sets the field value to the current timestamp on entity insert |
| `@UpdateTimestamp`               | Hibernate annotation that automatically updates the field value to the current timestamp on entity update |
| `@RestController`               | Spring annotation to mark the class as a RESTful controller |
| `@RequiredArgsConstructor`       | Lombok annotation to generate constructor with required final fields |
| `@CrossOrigin(origins = "...")` | Enables Cross-Origin Resource Sharing (CORS) for the specified origins in REST controller |
| `@RequestMapping("/tasks")`       | Maps HTTP requests to /tasks URI for the controller |
| `@PostMapping`, `@GetMapping`, `@PutMapping`, `@DeleteMapping` | Spring annotations to map HTTP POST, GET, PUT, DELETE requests respectively on methods |
| `@PathVariable`                  | Binds method parameter to URI template variable |
| `@RequestBody`                   | Binds HTTP request body to method parameter in controller |
| `@Repository`                   | Marks the class as a Spring Data repository component |
| `@Transactional`                | Defines transactional boundaries for methods, managing commit/rollback behavior |


