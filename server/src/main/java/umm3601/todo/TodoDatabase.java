// Adapted from UserDatabase.java

package umm3601.todo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.BadRequestResponse;

/**
 * A fake "database" of todo info
 * <p>
 * Since we don't want to complicate this lab with a real database, we're going
 * to instead just read a bunch of todo data from a specified JSON file, and
 * then provide various database-like methods that allow the `TodoController` to
 * "query" the "database".
 */
public class TodoDatabase {

  private Todo[] allTodos;

  public TodoDatabase(String todoDataFile) throws IOException {
    InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(todoDataFile));
    ObjectMapper objectMapper = new ObjectMapper();
    allTodos = objectMapper.readValue(reader, Todo[].class);
  }

  public int size() {
    return allTodos.length;
  }

  /**
   * Get the single todo specified by the given ID. Return `null` if there is no
   * todo with that ID.
   *
   * @param id the ID of the desired todo
   * @return the todo with the given ID, or null if there is no todo with that ID
   */
  public Todo getTodo(String id) {
    return Arrays.stream(allTodos).filter(x -> x._id.equals(id)).findFirst().orElse(null);
  }

  /**
   * Get an array of all the todos satisfying the queries in the params.
   *
   * @param queryParams map of key-value pairs for the query
   * @return an array of all the users matching the given criteria
   */
  public Todo[] listTodos(Map<String, List<String>> queryParams) {
    Todo[] filteredTodos = allTodos;

    // Filter owner if defined
    if (queryParams.containsKey("owner")) {
      String targetOwner = queryParams.get("owner").get(0);
      filteredTodos = filterTodosByOwner(filteredTodos, targetOwner);
    }
    // Filter category if defined
    if (queryParams.containsKey("category")) {
      String targetCategory = queryParams.get("category").get(0);
      filteredTodos = filterTodosByCategory(filteredTodos, targetCategory);
    }
    // Filter status if defined, changing String parameter to a corresponding boolean
    if (queryParams.containsKey("status")) {
      String statusParam = queryParams.get("status").get(0);
      boolean targetStatus = false;
      if (statusParam.equals("complete")) {
        targetStatus = true;
      } else if (!statusParam.contains("incomplete")) {
        // Throw BadRequestResponse if the requested status does not match a boolean value (complete/incomplete)
        throw new BadRequestResponse("Specified status '" + statusParam + "' can't be interpreted as a boolean");
      }
      filteredTodos = filterTodosByStatus(filteredTodos, targetStatus);
    }
    // Filter body if defined
    if (queryParams.containsKey("contains")) {
      String targetString = queryParams.get("contains").get(0);
      filteredTodos = filterTodosByBody(filteredTodos, targetString);
    }
    // Order by value if defined (IMPORTANT that this happens before limiting)
    /*if (queryParams.containsKey("orderBy")) {
      // code will happen here with issue #8
    }*/
    // Limit results if defined
    if (queryParams.containsKey("limit")) {
      try {
        int limit = Integer.parseInt(queryParams.get("limit").get(0));
        filteredTodos = filterTodosByLimit(filteredTodos, limit);
      } catch (NumberFormatException e) {
        throw new BadRequestResponse("Specified limit '" + queryParams.get("limit").get(0)
        + "' can't be parsed to an integer");
      }

    }

    return filteredTodos;
  }

  /**
   * Get an array of all the todos having the target owner.
   *
   * @param todos     the list of todos to filter by owner
   * @param targetOwner the target owner to look for
   * @return an array of all the todos from the given list that have the target
   *         owner
   */
  public Todo[] filterTodosByOwner(Todo[] todos, String targetOwner) {
    return Arrays.stream(todos).filter(x -> x.owner.equals(targetOwner)).toArray(Todo[]::new);
  }

  /**
   * Get an array of all the todos having the target category.
   *
   * @param todos     the list of todos to filter by category
   * @param targetCategory the target category to look for
   * @return an array of all the todos from the given list that have the target
   *         category
   */
  public Todo[] filterTodosByCategory(Todo[] todos, String targetCategory) {
    return Arrays.stream(todos).filter(x -> x.category.equals(targetCategory)).toArray(Todo[]::new);
  }

  /**
   * Get an array of all the todos having the target status.
   *
   * @param todos     the list of todos to filter by status
   * @param targetStatus the target status to look for
   * @return an array of all the todos from the given list that have the target
   *         status
   *
   */
  public Todo[] filterTodosByStatus(Todo[] todos, boolean targetStatus) {
    return Arrays.stream(todos).filter(x -> x.status == targetStatus).toArray(Todo[]::new);
  }

  /**
   * Get an array of all the todos having the target String in their body.
   *
   * @param todos     the list of todos to filter by body
   * @param targetString the target String to look for
   * @return an array of all the todos from the given list that have the target
   *         String in their body
   *
   */
  public Todo[] filterTodosByBody(Todo[] todos, String targetString) {
    return Arrays.stream(todos).filter(x -> x.body.toLowerCase()
    .contains(targetString.toLowerCase())).toArray(Todo[]::new);
  }

  /**
   * Get an array of all the todos in order within the specified limit.
   *
   * @param todos     the list of todos to filter by body
   * @param targetLimit the maximum amount of todos to be returned
   * @return an array of all the todos from the given list such that the array size
   *         does not exceed the specified limit
   *
   */
  public Todo[] filterTodosByLimit(Todo[] todos, int targetLimit) {
    // learning from the stream documentation here...
    return Arrays.stream(todos).limit(targetLimit).toArray(Todo[]::new);
  }

}
