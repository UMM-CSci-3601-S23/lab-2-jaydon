//Adapted from UserController.java

package umm3601.todo;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;

/**
 * Controller that manages requests for info about users.
 */
public class TodoController {

  private TodoDatabase database;

  /**
   * Construct a controller for todos.
   * <p>
   * This loads the "database" of todo info from a JSON file and stores that
   * internally so that (subsets of) todos can be returned in response to
   * requests.
   *
   * @param database the `Database` containing user data
   */
  public TodoController(TodoDatabase database) {
    this.database = database;
  }

}
