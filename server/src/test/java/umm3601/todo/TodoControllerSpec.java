package umm3601.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.javalin.http.Context;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import umm3601.Server;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

//import io.javalin.http.BadRequestResponse;
//import io.javalin.http.HttpStatus;
//import io.javalin.http.NotFoundResponse;


/**
 * Tests the logic of the TodoController
 *
 * @throws IOException
 */
// The tests here include a ton of "magic numbers" (numeric constants).
// It wasn't clear to me that giving all of them names would actually
// help things. The fact that it wasn't obvious what to call some
// of them says a lot. Maybe what this ultimately means is that
// these tests can/should be restructured so the constants (there are
// also a lot of "magic strings" that Checkstyle doesn't actually
// flag as a problem) make more sense.
@SuppressWarnings({ "MagicNumber" })
public class TodoControllerSpec {

  private Context ctx = mock(Context.class);

  private TodoController todoController;
  private static TodoDatabase db;

  @BeforeEach
  public void setUp() throws IOException {
    db = new TodoDatabase(Server.TODO_DATA_FILE);
    todoController = new TodoController(db);
  }

  @Test
  public void canGetSize() throws IOException {
    // I don't understand why this is necessary in the slightest to get the
    // project to build but whatever... i'll work on this more tomorrow
    db.size();
  }

  /**
   * Confirms that we can get all the todos.
   *
   * @throws IOException
   */
  @Test
  public void canGetAllTodos() throws IOException {
    // Call the method on the mock context, which doesn't
    // include any filters, so we should get all the users
    // back.
    todoController.getTodos(ctx);

    // Confirm that `json` was called with all the users.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    assertEquals(db.size(), argument.getValue().length);
  }

  /**
   * Confirms that we can filter todos by owner.
   *
   * @throws IOException
   */
  @Test
  public void canGetTodosWithOwner() throws IOException {
    // Add a query param map to the context that maps "owner"
    // to "Workman".
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Workman"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the method on the mock controller with the added
    // query param map to limit the result to just todos with
    // owner Workman.
    todoController.getTodos(ctx);

    // Confirm that all the users passed to `json` have owner Workman.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertEquals("Workman", todo.owner);
    }
    // Confirm that there are 49 todos with owner "Workman" (thank you, Ctrl+F)
    assertEquals(49, argument.getValue().length);
  }

  /**
   * Confirms that we can filter todos by category.
   *
   * @throws IOException
   */
  @Test
  public void canGetTodosWithCategory() throws IOException {
    // Add a query param map to the context that maps "owner"
    // to "Workman".
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("category", Arrays.asList(new String[] {"video games"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the method on the mock controller with the added
    // query param map to limit the result to just todos with
    // category "video games".
    todoController.getTodos(ctx);

    // Confirm that all the users passed to `json` have category "video games".
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertEquals("video games", todo.category);
    }
    // Confirm that there are 71 todos with category "video games"
    assertEquals(71, argument.getValue().length);
  }

  @Test
  public void canGetTodosWithOwnerAndCategory() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Dawn"}));
    queryParams.put("category", Arrays.asList(new String[] {"software design"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` are owned by Dawn
    // and have category "video games".
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertEquals("Dawn", todo.owner);
      assertEquals("software design", todo.category);
    }
    assertEquals(10, argument.getValue().length);
  }

}
