package me.angrybyte.android.coroutines.errors

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_slow_tasks.statusTextView
import kotlinx.android.synthetic.main.fragment_slow_tasks.switchErrors
import kotlinx.android.synthetic.main.fragment_slow_tasks.switchSimulation
import kotlinx.android.synthetic.main.fragment_slow_tasks.tasksList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.angrybyte.android.coroutines.R
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.ASYNC_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.ASYNC_HANDLER_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.ASYNC_HANDLER_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.ASYNC_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.ASYNC_SAME_PARENT_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.ASYNC_SAME_PARENT_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.LAUNCH_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.LAUNCH_HANDLER_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.LAUNCH_HANDLER_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.LAUNCH_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.LAUNCH__SAME_PARENT_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.LAUNCH__SAME_PARENT_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.NEW_SCOPE_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.NEW_SCOPE_HANDLER_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.NEW_SCOPE_HANDLER_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.NEW_SCOPE_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.NEW_SCOPE_SAME_PARENT_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.NEW_SCOPE_SAME_PARENT_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.RX_IO_THREAD_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.RX_IO_THREAD_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.SYNC_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.SYNC_HANDLER_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.SYNC_HANDLER_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.SYNC_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.SYNC_SAME_PARENT_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.SYNC_SAME_PARENT_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.WITH_CONTEXT_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.WITH_CONTEXT_HANDLER_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.WITH_CONTEXT_HANDLER_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.WITH_CONTEXT_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.WITH_CONTEXT_SAME_PARENT_ERR
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTask.WITH_CONTEXT_SAME_PARENT_OK
import me.angrybyte.android.coroutines.errors.tasks.CoroutineTasksAdapter
import java.lang.System.currentTimeMillis
import kotlin.coroutines.CoroutineContext

private typealias ItemClickListener = (CoroutineTask) -> Unit

@SuppressLint("SetTextI18n")
class SlowTasksFragment : Fragment(), CoroutineScope, ItemClickListener {

  companion object {
    fun newInstance() = SlowTasksFragment()
  }

  private val parentJob = SupervisorJob()
  private val defaultHandler = CoroutineExceptionHandler { _, error ->
    trace("Default handler")
    error.show(-1L)
  }
  override val coroutineContext: CoroutineContext =
    parentJob + Dispatchers.Main + defaultHandler

  private val disposables = CompositeDisposable()

  private val simulate
    get() = switchSimulation.isChecked

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(R.layout.fragment_slow_tasks, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val adapter = CoroutineTasksAdapter(itemClickListener = this)
    with(tasksList) {
      this.adapter = adapter
      layoutManager = LinearLayoutManager(context!!)
      addItemDecoration(DividerItemDecoration(context!!, VERTICAL))
    }
    switchErrors.setOnCheckedChangeListener { _, isChecked ->
      adapter.setErrorFilter(isChecked)
    }
  }

  override fun onStop() {
    super.onStop()
    trace("Canceling...")
    parentJob.cancelChildren()
  }

