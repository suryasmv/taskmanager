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

## Annotations Reference

| Annotation | Usage |
|------------|-------|
| `@Entity` | Marks class as JPA entity |
| `@Table(name = "tasks")` | Maps to `tasks` database table |
| `@Id` | Primary key field |
| `@GeneratedValue(strategy = GenerationType.IDENTITY)` | Auto-increment ID |
| `@Getter` | Generates getters for all fields |
| `@Setter` | Generates setters for all fields |
| `@NoArgsConstructor` | No-arg constructor (JPA required) |
| `@AllArgsConstructor` | Full constructor with all fields |
| `@Builder` | Fluent builder pattern |
| `@CreationTimestamp` | Auto-set `createdAt` on INSERT |
| `@UpdateTimestamp` | Auto-update `updatedAt` on UPDATE |


