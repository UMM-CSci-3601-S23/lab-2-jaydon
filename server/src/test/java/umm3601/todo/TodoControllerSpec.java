package umm3601.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;


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

  @Test
  public void canGetTodosWithSpecifiedId() throws IOException {
    String id = "58895985140cca06def60d82";
    Todo todo = db.getTodo(id);

    when(ctx.pathParam("id")).thenReturn(id);

    todoController.getTodo(ctx);

    verify(ctx).json(todo);
    verify(ctx).status(HttpStatus.OK);
    assertEquals("Fry", todo.owner);
    assertEquals("video games", todo.category);
    assertEquals(false, todo.status);

  }

  @Test
  public void respondsAppropriatelyToRequestForNonexistentId() throws IOException {
    when(ctx.pathParam("id")).thenReturn(null);
    Throwable exception = Assertions.assertThrows(NotFoundResponse.class, () -> {
      todoController.getTodo(ctx);
    });
    assertEquals("No todo with id " + null + " was found.", exception.getMessage());
  }

  @Test
  public void canGetTodosWithStatusComplete() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"complete"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the method on the mock controller with the added
    // query param map to limit the result to just todos with
    // a complete status
    todoController.getTodos(ctx);

    // Confirm that all the users passed to `json` have status "complete"
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertEquals(true, todo.status);
    }
    // Confirm that there are 143 todos with complete status
    assertEquals(143, argument.getValue().length);
  }

  @Test
  public void canGetTodosWithStatusIncomplete() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    // Start checking for incomplete status instead
    queryParams.put("status", Arrays.asList(new String[] {"incomplete"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertEquals(false, todo.status);
    }
    assertEquals(157, argument.getValue().length);
  }

  @Test
  public void respondsAppropriatelyToIllegalStatus() {
    // We'll set the requested "status" to be a String that can't be understood as a boolean
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"abc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // This should now throw a `BadRequestResponse` exception because
    // our request has a status that doesn't correspond to a boolean
    Throwable exception = Assertions.assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodos(ctx);
    });
    assertEquals("Specified status '" + "abc" + "' can't be interpreted as a boolean", exception.getMessage());
  }

  @Test
  public void canGetTodosWithStringInBody() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    // Start checking for incomplete status instead
    queryParams.put("contains", Arrays.asList(new String[] {"reprehenderit"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertTrue(todo.body.toLowerCase().contains("reprehenderit"));
    }
    assertEquals(96 - 12, argument.getValue().length);
    // ctrl+f reprehenderit -12 duplicates
  }

  @Test
  public void canLimitTodos() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    // Gather an arbitrary combination of parameters after setting the item limit to 7
    queryParams.put("limit", Arrays.asList(new String[] {"7"}));
    queryParams.put("category", Arrays.asList(new String[] {"homework"}));
    queryParams.put("status", Arrays.asList(new String[] {"incomplete"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());

    assertEquals(7, argument.getValue().length);
    // Make sure the order of items hasn't changed either. Check the id of first and last returned items
    assertEquals("5889598585bda42fb8388ba1", argument.getValue()[0]._id);
    assertEquals("58895985ea08e3fe6f31e42e", argument.getValue()[argument.getValue().length - 1]._id);
  }
  /* The limit parameter could be tested more thoroughly by ensuring that no limit is actually applied when
   * we request a limit larger than the total number of todos returned after filtering the list.
   * However, for the purposes of this lab I am willing to believe stream().limit() functions properly to
   * control this kind of situation on its own.
   */

  @Test
  public void respondsAppropriatelyToIllegalLimit() {
    Map<String, List<String>> queryParams = new HashMap<>();
    // Assign arbitrary value to limit that does not correspond to an integer
    queryParams.put("limit", Arrays.asList(new String[] {"abc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // This should now throw a `BadRequestResponse` exception because
    // our request has a limit that doesn't correspond to an integer
    Throwable exception = Assertions.assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodos(ctx);
    });
    assertEquals("Specified limit '" + "abc"
    + "' can't be parsed to an integer", exception.getMessage());
  }

  @Test
  public void canOrderTodosByOwner() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"owner"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());

    assertEquals(db.size(), argument.getValue().length);
    // Make sure the first name in the alphabet appears first, and same idea with the last.
    assertEquals("Barry", argument.getValue()[0].owner);
    assertEquals("Workman", argument.getValue()[argument.getValue().length - 1].owner);
  }

  @Test
  public void canOrderTodosByBody() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"body"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());

    assertEquals(db.size(), argument.getValue().length);
    // Check that the list items are sorted by body
    assertTrue(argument.getValue()[0].body.startsWith("Ad "));
    assertTrue(argument.getValue()[argument.getValue().length - 1].body.startsWith("Volu"));
  }

  @Test
  public void canOrderTodosByStatus() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"status"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());

    assertEquals(db.size(), argument.getValue().length);
    // You get the gist by now
    assertEquals(false, argument.getValue()[0].status);
    assertEquals(true, argument.getValue()[argument.getValue().length - 1].status);
  }

  @Test
  public void canOrderTodosByCategory() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"category"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());

    assertEquals(db.size(), argument.getValue().length);

    assertEquals("groceries", argument.getValue()[0].category);
    assertEquals("video games", argument.getValue()[argument.getValue().length - 1].category);
  }

  @Test
  public void respondsAppropriatelyToIllegalOrderArgument() {
    Map<String, List<String>> queryParams = new HashMap<>();
    // Assign arbitrary value to limit that does not correspond to an integer
    queryParams.put("orderBy", Arrays.asList(new String[] {"abc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // This should now throw a `BadRequestResponse` exception because
    // our request has a limit that doesn't correspond to an integer
    Throwable exception = Assertions.assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodos(ctx);
    });
    assertEquals("Specified value to order by '" + "abc" + "' is an invalid value", exception.getMessage());
  }

}