  override fun invoke(task: CoroutineTask) {
    val start = currentTimeMillis()
    val overrideHandler = CoroutineExceptionHandler { _, error ->
      trace("Override handler")
      error.show(-1)
    }

    when (task) {
      // region Default
      SYNC_OK, SYNC_ERR -> runSync(task, start)

      LAUNCH_OK, LAUNCH_ERR -> runLaunch(task, start)

      ASYNC_OK, ASYNC_ERR -> runAsync(task, start)

      WITH_CONTEXT_ERR, WITH_CONTEXT_OK ->
        launch {
          showProgress()
          try {
            withContext(IO) { task.execute() }
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }

      NEW_SCOPE_OK, NEW_SCOPE_ERR ->
        launch {
          showProgress()
          try {
            coroutineScope { task.execute() }
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }
      // endregion

      // region Same parent
      SYNC_SAME_PARENT_OK, SYNC_SAME_PARENT_ERR ->
        runBlocking(parentJob) {
          showProgress()
          try {
            task.execute()
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }

      LAUNCH__SAME_PARENT_OK, LAUNCH__SAME_PARENT_ERR ->
        launch(parentJob) {
          showProgress()
          try {
            task.execute()
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }

      ASYNC_SAME_PARENT_OK, ASYNC_SAME_PARENT_ERR ->
        launch(parentJob) {
          showProgress()
          try {
            @Suppress("RedundantAsync")
            async { task.execute() }.await()
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }

      WITH_CONTEXT_SAME_PARENT_OK, WITH_CONTEXT_SAME_PARENT_ERR ->
        launch(parentJob) {
          showProgress()
          try {
            withContext(IO) { task.execute() }
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }

      NEW_SCOPE_SAME_PARENT_OK, NEW_SCOPE_SAME_PARENT_ERR ->
        launch(parentJob) {
          showProgress()
          try {
            coroutineScope { task.execute() }
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }
      // endregion

      // region New handler
      SYNC_HANDLER_OK, SYNC_HANDLER_ERR ->
        runBlocking(overrideHandler) {
          showProgress()
          try {
            task.execute()
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }

      LAUNCH_HANDLER_OK, LAUNCH_HANDLER_ERR ->
        launch(overrideHandler) {
          showProgress()
          try {
            task.execute()
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }

      ASYNC_HANDLER_OK, ASYNC_HANDLER_ERR ->
        launch(overrideHandler) {
          showProgress()
          try {
            @Suppress("RedundantAsync")
            async { task.execute() }.await()
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }

      WITH_CONTEXT_HANDLER_OK, WITH_CONTEXT_HANDLER_ERR ->
        launch(overrideHandler) {
          showProgress()
          try {
            withContext(IO) { task.execute() }
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }

      NEW_SCOPE_HANDLER_OK, NEW_SCOPE_HANDLER_ERR ->
        launch(overrideHandler) {
          showProgress()
          try {
            coroutineScope { task.execute() }
            showSuccess(start.elapsed())
          } catch (e: Throwable) {
            throw e.show(start)
          }
        }
      // endregion

      // region ReactiveX
      RX_IO_THREAD_OK, RX_IO_THREAD_ERR -> {
      }
      // endregion
    }

  }

  private suspend fun CoroutineTask.execute() {
    trace("Running: simulated [$simulate] withError[$throws]")
    if (simulate) SlowTask.simulate() else SlowTask.work()
    trace("Work completed, active [$isActive]")
    check(!throws) { "Boom!" }
  }

  private fun showProgress() =
    with(statusTextView) {
      trace("Showing progress")
      val color = ContextCompat.getColor(context!!, android.R.color.white)
      setTextColor(color)
      text = "Working now.."
    }

  private fun showSuccess(value: Long) =
    with(statusTextView) {
      trace("Showing success [$value]")
      setTextColor(0xFF40CC40.toInt())
      text = "Done in ${value}ms"
    }

  private fun showError(value: Long) =
    with(statusTextView) {
      trace("Showing error [$value]")
      setTextColor(0xFFCC4040.toInt())
      text = "Failed in ${value}ms"
    }

  private fun showCrash() =
    with(statusTextView) {
      trace("Showing crash")
      setTextColor(0xFFFF1010.toInt())
      text = "Crash prevented!"
    }

  private fun showCancellation(value: Long) =
    with(statusTextView) {
      trace("Showing cancellation [$value]")
      setTextColor(0xFFCCFF40.toInt())
      text = "Canceled in ${value}ms"
    }

  private fun Throwable.show(start: Long) = apply {
    if (this is CancellationException) {
      showCancellation(start.elapsed())
    } else {
      if (start == -1L) {
        showCrash()
      } else {
        showError(start.elapsed())
      }
    }
  }

  // for de-compilation:

  private fun runSync(task: CoroutineTask, start: Long) =
    runBlocking {
      showProgress()
      try {
        task.execute()
        showSuccess(start.elapsed())
      } catch (e: Throwable) {
        throw e.show(start)
      }
    }

  private fun runLaunch(task: CoroutineTask, start: Long) =
    launch {
      showProgress()
      try {
        task.execute()
        showSuccess(start.elapsed())
      } catch (e: Throwable) {
        throw e.show(start)
      }
    }

  private fun runAsync(task: CoroutineTask, start: Long) =
    launch {
      try {
        @Suppress("RedundantAsync")
        async { task.execute() }.await()
        showSuccess(start.elapsed())
      } catch (e: Throwable) {
        throw e.show(start)
      }
    }

}

private object SlowTask {

  private const val TASK_DELAY = 2000L

  fun work() {
    val start = currentTimeMillis()
    while (currentTimeMillis() - start < TASK_DELAY) {
      // loop actively
    }
  }

  suspend fun simulate() = delay(TASK_DELAY)

}

private fun Long.elapsed() = currentTimeMillis() - this

private fun trace(content: String) = Log.d(
  "SlowTaskDemo",
  "'$content' is on [${Thread.currentThread().name}]"
)
