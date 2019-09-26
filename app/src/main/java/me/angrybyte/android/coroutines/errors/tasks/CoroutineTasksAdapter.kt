package me.angrybyte.android.coroutines.errors.tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.android.synthetic.main.item_task.view.itemTaskIcon
import kotlinx.android.synthetic.main.item_task.view.itemTaskNickname
import me.angrybyte.android.coroutines.R

class CoroutineTasksAdapter(
  private val okTasks: List<CoroutineTask> = CoroutineTask.values().filter { !it.throws },
  private val errTasks: List<CoroutineTask> = CoroutineTask.values().filter { it.throws },
  private val itemClickListener: (CoroutineTask) -> Unit
) : RecyclerView.Adapter<TaskItemViewHolder>() {

  private var isErrorFilter = false
  private val currentList: List<CoroutineTask>
    get() = if (isErrorFilter) errTasks else okTasks

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    LayoutInflater.from(parent.context)
      .inflate(R.layout.item_task, parent, false)
      .let { TaskItemViewHolder(it) }

  override fun getItemCount() = currentList.size

  override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) =
    holder.bind(currentList[position], itemClickListener)

  fun setErrorFilter(isEnabled: Boolean) {
    isErrorFilter = isEnabled
    notifyDataSetChanged()
  }

}

class TaskItemViewHolder(itemView: View) : ViewHolder(itemView) {

  fun bind(task: CoroutineTask, clickListener: (CoroutineTask) -> Unit) =
    with(itemView) {
      itemTaskNickname.text = task.nickname
      itemTaskIcon.setImageResource(
        if (task.throws) R.drawable.ic_error else R.drawable.ic_check_circle
      )
      val color = ContextCompat.getColor(
        context!!,
        if (task.throws) R.color.item_task_err else R.color.item_task_ok
      )
      setBackgroundColor(color)
      setOnClickListener { clickListener(task) }
    }

}