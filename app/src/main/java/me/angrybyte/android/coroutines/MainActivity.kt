package me.angrybyte.android.coroutines

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.angrybyte.android.coroutines.errors.SlowTasksFragment

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    supportFragmentManager
      .beginTransaction()
      .replace(R.id.fragmentContainer, SlowTasksFragment.newInstance())
      .disallowAddToBackStack()
      .commit()
  }

}
