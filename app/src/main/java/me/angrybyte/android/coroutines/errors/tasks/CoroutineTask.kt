package me.angrybyte.android.coroutines.errors.tasks

enum class CoroutineTask(
  val nickname: String,
  val throws: Boolean
) {

  // Successful tasks

  SYNC_OK("Just run", false),
  LAUNCH_OK("Run using 'launch'", false),
  ASYNC_OK("Run using 'async'", false),
  WITH_CONTEXT_OK("Run in IO context", false),
  NEW_SCOPE_OK("Run in a new scope", false),

  SYNC_SAME_PARENT_OK("Just run\n(keep parent)", false),
  LAUNCH__SAME_PARENT_OK("Run using 'launch'\n(keep parent)", false),
  ASYNC_SAME_PARENT_OK("Run using 'async'\n(keep parent)", false),
  WITH_CONTEXT_SAME_PARENT_OK("Run in IO context\n(keep parent)", false),
  NEW_SCOPE_SAME_PARENT_OK("Run in a new scope\n(keep parent)", false),

  SYNC_HANDLER_OK("Just run\n(with handler)", false),
  LAUNCH_HANDLER_OK("Run using 'launch'\n(with handler)", false),
  ASYNC_HANDLER_OK("Run using 'async'\n(with handler)", false),
  WITH_CONTEXT_HANDLER_OK("Run in IO context\n(with handler)", false),
  NEW_SCOPE_HANDLER_OK("Run in a new scope\n(with handler)", false),

  // Failing tasks

  SYNC_ERR("Just run", true),
  LAUNCH_ERR("Run using 'launch'", true),
  ASYNC_ERR("Run using 'async'", true),
  WITH_CONTEXT_ERR("Run in IO context", true),
  NEW_SCOPE_ERR("Run in a new scope", true),

  SYNC_SAME_PARENT_ERR("Just run\n(keep parent)", true),
  LAUNCH__SAME_PARENT_ERR("Run using 'launch'\n(keep parent)", true),
  ASYNC_SAME_PARENT_ERR("Run using 'async'\n(keep parent)", true),
  WITH_CONTEXT_SAME_PARENT_ERR("Run in IO context\n(keep parent)", true),
  NEW_SCOPE_SAME_PARENT_ERR("Run in a new scope\n(keep parent)", true),

  SYNC_HANDLER_ERR("Just run\n(with handler)", true),
  LAUNCH_HANDLER_ERR("Run using 'launch'\n(with handler)", true),
  ASYNC_HANDLER_ERR("Run using 'async'\n(with handler)", true),
  WITH_CONTEXT_HANDLER_ERR("Run in IO context\n(with handler)", true),
  NEW_SCOPE_HANDLER_ERR("Run in a new scope\n(with handler)", true),

  // Rx Tasks
  RX_IO_THREAD_OK("ReactiveX async delivery", false),
  RX_IO_THREAD_ERR("ReactiveX async delivery", true)

}