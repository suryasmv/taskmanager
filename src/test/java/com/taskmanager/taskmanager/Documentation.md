| Concept / Item                                  | Definition / Usage                                                                                                  |
|-------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| Stub (JUnit/Mockito)                            | A simple fake object with pre-programmed responses; returns fixed data when methods are called, no behavior checks. |
| Why stubs are difficult                         | Hard to maintain when behavior grows, require manual wiring for every method, and don’t verify interactions.       |
| Mock (JUnit/Mockito)                            | A dynamic test double that both stubs return values and verifies calls (methods called, args, times).              |
| How mocks use stubs                             | A mock behaves as a stub when you define `when(...).thenReturn(...)`, and as a spy/verifier with `verify(...)`.    |
| @ExtendWith(MockitoExtension.class)             | JUnit 5 annotation to enable Mockito support (creates mocks, handles @Mock/@InjectMocks lifecycle).                |
| @Mock                                           | Tells Mockito to create a mock instance of a dependency (e.g., `TaskRepository`).                                  |
| @InjectMocks                                    | Creates the real class under test and injects any matching @Mock fields into its constructor/fields.               |
| @BeforeEach                                     | JUnit method run before each test; often used to set up service instances or shared data.                          |
| @Test                                           | Marks a method as an individual test case to be run by JUnit.                                                      |
| when(...).thenReturn(...)                       | Mockito stubbing: define what a mock should return when a specific method is called.                               |
| when(...).thenThrow(...)                        | Mockito stubbing that makes a mock method throw an exception when invoked.                                         |
| thenAnswer(invocation -> ...)                   | Advanced stubbing: compute dynamic return based on invocation arguments.                                           |
| verify(mock).someMethod(...)                    | Mockito verification: assert that a method was called with specific arguments.                                     |
| verify(mock, times(n)).someMethod(...)          | Verification that a method on the mock was called exactly `n` times.                                               |
| ArgumentCaptor<T>                               | Captures arguments passed into a mock method so you can assert on their values/order.                              |
| assertEquals / assertFalse / assertTrue         | JUnit assertions to compare expected vs actual values and boolean conditions.                                      |
| assertThrows / assertThatThrownBy               | Assert that a given lambda or method call throws a specific exception type.                                        |
| assertThat(...).hasSize(...).allMatch(...)      | Assert fluent assertions used for collections and predicates in tests.                                             |
